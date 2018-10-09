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
import com.se421.slice.log.Log;

public class DataDependenceGraph extends DependenceGraph {

	/**
	 * Used to tag the edges between nodes that contain a data dependence
	 */
	public static final String DATA_DEPENDENCE_EDGE = "data-dependence";
	
	private Graph dfg; // data flow graph (SSA form)
	private Graph ddg; // data dependency graph
	
	public DataDependenceGraph(Graph dfg){
		// sanity checks
		if(dfg.nodes().isEmpty() || dfg.edges().isEmpty()){
			this.dfg = Common.toQ(dfg).eval();
			this.ddg = Common.empty().eval();
			return;
		}
		
		this.dfg = dfg;
		
		// for each data flow edge summarize its data dependence relationship at the statement level
		AtlasSet<Edge> dataDependenceEdgeSet = new AtlasHashSet<Edge>();
		for(Edge dfEdge : dfg.edges()){
			Node from = dfEdge.from();
			Node fromStatement = from;
			if(!fromStatement.taggedWith(XCSG.Identity) && !fromStatement.taggedWith(XCSG.Parameter)){
				fromStatement = getStatement(from);
			}
			
			Node to = dfEdge.to();
			Node toStatement = to;
			if(!toStatement.taggedWith(XCSG.ReturnValue)){
				toStatement = getStatement(to);
			}
			
			// sanity checks
			if(fromStatement == null){
				Log.warning("From node has no parent or is null: " + from.address().toAddressString());
				continue;
			}
			if(toStatement == null){
				Log.warning("To node has no parent or is null: " + to.address().toAddressString());
				continue;
			}
			
			// TODO: implement
			// If the fromStatement is not the toStatement (all statements are trivially dependent on themselves) 
			// then find or create a data dependence edge from the fromStatement to the toStatement. Add the 
			// resulting edge to the set of data dependence edges.
			//
			// Note: node equality can be checked with node1.equals(node2)
			// Note: a findOrCreateDataDependenceEdge(fromStatement, toStatement) has been implemented for you
			// Note: the dataDependenceEdgeSet is used to track newly added edges so remember to add edges you create there
		}
		
		///////////// Some Additional Edge Cases Are Handled For You Below ///////////// 
		
		// consider data dependencies on parameters, returns, and fields
		Q interproceduralDataFlowEdges = Query.universe().edges(XCSG.InterproceduralDataFlow);
		Q fields = Query.universe().nodes(XCSG.Field, XCSG.ArrayComponents);
		Q parameters = Query.universe().nodes(XCSG.Parameter);
		Q returns = Query.universe().nodes(XCSG.Return);
		Q localDFG = Common.toQ(dfg).difference(parameters, returns);
		for(Node field : interproceduralDataFlowEdges.between(fields, localDFG).nodes(XCSG.Field).eval().nodes()){
			for(Node localDFNode : interproceduralDataFlowEdges.forward(Common.toQ(field)).intersection(localDFG).eval().nodes()){
				Node toStatement = localDFNode;
				if(!toStatement.taggedWith(XCSG.ReturnValue)){
					toStatement = getStatement(localDFNode);
				}
				Node fromStatement = field;
				
				if(fromStatement == null || toStatement == null || fromStatement.equals(toStatement)){
					continue;
				}
				
				Q dataDependenceEdges = Query.universe().edges(DATA_DEPENDENCE_EDGE);
				Edge dataDependenceEdge = dataDependenceEdges.betweenStep(Common.toQ(fromStatement), Common.toQ(toStatement)).eval().edges().one();
				if(dataDependenceEdge == null){
					dataDependenceEdge = Graph.U.createEdge(fromStatement, toStatement);
					dataDependenceEdge.tag(DATA_DEPENDENCE_EDGE);
					dataDependenceEdge.putAttr(XCSG.name, DATA_DEPENDENCE_EDGE);
				}
				dataDependenceEdgeSet.add(dataDependenceEdge);
			}
		}
		
		// add data dependencies for array references
		Q arrayIdentityForEdges = Query.universe().edges(XCSG.ArrayIdentityFor);
		for(Node arrayRead : Common.toQ(dfg).nodes(XCSG.ArrayRead).eval().nodes()){
			for(Node arrayIdentityFor : arrayIdentityForEdges.predecessors(Common.toQ(arrayRead)).eval().nodes()){
				Node fromStatement = arrayIdentityFor;
				if(!fromStatement.taggedWith(XCSG.Parameter) && !fromStatement.taggedWith(XCSG.Field)){
					fromStatement = getStatement(arrayIdentityFor);
				}
				
				Node toStatement = getStatement(arrayRead);
				
				if(fromStatement == null || toStatement == null || fromStatement.equals(toStatement)){
					continue;
				}
				
				Q dataDependenceEdges = Query.universe().edges(DATA_DEPENDENCE_EDGE);
				Edge dataDependenceEdge = dataDependenceEdges.betweenStep(Common.toQ(fromStatement), Common.toQ(toStatement)).eval().edges().one();
				if(dataDependenceEdge == null){
					dataDependenceEdge = Graph.U.createEdge(fromStatement, toStatement);
					dataDependenceEdge.tag(DATA_DEPENDENCE_EDGE);
					dataDependenceEdge.putAttr(XCSG.name, DATA_DEPENDENCE_EDGE);
				}
				dataDependenceEdgeSet.add(dataDependenceEdge);
			}
		}
		
		// add data dependencies for array indexes
		Q arrayIndexForEdges = Query.universe().edges(XCSG.ArrayIndexFor);
		for(Node arrayRead : Common.toQ(dfg).nodes(XCSG.ArrayRead).eval().nodes()){
			for(Node arrayIndexFor : arrayIndexForEdges.predecessors(Common.toQ(arrayRead)).eval().nodes()){
				Node fromStatement = arrayIndexFor;
				if(!fromStatement.taggedWith(XCSG.Parameter) && !fromStatement.taggedWith(XCSG.Field)){
					fromStatement = getStatement(arrayIndexFor);
				}
				
				Node toStatement = getStatement(arrayRead);
				
				if(fromStatement == null || toStatement == null || fromStatement.equals(toStatement)){
					continue;
				}
				
				Q dataDependenceEdges = Query.universe().edges(DATA_DEPENDENCE_EDGE);
				Edge dataDependenceEdge = dataDependenceEdges.betweenStep(Common.toQ(fromStatement), Common.toQ(toStatement)).eval().edges().one();
				if(dataDependenceEdge == null){
					dataDependenceEdge = Graph.U.createEdge(fromStatement, toStatement);
					dataDependenceEdge.tag(DATA_DEPENDENCE_EDGE);
					dataDependenceEdge.putAttr(XCSG.name, DATA_DEPENDENCE_EDGE);
				}
				dataDependenceEdgeSet.add(dataDependenceEdge);
			}
		}
		
		this.ddg = Common.toQ(dataDependenceEdgeSet).eval();
	}

	/**
	 * Finds or creates a data dependence edge
	 * @param fromStatement
	 * @param toStatement
	 * @return
	 */
	private Edge findOrCreateDataDependenceEdge(Node fromStatement, Node toStatement) {
		Q dataDependenceEdges = Query.universe().edges(DATA_DEPENDENCE_EDGE);
		Edge dataDependenceEdge = dataDependenceEdges.betweenStep(Common.toQ(fromStatement), Common.toQ(toStatement)).eval().edges().one();
		if(dataDependenceEdge == null){
			dataDependenceEdge = Graph.U.createEdge(fromStatement, toStatement);
			dataDependenceEdge.tag(DATA_DEPENDENCE_EDGE);
			dataDependenceEdge.putAttr(XCSG.name, DATA_DEPENDENCE_EDGE);
		}
		return dataDependenceEdge;
	}

	@Override
	public Q getGraph() {
		return Common.toQ(ddg);
	}
	
	/**
	 * Returns the underlying data flow graph
	 * @return
	 */
	public Q getDataFlowGraph(){
		return Common.toQ(dfg);
	}
	
}
