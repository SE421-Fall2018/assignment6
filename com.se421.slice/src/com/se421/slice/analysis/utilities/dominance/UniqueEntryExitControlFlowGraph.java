package com.se421.slice.analysis.utilities.dominance;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.se421.slice.analysis.utilities.CommonQueries;
import com.se421.slice.log.Log;

public class UniqueEntryExitControlFlowGraph implements UniqueEntryExitGraph {

	/**
	 * Tag applied to the newly created master entry node
	 */
	public static final String UniqueEntryExitCFG_Master_Entry = "UniqueEntryExitCFG_Master_Entry";
	
	/**
	 * Tag applied to the newly create master exit node
	 */
	public static final String UniqueEntryExitCFG_Master_Exit = "UniqueEntryExitCFG_Master_Exit";
	
	/**
	 * The name attribute applied to the EventFlow_Master_Entry of the PCG
	 */
	public static final String UniqueEntryExitCFG_Master_Entry_Name = "\u22A4";
	
	/**
	 * The name attribute applied to the EventFlow_Master_Exit of the PCG
	 */
	public static final String UniqueEntryExitCFG_Master_Exit_Name = "\u22A5";
	
	/**
	 * Tag applied to the newly created edges between the master entry and the cfg roots 
	 * and the cfg exits and the master exit
	 */
	public static final String UniqueEntryExitCFG_Edge = "UniqueEntryExitCFG_Edge";
	
	/**
	 * The control flow graph
	 */
	private Graph cfg;
	
	/**
	 * The set of CFG roots
	 */
	private AtlasSet<Node> roots;
	
	/**
	 * The set of CFG exits
	 */
	private AtlasSet<Node> exits;
	
	/**
	 * The set of nodes in the current graph
	 */
	private AtlasSet<Node> nodes;
	
	/**
	 * The set of edges in the current graph
	 */
	private AtlasSet<Edge> edges;

	/**
	 * The master entry node
	 */
	private Node masterEntry;
	
	/**
	 * The master exit node
	 */
	private Node masterExit;
	
	/**
	 * The function containing the CFG
	 */
	private Node function;
	
	/**
	 * Constructs a new unique entry/exit control flow graph. Assumes the entry
	 * is tagged with XCSG.controlFlowRoot and the exits are tagged with
	 * XCSG.controlFlowExitPoint
	 * 
	 * @param cfg
	 *            a control flow graph
	 */
	public UniqueEntryExitControlFlowGraph(Graph cfg) {
		this(cfg, Common.toQ(cfg).nodes(XCSG.controlFlowRoot).eval().nodes(), Common.toQ(cfg).nodes(XCSG.controlFlowExitPoint).eval().nodes(), false);
	}
	
	/**
	 * Constructs a new unique entry/exit control flow graph. Assumes the entry
	 * is tagged with XCSG.controlFlowRoot and the exits are tagged with
	 * XCSG.controlFlowExitPoint
	 * 
	 * Optionally, containment edges can be added for display purposes
	 * 
	 * @param cfg
	 * @param addContains
	 */
	public UniqueEntryExitControlFlowGraph(Graph cfg, boolean addContains) {
		this(cfg, Common.toQ(cfg).nodes(XCSG.controlFlowRoot).eval().nodes(), Common.toQ(cfg).nodes(XCSG.controlFlowExitPoint).eval().nodes(), addContains);
	}
	
	/**
	 * Constructs a new unique entry/exit control flow graph with the specified
	 * entry and exit points.
	 * 
	 * @param cfg
	 *            a control flow graph
	 * @param roots
	 * @param exits
	 */
	public UniqueEntryExitControlFlowGraph(Graph cfg, AtlasSet<Node> roots, AtlasSet<Node> exits) {
		this(cfg, roots, exits, false);
	}
	
	/**
	 * Constructs a new unique entry/exit control flow graph with the specified
	 * entry and exit points.
	 * 
	 * @param cfg
	 *            a control flow graph
	 * @param roots
	 * @param exits
	 * @param addContains
	 */
	public UniqueEntryExitControlFlowGraph(Graph cfg, AtlasSet<Node> roots, AtlasSet<Node> exits, boolean addContains) {
		this(cfg, roots, false, exits, false, false);
	}
	
