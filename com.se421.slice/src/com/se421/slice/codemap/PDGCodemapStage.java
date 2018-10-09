package com.se421.slice.codemap;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.indexing.providers.ToolboxIndexingStage;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.se421.slice.analysis.DependenceGraph;
import com.se421.slice.log.Log;

/**
 * Builds the PDGs for each function
 * 
 * @author Ben Holland
 */
public class PDGCodemapStage implements ToolboxIndexingStage {

	@Override
	public String displayName() {
		return "Program Dependence Graph";
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		// compute the dominance analysis relationships
		Log.info(DominanceAnalysis.displayName() + "...");
		DominanceAnalysis.performIndexing(monitor);
		
		// compute the program dependence graph
		Log.info("Computing Program Dependence Graphs...");
		for(Node function : Query.universe().nodes(XCSG.Function).eval().nodes()){
			DependenceGraph.Factory.buildPDG(function);
		}
	}

}