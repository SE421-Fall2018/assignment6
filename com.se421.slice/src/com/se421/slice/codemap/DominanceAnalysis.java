package com.se421.slice.codemap;

import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.se421.slice.analysis.utilities.CommonQueries;
import com.se421.slice.analysis.utilities.dominance.DominatorTree;
import com.se421.slice.analysis.utilities.dominance.PostDominatorTree;
import com.se421.slice.analysis.utilities.dominance.UniqueEntryExitControlFlowGraph;
import com.se421.slice.analysis.utilities.dominance.UniqueEntryExitGraph;
import com.se421.slice.log.Log;

/**
 * Primary interface for computing dominance relationships. This also acts as a
 * code map stage to pre-compute dominance relationships for control flow
 * graphs.
 * 
 * @author Ben Holland
 */
public class DominanceAnalysis {

	/**
	 * The immediate dominator or idom of a node n is the unique node that strictly
	 * dominates n but does not strictly dominate any other node that strictly
	 * dominates n. Every node, except the entry node, has an immediate dominator.
	 * Because the immediate dominator is unique, it is a tree. The start node is
	 * the root of the tree.
	 */
	public static final String DOMINATOR_TREE_EDGE = "idom";
	
	/**
	 * Used to tag the edges from a node that post-dominate a node.
	 * 
	 * Wikipedia: Analogous to the definition of dominance above, a node z is said
	 * to post-dominate a node n if all paths to the exit node of the graph starting
	 * at n must go through z. Similarly, the immediate post-dominator of a node n
	 * is the postdominator of n that doesn't strictly postdominate any other strict
	 * postdominators of n.
	 */
	public static final String POST_DOMINATOR_TREE_EDGE = "ipdom";
	
	/**
	 * Used to tag the edges from a node that identify the node's dominance
	 * frontier.
	 * 
	 * Wikipedia: The dominance frontier of a node d is the set of all nodes n
	 * such that d dominates an immediate predecessor of n, but d does not
	 * strictly dominate n. It is the set of nodes where d's dominance stops.
	 */
	public static final String DOMINANCE_FRONTIER_EDGE = "dom-frontier";
	
	/**
	 * Used to tag the edges from a node that identify the node's post-dominance
	 * frontier.
	 * 
	 * Wikipedia: The dominance frontier of a node d is the set of all nodes n
	 * such that d dominates an immediate predecessor of n, but d does not
	 * strictly dominate n. It is the set of nodes where d's dominance stops.
	 * Note that analogous to the definition of dominance above, a node z is said
	 * to post-dominate a node n if all paths to the exit node of the graph starting
	 * at n must go through z. Similarly, the immediate post-dominator of a node n
	 * is the postdominator of n that doesn't strictly postdominate any other strict
	 * postdominators of n.
	 */
	public static final String POST_DOMINANCE_FRONTIER_EDGE = "pdom-frontier";
	
	public DominanceAnalysis() {}
	
	public static Q getDominatorTreeEdges(){
		return Query.universe().edges(DOMINATOR_TREE_EDGE).retainEdges();
	}
	
	public static Q getPostDominatorTreeEdges(){
		return Query.universe().edges(POST_DOMINATOR_TREE_EDGE).retainEdges();
	}
	
	public static Q getDominanceFrontierEdges(){
		return Query.universe().edges(DOMINANCE_FRONTIER_EDGE).retainEdges();
	}
	
	public static Q getPostDominanceFrontierEdges(){
		return Query.universe().edges(POST_DOMINANCE_FRONTIER_EDGE).retainEdges();
	}

	public static String displayName() {
		return "Computing Control Flow Graph Dominance";
	}

	public static void performIndexing(IProgressMonitor monitor) {
		Log.info("Computing Control Flow Graph Dominator Trees");
		AtlasSet<Node> functions = Query.resolve(null, Query.universe().nodes(XCSG.Function).eval().nodes());
		SubMonitor task = SubMonitor.convert(monitor, (int) functions.size());
		int functionsCompleted = 0;
		for(Node function : functions){
			Q cfg;
			boolean includeExceptionalEdges = false;
			if(includeExceptionalEdges){
				cfg = CommonQueries.excfg(function);
			} else {
				cfg = CommonQueries.cfg(function);
			}
			Graph g = cfg.eval();
			AtlasSet<Node> roots = cfg.nodes(XCSG.controlFlowRoot).eval().nodes();
			AtlasSet<Node> exits = cfg.nodes(XCSG.controlFlowExitPoint).eval().nodes();
			if(g.nodes().isEmpty() || roots.isEmpty() || exits.isEmpty()){
				// nothing to compute
				task.setWorkRemaining(((int) functions.size())-(functionsCompleted++));
				continue;
			} else {
				try {
					UniqueEntryExitGraph uexg = new UniqueEntryExitControlFlowGraph(g, roots, exits, true);
					computeDominance(uexg);
				} catch (Exception e){
					Log.error("Error computing control flow graph dominance tree", e);
				}
				if(monitor.isCanceled()){
					Log.warning("Cancelled: Computing Control Flow Graph Dominator Trees");
					break;
				}
				task.setWorkRemaining(((int) functions.size())-(functionsCompleted++));
			}
		}
	}

