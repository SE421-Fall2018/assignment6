package com.se421.slice.analysis.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.EdgeGraph;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.NodeGraph;
import com.ensoftcorp.atlas.core.db.graph.UncheckedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasEdgeHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasNodeHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

/**
 * Common queries which are useful for writing larger language agnostic analysis
 * programs, and for using on the shell. This also acts as a wrapper around
 * relevant <code>com.ensoftcorp.atlas.core.script.CommonQueries</code> API functions.
 * 
 * @author Ben Holland, Tom Deering, Jon Mathews
 */
public final class CommonQueries {	
	
	// hide constructor
	private CommonQueries() {}
	
	// begin wrapper queries
	
	/**
	 * Returns all references to class literals (Type.class) for the given
	 * types. 
	 * 
	 * Equivalent to classLiterals(universe(), types).
	 * 
	 * @param types
	 * @return the query expression
	 */
	public static Q classLiterals(Q types){
		return com.ensoftcorp.atlas.core.script.CommonQueries.classLiterals(types);
	}
	
	/**
	 * Returns all references to class literals (Type.class) for the given
	 * types.
	 * 
	 * @param types
	 * @return the query expression
	 */
	public static Q classLiterals(Q context, Q types){
		return com.ensoftcorp.atlas.core.script.CommonQueries.classLiterals(context, types);
	}
	
	/**
	 * Produces a declarations (contains) graph. 
	 * 
	 * Equivalent to declarations(universe(), origin).
	 * 
	 * @param origin
	 * @return the query expression
	 */
	public static Q declarations(Q origin){
		return com.ensoftcorp.atlas.core.script.CommonQueries.declarations(origin);
	}
	
	
	/**
	 * Produces a declarations (contains) graph. Uses only the given context for
	 * the traversal.
	 * 
	 * @param context
	 * @param origin
	 * @return the query expression
	 */
	public static Q declarations(Q context, Q origin){
		return com.ensoftcorp.atlas.core.script.CommonQueries.declarations(context, origin);
	}
	
	/**
	 * Returns direct edges of the given kinds which lay immediately between the
	 * first group and second group of nodes.
	 * 
	 * @param first
	 * @param second
	 * @param edgeTags
	 * @return the query expression
	 */
	public static Q interactions(Q first, Q second, String... edgeTags){
		return interactions(Query.codemap(), first, second, edgeTags);
	}
	
	/**
	 * Returns direct edges of the given kinds which lay immediately between the
	 * first group and second group of nodes.
	 * 
	 * @param context
	 * @param first
	 * @param second
	 * @param edgeTags
	 * @return the query expression
	 */
	public static Q interactions(Q context, Q first, Q second, String... edgeTags) {
		List<Q> results = interactions2(context, first, second, edgeTags);
		AtlasSet<Node> nodes = new AtlasNodeHashSet();
		AtlasSet<Edge> edges = new AtlasEdgeHashSet();
				
		for (Q q : results) {
			nodes.addAll(q.eval().nodes());
			edges.addAll(q.eval().edges());
		}
		
		Graph g = new UncheckedGraph(nodes, edges);
		return Query.toQ(g);
	}
	
	/**
	 * For each Node in second, return a Q containing all
	 * Edges immediately incident on Nodes in first.
	 * Omit results which are empty.
	 * 
	 * @param context result is a subset of the context
	 * @param first
	 * @param second
	 * @param tags edges with at least one of the given tags
	 * @return list of one Q per Node in second which has some incidence
	 * to Nodes in first
	 */
	public static List<Q> interactions2(Q context, Q first, Q second, String ... tags) {
		List<Q> results = new ArrayList<>();
		AtlasSet<Node> contextNodes = context.eval().nodes();
		AtlasSet<Edge> contextEdges = context.eval().edges();
		AtlasSet<Node> firstNodes = Query.resolve(null, first.eval().nodes());
		for (Node n : second.eval().nodes()) {
			if (!contextNodes.contains(n))
				continue;
			AtlasSet<Edge> edges = new AtlasEdgeHashSet();
			for (String tag : tags) {
				for (Edge e : n.in(tag)) {
					if (!contextEdges.contains(e))
						continue;
					if (firstNodes.contains(e.from()) 
							&& contextNodes.contains(e.from())) {
						edges.add(e);
					}
				}
				for (Edge e : n.out(tag)) {
					if (!contextEdges.contains(e))
						continue;
					if (firstNodes.contains(e.to()) 
							&& contextNodes.contains(e.to())) {
						edges.add(e);
					}
				}
			}
			if (!edges.isEmpty()) {
				Graph g = new EdgeGraph(edges);
				Q q = Query.toQ(g);
				results.add(q);
			}
		}
		return results;
	}
	
