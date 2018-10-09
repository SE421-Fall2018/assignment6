package com.se421.slice.ui.smart;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.UncheckedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.FrontierStyledResult;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IExplorableScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IResizableScript;
import com.ensoftcorp.atlas.ui.scripts.util.SimpleScriptUtil;
import com.ensoftcorp.atlas.ui.selection.event.FrontierEdgeExploreEvent;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.se421.slice.analysis.utilities.CommonQueries;
import com.se421.slice.analysis.utilities.dominance.UniqueEntryExitControlFlowGraph;

public abstract class ControlFlowDominanceSmartView extends FilteringAtlasSmartViewScript implements IResizableScript, IExplorableScript {

	protected abstract Q getDominanceEdges();
	
	private static final boolean INCLUDE_EXCEPTIONAL_CONTROL_FLOW = false;
	
	@Override
	protected String[] getSupportedNodeTags() {
		return new String[] { XCSG.DataFlow_Node, XCSG.ControlFlow_Node, XCSG.Function };
	}
	
	@Override
	protected String[] getSupportedEdgeTags() {
		return NOTHING;
	}

	@Override
	public int getDefaultStepTop() {
		return 1;
	}

	@Override
	public int getDefaultStepBottom() {
		return 1;
	}
	
	@Override
	public FrontierStyledResult explore(FrontierEdgeExploreEvent event, FrontierStyledResult oldResult) {
		return SimpleScriptUtil.explore(this, event, oldResult);
	}
	
	private Q getCFG(Q functions){
		AtlasSet<Node> nodes = new AtlasHashSet<Node>();
		AtlasSet<Edge> edges = new AtlasHashSet<Edge>();
		for(Node function : functions.eval().nodes()) {
			Q cfg;
			if(INCLUDE_EXCEPTIONAL_CONTROL_FLOW){
				cfg = CommonQueries.excfg(function);
			} else {
				cfg = CommonQueries.cfg(function);
			}
			UniqueEntryExitControlFlowGraph ucfg = new UniqueEntryExitControlFlowGraph(cfg.eval(), true);
			Graph graph = ucfg.getGraph();
			nodes.addAll(graph.nodes());
			edges.addAll(graph.edges());
		}
		return Common.toQ(new UncheckedGraph(nodes,edges));
	}
	
	public Q convertSelection(Q filteredSelections){
		Q dataFlowNodes = filteredSelections.nodes(XCSG.DataFlow_Node);
		Q controlFlowNodes = filteredSelections.nodes(XCSG.ControlFlow_Node);
		Q functions = filteredSelections.nodes(XCSG.Function);
		
		// convert data flow nodes to control flow nodes
		return controlFlowNodes.union(functions, dataFlowNodes.parent());
	}

	private UnstyledFrontierResult computeFrontierResult(Q origin, Q graph, int reverse, int forward){
		// compute what to show for current steps
		Q f = origin.forwardStepOn(graph, forward);
		Q r = origin.reverseStepOn(graph, reverse);
		Q result = f.union(r);
		
		// compute what is on the frontier
		Q frontierReverse = origin.reverseStepOn(graph, reverse+1);
		frontierReverse = frontierReverse.differenceEdges(result).retainEdges();
		Q frontierForward = origin.forwardStepOn(graph, forward+1);
		frontierForward = frontierForward.differenceEdges(result).retainEdges();
		
		// show the result
		return new UnstyledFrontierResult(result, frontierReverse, frontierForward);
	}
	
	@Override
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward) {
		
		Q filteredSelections = filter(event.getSelection());
		if(filteredSelections.eval().nodes().isEmpty()){
			return null;
		}
		
		AtlasSet<Node> dataFlowNodes = filteredSelections.nodes(XCSG.DataFlow_Node).eval().nodes();
		AtlasSet<Node> correspondingControlFlowStatements = Common.toQ(dataFlowNodes).parent().nodes(XCSG.ControlFlow_Node).eval().nodes();
		AtlasSet<Node> functions = filteredSelections.nodes(XCSG.Function).eval().nodes();
		Q selectedStatements = filteredSelections.difference(Common.toQ(functions), Common.toQ(dataFlowNodes)).union(Common.toQ(correspondingControlFlowStatements));
		Q containingFunctions = CommonQueries.getContainingFunctions(selectedStatements);
		
		FrontierStyledResult styledResult;
		if(functions.isEmpty()){
			// just cfg nodes were selected
			Q cfgs = getCFG(containingFunctions);
			
			Q dominanceEdges = getDominanceEdges();
			Q dominance = cfgs.retainNodes().induce(dominanceEdges);
			
			UnstyledFrontierResult frontier = computeFrontierResult(selectedStatements, dominance, reverse, forward);
			
			// a selection could include a function, so explicitly include it in the result to be highlighted
			Q result = frontier.getResult().union(filteredSelections.nodes(XCSG.Function));
			styledResult = new FrontierStyledResult(result, frontier.getFrontierReverse(), frontier.getFrontierForward(), new Markup());
		} else {
			// a function was selected possibly along with cfg nodes
			Q cfgs = getCFG(containingFunctions);
			Q selectedFunctions = Common.toQ(functions);
			
//			// remove any functions that are selected because callsites were selected
//			Q selectedCallsites = selectedStatements.children().nodes(XCSG.CallSite);
//			Q selectedCallsiteFunctions = CallSiteAnalysis.getTargets(selectedCallsites);
//			selectedFunctions = selectedFunctions.difference(selectedCallsiteFunctions);
			
			// get the complete CFGs for any intentionally selected function
			Q selectedFunctionCFGs = getCFG(selectedFunctions);

			// just pretend the entire cfg was selected for selected functions
			selectedStatements = selectedStatements.union(selectedFunctionCFGs);
			
			Q allCFGs = cfgs.union(selectedFunctionCFGs);

			Q dominanceEdges = getDominanceEdges();
			Q dominance = allCFGs.retainNodes().induce(dominanceEdges);
			
			UnstyledFrontierResult frontier = computeFrontierResult(selectedStatements, dominance, reverse, forward);
			
			// a selection could include a function, so explicitly include it in the result to be highlighted
			Q result = frontier.getResult().union(filteredSelections.nodes(XCSG.Function));
			styledResult = new FrontierStyledResult(result, frontier.getFrontierReverse(), frontier.getFrontierForward(), new Markup());
		}
		
		styledResult.setInput(convertSelection(filteredSelections));
		return styledResult;
	}

	@Override
	protected StyledResult selectionChanged(IAtlasSelectionEvent event, Q filteredSelection) {
		return null;
	}
}