	/**
	 * Constructs a new unique entry/exit control flow graph with the specified
	 * entry and exit points.
	 * 
	 * @param cfg
	 * @param roots
	 * @param relaxNonEmptyRootsRequirement Relaxes the requirement that roots must be a non-empty set
	 * @param exits
	 * @param relaxNonEmptyExitsRequirement Relaxes the requirement that exist must be a non-empty set
	 * @param addContains
	 */
	public UniqueEntryExitControlFlowGraph(Graph cfg, AtlasSet<Node> roots, boolean relaxNonEmptyRootsRequirement, AtlasSet<Node> exits, boolean relaxNonEmptyExitsRequirement, boolean addContains) {
		AtlasSet<Node> functions = CommonQueries.getContainingFunctions(Common.toQ(cfg)).eval().nodes();
		if(functions.isEmpty()){
			String message = "CFG is empty or is not contained within a function!";
			IllegalArgumentException e = new IllegalArgumentException(message);
			Log.error(message, e);
			throw e;
		} else if(functions.size() > 1){
			String message = "CFG should be restricted to a single function!";
			IllegalArgumentException e = new IllegalArgumentException(message);
			Log.error(message, e);
			throw e;
		} else {
			this.function = functions.one();
			this.cfg = cfg;
			this.nodes = new AtlasHashSet<Node>(cfg.nodes());
			this.edges = new AtlasHashSet<Edge>(cfg.edges());
			
			this.roots = Common.toQ(roots).intersection(Common.toQ(cfg)).eval().nodes();

			if(!relaxNonEmptyRootsRequirement && this.roots.isEmpty()){
				String message = "CFG roots must be a non-empty set contained within the CFG!";
				IllegalArgumentException e = new IllegalArgumentException(message);
				Log.error(message, e);
				throw e;
			}
			
			this.exits = Common.toQ(exits).intersection(Common.toQ(cfg)).eval().nodes();
			if(!relaxNonEmptyExitsRequirement && this.exits.isEmpty()){
				String message = "CFG exits must be a non-empty set contained within the CFG!";
				IllegalArgumentException e = new IllegalArgumentException(message);
				Log.error(message, e);
				throw e;
			}
			
			this.masterEntry = setupMasterEntryNode(this.roots, addContains);
			this.masterExit = setupMasterExitNode(this.exits, addContains);
		}
	}
	
	/**
	 * Creates the nodes and edges for setting up the master entry node
	 * @param roots nodes to consider as control flow roots (entry points) in the graph
	 */
	private Node setupMasterEntryNode(AtlasSet<Node> roots, boolean addContains){
		// search if the function has a master entry node created previously
		Node masterEntryNode;
		if(addContains){
			// note we are reusing master entry nodes so the search should be from
			// as a child of the function
			masterEntryNode = Common.toQ(function).children()
					.nodes(UniqueEntryExitCFG_Master_Entry)
					.eval().nodes().one();
		} else {
			// note we are reusing master entry nodes so the search should be from
			// the entire function cfg not just the specified roots
			masterEntryNode = Query.universe()
					.predecessors(CommonQueries.cfg(function))
					.nodes(UniqueEntryExitCFG_Master_Entry)
					.eval().nodes().one();
		}
		
		// if master entry node has not been created previously, then we need to
		// create one now
		if (masterEntryNode == null) {
			masterEntryNode = Graph.U.createNode();
			masterEntryNode.attr().put(XCSG.name, UniqueEntryExitCFG_Master_Entry_Name);
			masterEntryNode.tag(UniqueEntryExitCFG_Master_Entry);
			if(addContains){
				Edge containsEdge = Graph.U.createEdge(function, masterEntryNode);
				containsEdge.tag(XCSG.Contains);
			}
		}
		
		// add the master entry node
		this.nodes.add(masterEntryNode);
		
		// create entry edges from the master entry to the root nodes
		// and check if the entry edges exist before creating new ones
		for(Node root : roots){
			Edge entryEdge = this.getOrCreateEdge(masterEntryNode, root);
			this.edges.add(entryEdge);
		}
		return masterEntryNode;
	}