	/**
	 * Returns those nodes which are declared by a library.
	 * 
	 * @return the query expression
	 */
	public static Q libraryDeclarations(){
		return com.ensoftcorp.atlas.core.script.CommonQueries.libraryDeclarations(); 
	}
	
	/**
	 * Returns those nodes which are declared by a library. Results are only
	 * returned if they are within the given context.
	 * 
	 * Equivalent to libraryDeclarations(universe())
	 * 
	 * @param context
	 * @return the query expression
	 */
	public static Q libraryDeclarations(Q context){
		return com.ensoftcorp.atlas.core.script.CommonQueries.libraryDeclarations(context); 
	}
	
	/**
	 * Returns those nodes which are declared by a library with the given name.
	 * 
	 * @param name
	 * @return the query expression
	 */
	public static Q libraryDeclarations(String name){
		return com.ensoftcorp.atlas.core.script.CommonQueries.libraryDeclarations(name); 
	}
	
	/**
	 * Returns those nodes which are declared by a library with the given name.
	 * Results are only returned if they are within the given context.
	 * 
	 * Equivalent to libraryDeclarations(universe(), name)
	 * 
	 * @param context
	 * @param name
	 * @return the query expression
	 */
	public static Q libraryDeclarations(Q context, String name){
		return com.ensoftcorp.atlas.core.script.CommonQueries.libraryDeclarations(context, name); 
	}
	
	/**
	 * Returns the nodes whose names contain the given string.
	 * 
	 * Equivalent to nodesContaining(universe(), substring).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesContaining(String substring){
		return nodesMatchingRegex(Query.universe(), ".*" + Pattern.quote(substring) + ".*");
	}
	
	/**
	 * Returns the nodes whose names contain the given string within the given
	 * context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesContaining(Q context, String substring){
		return nodesMatchingRegex(context, ".*" + Pattern.quote(substring) + ".*");
	}
	
	/**
	 * Returns the nodes whose names end with the given string.
	 * 
	 * Equivalent to nodesEndingWith(universe(), suffix).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesEndingWith(String suffix){
		return nodesMatchingRegex(Query.universe(), ".*" + Pattern.quote(suffix));
	}
	
	/**
	 * Returns the nodes whose names end with the given string within the given
	 * context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesEndingWith(Q context, String suffix){
		return nodesMatchingRegex(context, ".*" + Pattern.quote(suffix));
	}
	
	/**
	 * Returns the nodes whose names match the given regular expression.
	 * 
	 * Equivalent to nodesMatchingRegex(universe(), regex).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesMatchingRegex(String regex){
		return nodesMatchingRegex(Query.universe(), regex);
	}
	
	/**
	 * Returns the nodes whose names start with the given string.
	 * 
	 * Equivalent to nodesStartingWith(universe(), prefix).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesStartingWith(String prefix){
		return nodesMatchingRegex(Query.universe(), Pattern.quote(prefix) + ".*");
	}
	
	/**
	 * Returns the nodes whose names start with the given string within the
	 * given context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesStartingWith(Q context, String prefix){
		return nodesMatchingRegex(context, Pattern.quote(prefix) + ".*");
	}
	
	/**
	 * Returns the nodes whose names match the given regular expression within
	 * the given context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesMatchingRegex(Q context, String regex){
		return nodesAttributeValuesMatchingRegex(context, XCSG.name, regex);
	}
	
	/**
	 * Returns the nodes whose attribute values contain the given string.
	 * 
	 * Equivalent to nodesAttributeValuesContaining(universe(), attribute, substring).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesAttributeValuesContaining(String attribute, String substring){
		return nodesAttributeValuesMatchingRegex(Query.universe(), attribute, ".*" + Pattern.quote(substring) + ".*");
	}
	
	/**
	 * Returns the nodes whose attribute values contain the given string within the given
	 * context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesAttributeValuesContaining(Q context, String attribute, String substring){
		return nodesAttributeValuesMatchingRegex(context, attribute, ".*" + Pattern.quote(substring) + ".*");
	}
	
	/**
	 * Returns the nodes whose attribute values end with the given string.
	 * 
	 * Equivalent to nodesAttributeValuesEndingWith(universe(), attribute, suffix).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesAttributeValuesEndingWith(String attribute, String suffix){
		return nodesAttributeValuesMatchingRegex(Query.universe(), attribute, ".*" + Pattern.quote(suffix));
	}
	
	/**
	 * Returns the nodes whose attribute values end with the given string within the given
	 * context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesAttributeValuesEndingWith(Q context, String attribute, String suffix){
		return nodesAttributeValuesMatchingRegex(context, attribute, ".*" + Pattern.quote(suffix));
	}
	
	/**
	 * Returns the nodes whose attribute values match the given regular expression.
	 * 
	 * Equivalent to nodesAttributeValuesMatchingRegex(universe(), attribute, regex).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesAttributeValuesMatchingRegex(String attribute, String regex){
		return nodesAttributeValuesMatchingRegex(Query.universe(), attribute, regex);
	}
	
	/**
	 * Returns the nodes whose attribute values start with the given string.
	 * 
	 * Equivalent to nodesAttributeValuesStartingWith(universe(), attribute, prefix).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesAttributeValuesStartingWith(String attribute, String prefix){
		return nodesAttributeValuesMatchingRegex(Query.universe(), attribute, Pattern.quote(prefix) + ".*");
	}
	
	/**
	 * Returns the nodes whose attribute values start with the given string within the
	 * given context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesAttributeValuesStartingWith(Q context, String attribute, String prefix){
		return nodesAttributeValuesMatchingRegex(context, attribute, Pattern.quote(prefix) + ".*");
	}
	
	/**
	 * Returns the nodes whose attribute values match the given regular
	 * expression within the given context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesAttributeValuesMatchingRegex(Q context, String attribute, String regex){
		AtlasSet<Node> result = new AtlasHashSet<Node>();
		Iterator<Node> iterator = context.eval().nodes().iterator();
		while (iterator.hasNext()) {
			Node node = iterator.next();
			Object value = node.getAttr(attribute);
			if(value != null){
				if(value instanceof String){
					String name = (String) value;
					if (name.matches(regex)) {
						result.add(node);
					}
				}
			}
		}
		return Common.toQ(new NodeGraph(result));
	}
	
	/**
	 * Returns the number of edges contained.
	 * @param toCount
	 * @return
	 */
	public static long edgeSize(Q toCount){
		return com.ensoftcorp.atlas.core.script.CommonQueries.edgeSize(toCount);
	}
	