	/**
	 * Returns the immediate dominance tree
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computeDominanceTree(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> treeEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(DOMINATOR_TREE_EDGE)){
				treeEdges.add(edge);
			}
		}
		return Common.toQ(treeEdges).eval();
	}
	
	/**
	 * Returns the dominance frontier
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computeDominanceFrontier(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> frontierEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(DOMINANCE_FRONTIER_EDGE)){
				frontierEdges.add(edge);
			}
		}
		return Common.toQ(frontierEdges).eval();
	}
	
	/**
	 * Returns the post dominance graph
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computePostDominanceTree(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> treeEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(POST_DOMINATOR_TREE_EDGE)){
				treeEdges.add(edge);
			}
		}
		return Common.toQ(treeEdges).eval();
	}
	
	/**
	 * Returns the post-dominance frontier
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computePostDominanceFrontier(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> frontierEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(POST_DOMINANCE_FRONTIER_EDGE)){
				frontierEdges.add(edge);
			}
		}
		return Common.toQ(frontierEdges).eval();
	}

	/**
	 * Returns a graph of all dominance relationship edges
	 * @param ucfg
	 * @return
	 */
	public static Graph computeDominance(UniqueEntryExitGraph ucfg) {
		AtlasSet<Edge> dominanceEdges = new AtlasHashSet<Edge>();
		
		// compute the immediate dominator tree (idom)
		DominatorTree dominatorTree = new DominatorTree(ucfg);
		for(Entry<Node,Node> entry : dominatorTree.getIdoms().entrySet()) {
			Node fromNode = entry.getKey();
			Node toNode = entry.getValue();
			Q idomEdges = Query.universe().edges(DOMINATOR_TREE_EDGE);
			Edge idomEdge = idomEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
			if(idomEdge == null){
				idomEdge = Graph.U.createEdge(fromNode, toNode);
				idomEdge.tag(DOMINATOR_TREE_EDGE);
				idomEdge.putAttr(XCSG.name, DOMINATOR_TREE_EDGE);
			}
			dominanceEdges.add(idomEdge);
		}
		
		// compute the dominance frontier
		DominatorTree.Multimap<Node> dominanceFrontier = dominatorTree.getDominanceFrontiers();
		for(Entry<Node, Set<Node>> entry : dominanceFrontier.entrySet()){
			Node fromNode = entry.getKey();
			for(Node toNode : entry.getValue()){
				Q dominanceFrontierEdges = Query.universe().edges(DOMINANCE_FRONTIER_EDGE);
				Edge dominanceFrontierEdge = dominanceFrontierEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
				if(dominanceFrontierEdge == null){
					dominanceFrontierEdge = Graph.U.createEdge(fromNode, toNode);
					dominanceFrontierEdge.tag(DOMINANCE_FRONTIER_EDGE);
					dominanceFrontierEdge.putAttr(XCSG.name, DOMINANCE_FRONTIER_EDGE);
				}
				dominanceEdges.add(dominanceFrontierEdge);
			}
		}
		
		// compute the post-dominator tree (postdom)
		PostDominatorTree postDominatorTree = new PostDominatorTree(ucfg);
		for(Entry<Node,Node> entry : postDominatorTree.getIdoms().entrySet()) {
			Node fromNode = entry.getValue();
			Node toNode = entry.getKey();
			Q postdomEdges = Query.universe().edges(POST_DOMINATOR_TREE_EDGE);
			Edge postdomEdge = postdomEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
			if(postdomEdge == null){
				postdomEdge = Graph.U.createEdge(fromNode, toNode);
				postdomEdge.tag(POST_DOMINATOR_TREE_EDGE);
				postdomEdge.putAttr(XCSG.name, POST_DOMINATOR_TREE_EDGE);
			}
			dominanceEdges.add(postdomEdge);
		}
		
		// compute the post-dominance frontier
		PostDominatorTree.Multimap<Node> postDominanceFrontier = postDominatorTree.getDominanceFrontiers();
		for(Entry<Node, Set<Node>> entry : postDominanceFrontier.entrySet()){
			Node fromNode = entry.getKey();
			for(Node toNode : entry.getValue()){
				Q dominanceFrontierEdges = Query.universe().edges(POST_DOMINANCE_FRONTIER_EDGE);
				Edge dominanceFrontierEdge = dominanceFrontierEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
				if(dominanceFrontierEdge == null){
					dominanceFrontierEdge = Graph.U.createEdge(fromNode, toNode);
					dominanceFrontierEdge.tag(POST_DOMINANCE_FRONTIER_EDGE);
					dominanceFrontierEdge.putAttr(XCSG.name, POST_DOMINANCE_FRONTIER_EDGE);
				}
				dominanceEdges.add(dominanceFrontierEdge);
			}
		}
		
		return Common.toQ(dominanceEdges).eval();
	}

}