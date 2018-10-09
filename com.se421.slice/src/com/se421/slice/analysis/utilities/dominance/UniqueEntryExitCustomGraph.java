package com.se421.slice.analysis.utilities.dominance;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;

public class UniqueEntryExitCustomGraph implements UniqueEntryExitGraph {

	/**
	 * The set of nodes in the current graph
	 */
	private AtlasSet<Node> nodes;
	
	/**
	 * The set of edges in the current graph
	 */
	private AtlasSet<Edge> edges;
	
	private Node masterEntry;
	private Node masterExit;
	
	/** 
	 * @param a wrapper for a custom pre-formed entry/exit graph
	 */
	public UniqueEntryExitCustomGraph(Graph graph, Node masterEntry, Node masterExit) {
		if(!graph.nodes().contains(masterEntry)){
			throw new IllegalArgumentException("Graph does not contain the master entry!");
		}
		if(!graph.nodes().contains(masterExit)){
			throw new IllegalArgumentException("Graph does not contain the master exit!");
		}
		this.nodes = new AtlasHashSet<Node>(graph.nodes());
		this.edges = new AtlasHashSet<Edge>(graph.edges());
		this.masterEntry = masterEntry;
		this.masterExit = masterExit;
	}
	
	/**
	 * Gets the predecessors of a given node
	 * @param node
	 * @return Predecessors of node
	 */
	@Override
	public AtlasSet<Node> getPredecessors(Node node){
		AtlasSet<Node> predecessors = new AtlasHashSet<Node>();
		for(Edge edge : this.edges()){
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
		for(Edge edge : this.edges()){
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