	/**
	 * Returns the number of nodes contained.
	 * @param toCount
	 * @return
	 */
	public static long nodeSize(Q toCount){
		return com.ensoftcorp.atlas.core.script.CommonQueries.nodeSize(toCount);
	}
	
	/**
	 * Returns whether the given Q is empty.
	 * 
	 * @param test
	 * @return
	 */
	public static boolean isEmpty(Q test){
		return !isNotEmpty(test);
	}
	
	/**
	 * Returns whether the given Q is not empty
	 * @param test
	 * @return
	 */
	public static boolean isNotEmpty(Q test) {
		return test.eval().nodes().iterator().hasNext();
	}
	
	// begin toolbox commons queries
	
	/**
	 * Returns the set of functions where their names matches the any of the
	 * names given (functionNames) list. A (*) in (functionNames) represents a
	 * wildcard that matches any string.
	 * 
	 * @param functionNames:
	 *            A list of function names as Strings
	 * @return A set of functions
	 */
	public static Q functions(String... functionNames){
		return find(XCSG.Function, functionNames);
	}

	/**
	 * Returns the nodes representing the global variable(s) given by the
	 * parameter list (names). A (*) in any string in the list (names)
	 * represents a wildcard that matches any string.
	 * 
	 * @param names:
	 *            A list of global variable names as Strings
	 * @return A set of global variable nodes
	 */
	public static Q globals(String... names){
		return find(XCSG.GlobalVariable, names);
	}

	/**
	 * Returns the nodes representing the types given by the names. 
	 * A (*) is a wildcard that matches any string.
	 * @param names A list of type names
	 * @return A set of global variable nodes
	 */
	public static Q types(String... names){
		return find(XCSG.Type, names);
	}
	
	/**
	 * Selects the Atlas graph element given a serialized graph
	 * element address
	 * 
	 * Returns null if the address does not correspond to a graph element
	 * 
	 * @param address
	 * @return
	 */
	public static GraphElement getGraphElementByAddress(String address){
		int hexAddress = Integer.parseInt(address, 16);
		GraphElement ge = Graph.U.getAt(hexAddress);
		return ge;
	}
	
	/**
	 * Selects the Atlas node graph element given a serialized graph
	 * element address
	 * 
	 * Returns null if the address does not correspond to a node
	 * 
	 * @param address
	 * @return
	 */
	public static Node getNodeByAddress(String address){
		GraphElement ge = getGraphElementByAddress(address);
		if(ge != null && ge instanceof Node){
			return (Node) ge;
		}
		return null;
	}
	
	/**
	 * Selects the Atlas edge graph element given a serialized graph
	 * element address
	 * 
	 * Returns null if the address does not correspond to a edge
	 * 
	 * @param address
	 * @return
	 */
	public static Edge getEdgeByAddress(String address){
		GraphElement ge = getGraphElementByAddress(address);
		if(ge != null && ge instanceof Edge){
			return (Edge) ge;
		}
		return null;
	}
	
	/**
	 * Returns the parameters of the given functions. 
	 * 
	 * Equivalent to functionParameter(universe(), functions)
	 * 
	 * @param functions
	 * @return the query expression
	 */
	public static Q functionParameter(Q functions){
		return functionParameter(Query.universe(), functions);
	}
	