	/**
	 * Creates the nodes and edges for setting up the master exit node
	 * @param exits nodes to consider as control flow exits (exit points) in the graph
	 * @return
	 */
	private Node setupMasterExitNode(AtlasSet<Node> exits, boolean addContains) {
		// search if the function has a master entry node created previously
		Node masterExitNode;
		if(addContains){
			// note we are reusing master entry nodes so the search should be from
			// as a child of the function
			masterExitNode = Common.toQ(function).children()
					.nodes(UniqueEntryExitCFG_Master_Exit)
					.eval().nodes().one();
		} else {
			// note we are reusing master entry nodes so the search should be from
			// the entire function cfg not just the specified roots
			masterExitNode = Query.universe()
					.successors(CommonQueries.cfg(function))
					.nodes(UniqueEntryExitCFG_Master_Exit)
					.eval().nodes().one();
		}
		
		// if master exit node has not been created previously, then we need to
		// create one now
		if (masterExitNode == null) {
			masterExitNode = Graph.U.createNode();
			masterExitNode.attr().put(XCSG.name, UniqueEntryExitCFG_Master_Exit_Name);
			masterExitNode.tag(UniqueEntryExitCFG_Master_Exit);
			if(addContains){
				Edge containsEdge = Graph.U.createEdge(function, masterExitNode);
				containsEdge.tag(XCSG.Contains);
			}
		}
		
		// add the master exit node to the pcg
		this.nodes.add(masterExitNode);

		// create exit edges from the exits to the master exit node
		// and check if the exit edges exist before creating new ones
		for (Node exit : exits) {
			Edge exitEdge = this.getOrCreateEdge(exit, masterExitNode);
			this.edges.add(exitEdge);
		}
		return masterExitNode;
	}
	
	private Edge getOrCreateEdge(Node masterEntryNode, Node root) {
		AtlasSet<Edge> betweenEdges = Query.universe().edges(UniqueEntryExitCFG_Edge).betweenStep(Common.toQ(masterEntryNode), Common.toQ(root)).eval().edges();
		if (!betweenEdges.isEmpty()) {
			return betweenEdges.one();
		} else {
			Edge edge = Graph.U.createEdge(masterEntryNode, root);
			edge.tag(XCSG.Edge);
			edge.tag(UniqueEntryExitCFG_Edge);
			return edge;
		}
	}
	
	public Graph getCFG(){
		return cfg;
	}
	
	public AtlasSet<Node> getRoots(){
		return roots;
	}
	
	public AtlasSet<Node> getExits(){
		return exits;
	}
	
	public Node getFunction(){
		return function;
	}
	
	public Graph getGraph(){
		return Common.toQ(nodes).induce(Common.toQ(edges)).eval();
	}
	
	/**
	 * Gets the predecessors of a given node
	 * @param node
	 * @return Predecessors of node
	 */
	@Override
	public AtlasSet<Node> getPredecessors(Node node){
		AtlasSet<Node> predecessors = new AtlasHashSet<Node>();
		for(Edge edge : edges()){
			if(edge.getNode(EdgeDirection.TO).equals(node)){
				Node parent = edge.getNode(EdgeDirection.FROM);
				predecessors.add(parent);
			}
		}
		return predecessors;
	}

	/**
	 * Gets the successors of a given node 
	 * @param node
	 * @return Successors of node
	 */
	@Override
	public AtlasSet<Node> getSuccessors(Node node){		
		AtlasSet<Node> successors = new AtlasHashSet<Node>();
		for(Edge edge : edges()){
			if(edge.getNode(EdgeDirection.FROM).equals(node)){
				Node child = edge.getNode(EdgeDirection.TO);
				successors.add(child);
			}
		}
		return successors;
	}

	@Override
	public Node getEntryNode() {
		return masterEntry;
	}

	@Override
	public Node getExitNode() {
		return masterExit;
	}

	@Override
	public AtlasSet<Node> nodes() {
		return nodes;
	}

	@Override
	public AtlasSet<Edge> edges() {
		return edges;
	}
	
}
