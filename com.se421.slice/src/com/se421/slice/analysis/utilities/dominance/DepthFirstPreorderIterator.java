package com.se421.slice.analysis.utilities.dominance;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;

/**
 * DepthFirstPreorderIterator yields a depth-first pre-order traversal of a graph.
 */
public class DepthFirstPreorderIterator implements Iterator<Node> {

	/**
	 * Unique Entry/Exit Graph to operate on
	 */
	protected UniqueEntryExitGraph graph;

	/**
	 * @param roots
	 *            the caller's root(s) of the flowgraph. There should be only one
	 *            start block, but multiple roots are tolerated to work around fuzzy
	 *            successor logic to exception handlers.
	 */
	public DepthFirstPreorderIterator(UniqueEntryExitGraph graph, AtlasSet<Node> roots) {
		this.graph = graph;
		for (Node root : roots) {
			this.stack.add(root);
		}
	}

	public DepthFirstPreorderIterator(UniqueEntryExitGraph graph, Node root) {
		this.graph = graph;
		this.stack.add(root);
	}

	/**
	 * The to-be-visited stack of blocks.
	 */
	private Stack<Node> stack = new Stack<Node>();

	/**
	 * The set of edges already traversed.
	 */
	private Set<Edge> visitedEdges = new HashSet<Edge>();

	@Override
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	@Override
	public Node next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		Node next = stack.pop();
		pushSuccessors(next);
		return next;
	}

	/**
	 * Traverse any previously-untraversed edges by adding the destination block to
	 * the to-do stack.
	 * 
	 * @param b
	 *            the current block.
	 */
	private void pushSuccessors(Node b) {
		for (Node successor : getSuccessors(b)) {
			if (visitedEdges.add(new Edge(b, successor))) {
				stack.push(successor);
			}
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Edge is used to detect edges previously traversed. It implements composite
	 * hash and equality operations so it can be used as a key in a hashed
	 * collection.
	 */
	private static class Edge {
		private String fromAddress;
		private String toAddress;

		public Edge(Node from, Node to) {
			this.fromAddress = from.address().toAddressString();
			this.toAddress = to.address().toAddressString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fromAddress == null) ? 0 : fromAddress.hashCode());
			result = prime * result + ((toAddress == null) ? 0 : toAddress.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Edge other = (Edge) obj;
			if (fromAddress == null) {
				if (other.fromAddress != null)
					return false;
			} else if (!fromAddress.equals(other.fromAddress))
				return false;
			if (toAddress == null) {
				if (other.toAddress != null)
					return false;
			} else if (!toAddress.equals(other.toAddress))
				return false;
			return true;
		}
	}
	
	protected AtlasSet<Node> getSuccessors(Node node) {
		return this.graph.getSuccessors(node);
	}
}