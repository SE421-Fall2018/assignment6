package com.se421.slice.ui.smart;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.se421.slice.analysis.DependenceGraph;

public class ControlDependenceSliceSmartView extends DependenceSliceSmartView {

	@Override
	public String getTitle() {
		return "Control Dependence Slice (CDG)";
	}
	
	@Override
	protected DependenceGraph getDependenceGraph(Node function) {
		return DependenceGraph.Factory.buildCDG(function);
	}

}