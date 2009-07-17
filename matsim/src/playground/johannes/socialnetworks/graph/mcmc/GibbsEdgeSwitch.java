/* *********************************************************************** *
 * project: org.matsim.*
 * GibbsEdgeReplace.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.johannes.socialnetworks.graph.mcmc;

/**
 * @author illenberger
 *
 */
public class GibbsEdgeSwitch extends GibbsEdgeFlip {

	public boolean step(AdjacencyMatrix y, ConditionalDistribution d) {
		boolean accept = false;
		int idx_ij = random.nextInt(edges.length);
		int i = edges[idx_ij][0];
		int j = edges[idx_ij][1];
		
		int u = random.nextInt(y.getVertexCount());
		int v = random.nextInt(y.getVertexCount());
		
		if(u != v && !y.getEdge(u, v)) {
			double p_change = 1/d.changeStatistic(y, i, j, true) * d.changeStatistic(y, u, v, false);
			
			double p = 1 / (1 + p_change);
			if(random.nextDouble() <= p) {
				y.removeEdge(i, j);
				y.addEdge(u, v);
				
				edges[idx_ij][0] = u;
				edges[idx_ij][1] = v;
				
				accept = true;
			}
		}
		
		return accept;
	}
}