	/**
	 * Returns the parameters of the given functions at the given indices. 
	 * 
	 * Equivalent to functionParameter(universe(), functions, index)
	 * 
	 * @param functions
	 * @param index
	 * @return the query expression
	 */
	public static Q functionParameter(Q functions, Integer... index){
		return functionParameter(Query.universe(), functions, index);
	}
	
	/**
	 * Returns the parameters of the given functions. Results are only returned if
	 * they are within the given context.
	 * 
	 * @param context
	 * @param functions
	 * @return the query expression
	 */
	public static Q functionParameter(Q context, Q functions){
		return functions.children().intersection(context).nodes(XCSG.Parameter);
	}
	
	/**
	 * Returns the parameters of the given functions at the given indices. Results
	 * are only returned if they are within the given context.
	 * 
	 * @param context
	 * @param functions
	 * @param index
	 * @return the query expression
	 */
	public static Q functionParameter(Q context, Q functions, Integer... index){
		return functionParameter(context, functions).selectNode(XCSG.parameterIndex, (Object[]) index);
	}
	
	/**
	 * Returns the return nodes for the given functions.
	 * 
	 * Equivalent to functionReturn(universe(), functions).
	 * 
	 * @param functions
	 * @return the query expression
	 */
	public static Q functionReturn(Q functions){
		return functionReturn(Query.universe(), functions);
	}
	
	/**
	 * Returns the return nodes for the given functions.
	 * @param context
	 * @param functions
	 * @return the query expression
	 */
	public static Q functionReturn(Q context, Q functions){
		return context.edges(XCSG.Contains).successors(functions).nodes(XCSG.ReturnValue);
	}
	
	/**
	 * Returns the functions declared by the given types. 
	 * 
	 * Equivalent to functionsOf(universe(), types).
	 * 
	 * @param params
	 * @return the query expression
	 */
	public static Q functionsOf(Q types){
		return functionsOf(Query.universe(), types);
	}
	
	/**
	 * Returns the functions declared by the given types.
	 * 
	 * @param context
	 * @param types
	 * @return the query expression
	 */
	public static Q functionsOf(Q context, Q types){
		return types.nodes(XCSG.Type).children().intersection(context).nodes(XCSG.Function);
	}
	
	/**
	 * 
	 * @param functions
	 * @return the data flow graph under the function
	 */
	public static Q dfg(Q functions) {
		return localDeclarations(functions).nodes(XCSG.DataFlow_Node).induce(Common.edges(XCSG.DataFlow_Edge));
	}
	
	/**
	 * 
	 * @param function
	 * @return the data flow graph under the function
	 */
	public static Q dfg(Node function) {
		return dfg(Common.toQ(function));
	}
	
	/**
	 * 
	 * @param functions
	 * @return the control flow graph under the function
	 */
	public static Q cfg(Q functions) {
		return com.ensoftcorp.atlas.core.script.CommonQueries.cfg(functions);
	}
	
	/**
	 * 
	 * @param function
	 * @return the control flow graph under the function
	 */
	public static Q cfg(Node function) {
		return cfg(Common.toQ(function));
	}
	
	/**
	 * 
	 * @param functions
	 * @return the control flow graph (including exceptional control flow) under the function
	 */
	public static Q excfg(Q functions) {
		return com.ensoftcorp.atlas.core.script.CommonQueries.excfg(functions);
	}
	
	/**
	 * 
	 * @param function
	 * @return the control flow graph (including exceptional control flow) under the function
	 */
	public static Q excfg(Node function) {
		return excfg(Common.toQ(function));
	}
	
	public static boolean isCallSite(Q cfNode) {
		Q callsites = getContainingCallSites(cfNode);
		if(isEmpty(callsites)) {
			return false;
		}
		return true;
	}
	
	public static Q getContainingCallSites(Q cfNode) {
		return cfNode.children().nodes(XCSG.CallSite);
	}
	
	/**
	 * All nodes declared under the given functions, but NOT declared under
	 * additional functions or types. Retrieves declarations of only this function.
	 * Results are only returned if they are within the given context.
	 * 
	 * @param functions
	 * @return
	 */
	public static Q localDeclarations(Q functions) {
		return localDeclarations(Query.universe(), functions);
	}

	/**
	 * All nodes declared under the given functions, but NOT declared under
	 * additional functions or types. Retrieves declarations of only this function.
	 * Results are only returned if they are within the given context.
	 * 
	 * @param context
	 * @param functions
	 * @return
	 */
	public static Q localDeclarations(Q context, Q functions) {
		AtlasSet<Node> result = new AtlasHashSet<Node>();
		AtlasSet<Node> worklist = new AtlasHashSet<Node>(functions.children().eval().nodes());
		while(!worklist.isEmpty()){
			Iterator<Node> iter = worklist.iterator();
			Node child = iter.next();
			iter.remove();
			if(child.taggedWith(XCSG.Type) || child.taggedWith(XCSG.Function)){
				continue;
			} else {
				result.add(child);
				worklist.addAll(Common.toQ(child).children().eval().nodes());
			}
		}
		return Common.toQ(result);
	}

