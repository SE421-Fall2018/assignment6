package com.se421.slice.analysis.utilities.dominance;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;

public interface UniqueEntryExitGraph {

	/**
	 * Returns the predecessors of a given node
	 */
	public AtlasSet<Node> getPredecessors(Node node);

	/**
	 * Returns the successors of a given node
	 */
	public AtlasSet<Node> getSuccessors(Node node);

	/**
	 * Returns the master entry node
	 */
	public Node getEntryNode();

	/**
	 * Returns the master exit node
	 */
	public Node getExitNode();

	/**
	 * The set of nodes in the graph
	 * @return
	 */
	public AtlasSet<Node> nodes();

	/**
	 * The set of edges in the graph
	 * @return
	 */
	public AtlasSet<Edge> edges();
}