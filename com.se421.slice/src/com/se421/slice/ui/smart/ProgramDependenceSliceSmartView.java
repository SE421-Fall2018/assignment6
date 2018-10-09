package com.se421.slice.ui.smart;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.se421.slice.analysis.DependenceGraph;

public class ProgramDependenceSliceSmartView extends DependenceSliceSmartView {

	@Override
	public String getTitle() {
		return "Program Dependence Slice (PDG)";
	}
	
	@Override
	protected DependenceGraph getDependenceGraph(Node function) {
		return DependenceGraph.Factory.buildPDG(function);
	}
	
}