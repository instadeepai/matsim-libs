/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2023 by the members listed in the COPYING,        *
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

package ch.sbb.matsim.contrib.railsim;

import ch.sbb.matsim.contrib.railsim.qsimengine.TrainState;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

import ch.sbb.matsim.contrib.railsim.qsimengine.RailsimQSimModule;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.util.List;
import java.util.Map;

/**
 * Example script that shows how to use railsim included in this contrib.
 */
public final class RunRailsimExample {

	private RunRailsimExample() {
	}

	public static void main(String[] args) {

		String configFilename;
		if (args.length != 0) {
			configFilename = args[0];
		} else {
			configFilename = "/Users/akashsinha/Documents/SBB/matsim-libs/contribs/railsim/test/input/ch/sbb/matsim/contrib/railsim/integration/microTrackOppositeTrafficMany/config.xml";
		}

		Config config = ConfigUtils.loadConfig(configFilename);
		config.controller().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

		Scenario scenario = ScenarioUtils.loadScenario(config);
		Controler controler = new Controler(scenario);

		controler.addOverridingModule(new RailsimModule());

		// if you have other extensions that provide QSim components, call their configure-method here
		controler.configureQSimComponents(components -> new RailsimQSimModule().configure(components));

		controler.run();

		// Required: List of all agents
		// Schedule of arrival and departure for all the halts for all trains

//		// Transit stops
//		Map<Id<TransitStopFacility>, TransitStopFacility> transitStops = scenario.getTransitSchedule().getFacilities();
//
//		// Transit lines
//		Map<Id<TransitLine>, TransitLine> transitLineMap = scenario.getTransitSchedule().getTransitLines();

		/*
		Transit line contains:
		 - transitRoute
			- route profile : sequence of stops with their arrival and departure times
			- route: sequence of links
			- departures: the train ids and their corresponding departure times.

		Each stop is essentially a link

		There can be multiple transit lines
		 */

		/*
		Output data structure

		 */
//		List<TransitLine> transitLines  = scenario.getTransitSchedule().getTransitLines().values().stream().toList();
//
//		for (TransitLine tl: transitLines){
//			List<TransitRoute> transitRoutes = tl.getRoutes().values().stream().toList();
//			for (TransitRoute tr: transitRoutes){
//				tr.getDepartures();
//				tr.getStops().get(0).getStopFacility();
//
//			}
//		}


	}

}
