/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
// Adapted from: https://svn.apache.org/repos/asf/flex/falcon/trunk/compiler/src/org/apache/flex/abc/graph/algorithms/DepthFirstPreorderIterator.java

package com.se421.slice.analysis.utilities.dominance;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;

/**
 * InvertedDepthFirstPreorderIterator yields a depth-first an inverted pre-order traversal of a graph
 */
public class InvertedDepthFirstPreorderIterator extends DepthFirstPreorderIterator {

	public InvertedDepthFirstPreorderIterator(UniqueEntryExitGraph graph, AtlasSet<Node> roots) {
		super(graph, roots);
	}
	
	public InvertedDepthFirstPreorderIterator(UniqueEntryExitGraph graph, Node root) {
		super(graph, root);
	}

	@Override
	protected AtlasSet<Node> getSuccessors(Node node) {
		return graph.getPredecessors(node);
	}
}