	/**
	 * Returns the direct callers of the given functions.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q callers(Q origin) {
		return callers(Query.universe(), origin);
	}

	/**
	 * Returns the direct callers of the given functions.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q callers(Q context, Q origin) {
		return context.edges(XCSG.Call).predecessors(origin);
	}

	/**
	 * Returns the subset of the given functions which are called.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q called(Q origin) {
		return called(Query.universe(), origin);
	}

	/**
	 * Returns the subset of the given functions which are called. Results are
	 * only returned if they are within the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q called(Q context, Q origin) {
		return context.edges(XCSG.Call).successors(origin);
	}

	/**
	 * Returns the given functions which were called by the given callers.
	 * 
	 * Operates in the index context.
	 * 
	 * @param callers
	 * @param called
	 * @return
	 */
	public static Q calledBy(Q callers, Q called) {
		return calledBy(Query.universe(), callers, called);
	}

	/**
	 * Returns the given functions which were called by the given callers. Results
	 * are only returned if they are within the given context.
	 * 
	 * @param context
	 * @param callers
	 * @param called
	 * @return
	 */
	public static Q calledBy(Q context, Q callers, Q called) {
		return context.edges(XCSG.Call).betweenStep(callers, called).retainEdges().leaves();
	}

	/**
	 * Returns the first declaring node of the given Q which is tagged with one
	 * of the given types.
	 * 
	 * Operates in the index context.
	 * 
	 * @param declared
	 * @param declaratorTypes
	 * @return
	 */
	public static Q firstDeclarator(Q declared, String... declaratorTypes) {
		return firstDeclarator(Query.universe(), declared, declaratorTypes);
	}

	/**
	 * Returns the first declaring node of the given Q which is tagged with one
	 * of the given types. Results are only returned if they are within the
	 * given context.
	 * 
	 * @param context
	 * @param declared
	 * @param declaratorTypes
	 * @return
	 */
	public static Q firstDeclarator(Q context, Q declared, String... declaratorTypes) {
		Q subContext = declared.containers().intersection(context);
		subContext = subContext.differenceEdges(subContext.reverseStep(subContext.nodes(declaratorTypes)));
		return subContext.reverse(declared).nodes(declaratorTypes);
	}

	/**
	 * Given two query expressions, intersects the given node and edge kinds to
	 * produce a new expression.
	 * 
	 * @param first
	 * @param second
	 * @param nodeTags
	 * @param edgeTags
	 * @return
	 */
	public static Q advancedIntersection(Q first, Q second, String[] nodeTags, String[] edgeTags) {
		Q plainIntersection = first.intersection(second);
		return plainIntersection.nodes(nodeTags).induce(plainIntersection.edges(edgeTags));
	}

	/**
	 * Returns the nodes which directly read from nodes in origin.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q readersOf(Q origin) {
		return readersOf(Query.universe(), origin);
	}

	/**
	 * Returns the nodes which directly read from nodes in origin.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q readersOf(Q context, Q origin) {
		return context.edges(XCSG.DataFlow_Edge).successors(origin);
	}

	/**
	 * Returns the nodes which directly write to nodes in origin.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q writersOf(Q origin) {
		return writersOf(Query.universe(), origin);
	}

	/**
	 * Returns the nodes which directly write to nodes in origin.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q writersOf(Q context, Q origin) {
		return context.edges(XCSG.DataFlow_Edge).predecessors(origin);
	}

	/**
	 * Returns the nodes from which nodes in the origin read.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q readBy(Q origin) {
		return readBy(Query.universe(), origin);
	}

	/**
	 * Returns the nodes from which nodes in the origin read.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q readBy(Q context, Q origin) {
		return writersOf(context, origin);
	}

	/**
	 * Returns the nodes to which nodes in origin write.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q writtenBy(Q origin) {
		return writtenBy(Query.universe(), origin);
	}

	/**
	 * Returns the nodes to which nodes in origin write.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q writtenBy(Q context, Q origin) {
		return readersOf(context, origin);
	}
	
	/**
	 * Returns points where there are no competing branch points
	 * @param roots
	 * @param leaves
	 * @param graph
	 * @return
	 */
	public static AtlasSet<Node> syncPoints(AtlasSet<Node> roots, AtlasSet<Node> leaves, Graph graph){
		Q lcc = Common.toQ(leastCommonChildren(roots, graph));
		Q lccParents = Common.toQ(graph).predecessors(lcc);
		Q unsyncedRoots = Common.toQ(graph).reverse(lccParents);
		Q lca = Common.toQ(leastCommonAncestors(leaves, graph));
		Q lcaChildren = Common.toQ(graph).successors(lca);
		Q unsyncedLeaves = Common.toQ(graph).forward(lcaChildren);
		graph = Common.toQ(graph).difference(unsyncedRoots, unsyncedLeaves).eval();
		AtlasSet<Node> syncPoints = new AtlasHashSet<Node>();
		AtlasSet<Node> cutPoints = Common.toQ(graph).leaves().eval().nodes();
		boolean fixedPoint = false;
		while(!fixedPoint) {
			cutPoints = leastCommonAncestors(cutPoints, graph);
			fixedPoint = syncPoints.addAll(cutPoints);
		}
		return syncPoints;
	}
	
