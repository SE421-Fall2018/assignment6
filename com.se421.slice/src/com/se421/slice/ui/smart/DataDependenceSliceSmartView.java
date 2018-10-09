package com.se421.slice.ui.smart;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.se421.slice.analysis.DependenceGraph;

public class DataDependenceSliceSmartView extends DependenceSliceSmartView {

	@Override
	public String getTitle() {
		return "Data Dependence Slice (DDG)";
	}

	@Override
	protected DependenceGraph getDependenceGraph(Node function) {
		return DependenceGraph.Factory.buildDDG(function);
	}

}