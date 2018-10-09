package com.se421.slice.ui.smart;

import com.ensoftcorp.atlas.core.query.Q;
import com.se421.slice.codemap.DominanceAnalysis;

public class ControlFlowPostDominanceFrontierSmartView extends ControlFlowDominanceSmartView {

	@Override
	public String getTitle() {
		return "Control Flow Post Dominance Frontier";
	}
	
	@Override
	protected Q getDominanceEdges() {
		return DominanceAnalysis.getPostDominanceFrontierEdges();
	}
	
}