package com.se421.slice.ui.smart;

import com.ensoftcorp.atlas.core.query.Q;

/**
 * Holds and unstyled frontier result
 */
public class UnstyledFrontierResult {
	private Q result;
	private Q frontierForward;
	private Q frontierReverse;
	
	public UnstyledFrontierResult(Q result, Q frontierReverse, Q frontierForward){
		this.result = result;
		this.frontierReverse = frontierReverse;
		this.frontierForward = frontierForward;
	}
	
	public Q getResult(){
		return result;
	}
	
	public Q getFrontierForward(){
		return frontierForward;
	}
	
	public Q getFrontierReverse(){
		return frontierReverse;
	}
}