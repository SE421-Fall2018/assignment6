package com.se421.slice.analysis;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.se421.slice.codemap.DominanceAnalysis;

/**
 * Constructs the Control Dependence Graph (CGD) from a given Control Flow Graph (CFG)
 * 
 * Instead of computing control dependence using the Ferrante, Ottenstein, and Warren algorithm
 * this uses the post-dominance frontier to compute the dominance edges.
 * 
 * Reference: 
 * 1) http://www.cc.gatech.edu/~harrold/6340/cs6340_fall2009/Slides/BasicAnalysis4.pdf
 * 2) https://www.cc.gatech.edu/~harrold/6340/cs6340_fall2009/Slides/BasicAnalysis5.pdf
 * 3) http://www.cs.utexas.edu/~pingali/CS380C/2016-fall/lectures/Dominators.pdf
 * 
 * @author Ben Holland
 */
public class ControlDependenceGraph extends DependenceGraph {

	/**
	 * Used to tag the edges between nodes that contain a control dependence
	 */
	public static final String CONTROL_DEPENDENCE_EDGE = "control-dependence";

	private Graph cfg;
	private Graph cdg;
	
	public ControlDependenceGraph(Graph cfg){
		// sanity checks
		if(cfg.nodes().isEmpty() || cfg.edges().isEmpty()){
			this.cdg = Common.toQ(cfg).eval();
			this.cfg = Common.toQ(cfg).eval();
			return;
		}
		
		
		AtlasSet<Edge> controlDependenceEdgeSet = new AtlasHashSet<Edge>();
		AtlasSet<Edge> dominanceFrontierEdges = DominanceAnalysis.getPostDominanceFrontierEdges().eval().edges();
		
		// TODO: implement
		// for each edge in the dominance frontier edges (x --pdomf--> y)
		// find or create a a control dependence edge from the successor to the predecessor (y --control-dependence--> x)
		// add the resulting edge to the controlDependenceEdgeSet
		
		this.cdg = Common.toQ(controlDependenceEdgeSet).eval();
	}

	/**
	 * Finds or creates a control dependence edge
	 * @param controlDependenceEdgeSet
	 * @param fromStatement
	 * @param toStatement
	 */
	private Edge findOrCreateControlDependenceEdge(Node fromStatement, Node toStatement) {
		Q controlDependenceEdges = Query.universe().edges(CONTROL_DEPENDENCE_EDGE);
		Edge controlDependenceEdge = controlDependenceEdges.betweenStep(Common.toQ(fromStatement), Common.toQ(toStatement)).eval().edges().one();
		if(controlDependenceEdge == null){
			controlDependenceEdge = Graph.U.createEdge(fromStatement, toStatement);
			controlDependenceEdge.tag(CONTROL_DEPENDENCE_EDGE);
			controlDependenceEdge.putAttr(XCSG.name, CONTROL_DEPENDENCE_EDGE);
		}
		return controlDependenceEdge;
	}
	
	public Q getControlFlowGraph(){
		return Common.toQ(cfg);
	}
	
	public Q getGraph(){
		return Common.toQ(cdg);
	}
	
}