	/**
	 * Returns the least common children of of the given parents within the given graph
	 * @param child1
	 * @param child2
	 * @param graph
	 * @return
	 */
	public static AtlasSet<Node> leastCommonAncestors(AtlasSet<Node> children, Graph graph){
		Graph intersectingParents = graph;
		for(Node child : children) {
			intersectingParents = Common.toQ(graph).reverse(Common.toQ(child)).intersection(Common.toQ(intersectingParents)).eval();
		}
		return Common.toQ(intersectingParents).leaves().eval().nodes();
	}
	
	/**
	 * Returns the least common children of of the given parents within the given graph
	 * @param child1
	 * @param child2
	 * @param graph
	 * @return
	 */
	public static AtlasSet<Node> leastCommonChildren(AtlasSet<Node> parents, Graph graph){
		Graph intersectingChildren = graph;
		for(Node parent : parents) {
			intersectingChildren = Common.toQ(graph).forward(Common.toQ(parent)).intersection(Common.toQ(intersectingChildren)).eval();
		}
		return Common.toQ(intersectingChildren).roots().eval().nodes();
	}
	
	/**
	 * Returns the least common child of both parent1 and parent2 within the given graph
	 * @param child1
	 * @param child2
	 * @param graph
	 * @return
	 */
	public static Node leastCommonChild(Node parent1, Node parent2, Graph graph){
		return leastCommonAncestor(parent1, parent2, Common.toQ(graph));
	}
	
	/**
	 * Returns the least common ancestor of both parent1 and parent2 within the given graph
	 * @param parent1
	 * @param parent2
	 * @param graph
	 * @return
	 */
	public static Node leastCommonChild(Node parent1, Node parent2, Q graph){
		Q children = graph.reverse(Common.toQ(parent1)).intersection(graph.reverse(Common.toQ(parent2)));
		return children.roots().eval().nodes().one();
	}
	
	/**
	 * Returns the least common ancestor of both child1 and child2 within the given graph
	 * @param child1
	 * @param child2
	 * @param graph
	 * @return
	 */
	public static Node leastCommonAncestor(Node child1, Node child2, Graph graph){
		return leastCommonAncestor(child1, child2, Common.toQ(graph));
	}
	
	/**
	 * Returns the least common ancestor of both child1 and child2 within the given graph
	 * @param child1
	 * @param child2
	 * @param graph
	 * @return
	 */
	public static Node leastCommonAncestor(Node child1, Node child2, Q graph){
		Q ancestors = graph.reverse(Common.toQ(child1)).intersection(graph.reverse(Common.toQ(child2)));
		return ancestors.leaves().eval().nodes().one();
	}

	/**
	 * Returns the containing function of a given Q or empty if one is not found
	 * @param nodes
	 * @return
	 */
	public static Q getContainingFunctions(Q nodes) {
		AtlasSet<Node> nodeSet = new AtlasHashSet<Node>(nodes.eval().nodes());
		AtlasSet<Node> containingFunctions = new AtlasHashSet<Node>();
		for (Node currentNode : nodeSet) {
			Node function = getContainingFunction(currentNode);
			if (function != null){
				containingFunctions.add(function);
			}
		}
		return Common.toQ(Common.toGraph(containingFunctions));
	}
	
	/**
	 * Returns the nearest parent that is a control flow node
	 * @param node
	 * @return
	 */
	public static Node getContainingControlFlowNode(Node node) {
		// NOTE: this logic considers that the enclosing control flow node may be two steps or more above
		return getContainingNode(node, XCSG.ControlFlow_Node);
	}

	/**
	 * Returns the containing function of a given graph element or null if one is not found
	 * @param node
	 * @return
	 */
	public static Node getContainingFunction(Node node) {
		// NOTE: this logic considers that the enclosing function may be two steps or more above
		return getContainingNode(node, XCSG.Function);
	}
	
	/**
	 * Find the next immediate containing node with the given tag.
	 * 
	 * @param node 
	 * @param containingTag
	 * @return the next immediate containing node, or null if none exists; never returns the given node
	 */
	public static Node getContainingNode(Node node, String containingTag) {
		if (node == null) {
			return null;
		}
		while (true) {
			Edge containsEdge = node.in(XCSG.Contains).one();
			if (containsEdge == null) {
				return null;
			}
			Node parent = containsEdge.from();
			if (parent.taggedWith(containingTag)) {
				return parent;
			}
			node = parent;
		}
	}
	
	/**
	 * Given a function, a branch, and an event of interest returns true if the
	 * branch governs whether or not the event of interest could be executed. If
	 * true the branch could prevent the event from being executed. The branch
	 * and event must both be contained in the same function. This method does 
	 * not consider exceptional control flow paths.
	 * 
	 * @param function
	 * @param branch
	 * @param event
	 * @return
	 */
	public static boolean isGoverningBranch(Node branch, Node event){
		return isGoverningBranch(branch, event, true);
	}
	
	/**
	 * Given a function, a branch, and an event of interest returns true if the
	 * branch governs whether or not the event of interest could be executed. If
	 * true the branch could prevent the event from being executed. The branch
	 * and event must both be contained in the same function.
	 * @param branch
	 *            An XCSG.ControlFlowCondition node
	 * @param event
	 *            An ControlFlow_Node node
	 * @param includeExceptionalPaths
	 *            If true considers exceptional control flow paths
	 * @return
	 */
	public static boolean isGoverningBranch(Node branch, Node event, boolean includeExceptionalPaths){
		Node branchFunction = getContainingFunction(branch);
		Node eventFunction = getContainingFunction(event);
		if(!branchFunction.equals(eventFunction)){
			throw new IllegalArgumentException("Branch and event must be contained in the same function.");
		}
		Node function = branchFunction;
		if(!branch.taggedWith(XCSG.ControlFlowCondition)){
			throw new IllegalArgumentException("branch parameter is not a control flow condition!");
		}
		if(!event.taggedWith(XCSG.ControlFlow_Node)){
			throw new IllegalArgumentException("event parameter is not a control flow node!");
		}
		Q cfg = includeExceptionalPaths ? excfg(function) : cfg(function);
		AtlasSet<Node> roots = cfg.roots().eval().nodes();
		if(roots.size() != 1){
			throw new RuntimeException("Function " + function.getAttr(XCSG.name) + " must only have one control flow root.");
		}
		Node root = roots.one();
		// a lovely rare corner case here, a void method can have a loop
		// with no termination conditions that forms a strongly connected
		// component, so root -> ... SCC, since the SCC will not have any
		// leaves could be empty. In order to deal with this we remove the
		// back edges to make the cfg leaves explicit
		AtlasSet<Node> exits = cfg.differenceEdges(cfg.edges(XCSG.ControlFlowBackEdge)).leaves().eval().nodes();
		if(exits.isEmpty()){
			throw new RuntimeException("Control flow graph does not have any exits.");
		}
		// is there a path from the root to the event that does not go through the branch?
		// if not then all paths must be going through the branch to reach the event and so the branch dominates the event
		boolean branchDominatesEvent = CommonQueries.isEmpty(cfg.between(Common.toQ(root), Common.toQ(event), Common.toQ(branch)));
		if(branchDominatesEvent){
			// is there a path that could be taken through at this branch where the event cannot occur?
			// i.e. this branch could potentially block the event from occurring
			boolean branchCanBlockEvent = !CommonQueries.isEmpty(cfg.between(Common.toQ(branch), Common.toQ(exits), Common.toQ(event)));
			// returns branchDominatesEvent && branchCanBlockEvent
			return branchCanBlockEvent;
		}
		return false;
	}

	/**
	 * Returns the control flow graph between conditional nodes and the given
	 * origin.
	 * 
	 * Operates within the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q conditionsAbove(Q origin) {
		return conditionsAbove(Query.universe(), origin);
	}

	/**
	 * Returns the control flow graph between conditional nodes and the given
	 * origin.
	 * 
	 * Operates within the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q conditionsAbove(Q context, Q origin) {
		Q conditionNodes = context.nodes(XCSG.ControlFlowCondition);
		return context.edges(XCSG.ControlFlow_Edge).between(conditionNodes, origin);
	}

	/**
	 * Given a Q containing functions or data flow nodes, returns a Q of things
	 * which write to or call things in the Q.
	 * 
	 * Operates within the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q mutators(Q origin) {
		return mutators(Query.universe(), origin);
	}

	/**
	 * Returns those nodes in the context which have self edges.
	 * 
	 * @param context
	 * @return
	 */
	public static Q nodesWithSelfEdges(Q context) {
		AtlasSet<GraphElement> result = new AtlasHashSet<GraphElement>();
		for (Edge edge : context.eval().edges()) {
			Node to = edge.getNode(EdgeDirection.TO);
			Node from = edge.getNode(EdgeDirection.FROM);
			if (to == from){
				result.add(to);
			}
		}
		return Common.toQ(new NodeGraph(result));
	}

	/**
	 * Given a Q containing functions or data flow nodes, returns a Q of things
	 * which write to or call things in the Q.
	 * 
	 * Operates within the index context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q mutators(Q context, Q origin) {
		return writersOf(context, origin).union(callers(context, origin));
	}

	/**
	 * Returns those elements in the origin which were called by or written by
	 * elements in the mutators set.
	 * 
	 * Operates within the index context.
	 * 
	 * @param mutators
	 * @param origin
	 * @return
	 */
	public static Q mutatedBy(Q mutators, Q origin) {
		return mutatedBy(Query.universe(), mutators, origin);
	}

	/**
	 * Returns those elements in the origin which were called by or written by
	 * elements in the mutators set.
	 * 
	 * Operates within the given context.
	 * 
	 * @param context
	 * @param mutators
	 * @param origin
	 * @return
	 */
	public static Q mutatedBy(Q context, Q mutators, Q origin) {
		return writtenBy(context, origin).union(calledBy(context, origin, mutators)).intersection(origin);
	}
	
	/**
	 * Helper function to get the stringified qualified name of the class
	 * @param type
	 * @return
	 */
	public static String getQualifiedTypeName(Node type) {
		if(type == null){
			throw new IllegalArgumentException("Type is null!");
		}
		if(!type.taggedWith(XCSG.Type)){
			throw new IllegalArgumentException("Type parameter is not a type!");
		}
		return getQualifiedName(type, XCSG.Package);
	}
	
	/**
	 * Helper function to get the stringified qualified name of the function
	 * @param function
	 * @return
	 */
	public static String getQualifiedFunctionName(Node function) {
		if(function == null){
			throw new IllegalArgumentException("Function is null!");
		}
		if(!function.taggedWith(XCSG.Function)){
			throw new IllegalArgumentException("Function parameter is not a function!");
		}
		return getQualifiedName(function, XCSG.Package);
	}
	
	/**
	 * Helper function to get the stringified qualified name of the function
	 * @param function
	 * @return
	 */
	public static String getQualifiedName(Node node) {
		return getQualifiedName(node, XCSG.Package);
	}
	
	/**
	 * Helper function to get the stringified qualified name of the class
	 * Stop after tags specify parent containers to stop qualifying at (example packages or jars)
	 * @param node
	 * @return
	 */
	public static String getQualifiedName(Node node, String...stopAfterTags) {
		if(node == null){
			throw new IllegalArgumentException("Node is null!");
		}
		String result = node.attr().get(XCSG.name).toString();
		Node parent = getDeclarativeParent(node);
		boolean qualified = false;
		while (parent != null && !qualified) {
			for(String stopAfterTag : stopAfterTags){
				if(parent.taggedWith(stopAfterTag)){
					qualified = true;
				}
			}
			String prefix = parent.attr().get(XCSG.name).toString();
			if(!prefix.equals("")){
				result = parent.attr().get(XCSG.name) + "." + result;
			}
			parent = getDeclarativeParent(parent);
		}
		return result;
	}
	
	/**
	 * Returns the single declarative parent
	 * Returns null if there is no parent
	 * Throws an IllegalArgumentException if there is more than one parent
	 * @param function
	 * @return
	 */
	private static Node getDeclarativeParent(Node node) {
		AtlasSet<Node> parentNodes = Common.toQ(node).parent().eval().nodes();
		if(parentNodes.size() > 1){
			throw new IllegalArgumentException("Multiple declarative parents!");
		}
		return parentNodes.one();
	}
	
	// begin utility functions
	
	private static Q find(String tag, String... names) {
		Q ts = Common.empty();
		for(String n : names){
			ts = ts.union(findByName(n, tag));
		}
		return ts;
	}

	private static Q findByName(String functionName, String tag) {
		if(functionName.indexOf("*") >= 0){
			Q nodes = Query.universe().nodes(tag);
			Q result = getMatches(functionName, nodes);
			return result;
		}
		// Atlas has an index over literal attribute values, so it's faster to query directly
		return Query.universe().nodes(tag).selectNode(XCSG.name, functionName);
	}
	
	/**
	 * Returns all nodes with a name matching the wildcard expression
	 * 
	 * @param name
	 * @param nodes
	 * @return
	 */
	private static Q getMatches(String name, Q nodes){
		name = name.replace("*", ".*");
		AtlasSet<Node> allNodes = nodes.eval().nodes();
		AtlasSet<GraphElement> result = new AtlasHashSet<GraphElement>();
		
		for(GraphElement node : allNodes){
			String thisName = (String) node.getAttr(XCSG.name);
			if(thisName.matches(name)){
				result.add(node);
			}
		}
		return Common.toQ(Common.toGraph(result));
	}
	
}