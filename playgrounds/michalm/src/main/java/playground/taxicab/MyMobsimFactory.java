/* *********************************************************************** *
 * project: org.matsim.*
 * MyMobsimFactory.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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
package playground.taxicab;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.DriverAgent;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.framework.MobsimFactory;
import org.matsim.core.mobsim.framework.Simulation;
import org.matsim.core.router.util.PersonalizableTravelCost;
import org.matsim.core.router.util.PersonalizableTravelTime;
import org.matsim.ptproject.qsim.QSim;
import org.matsim.ptproject.qsim.interfaces.DepartureHandler;
import org.matsim.ptproject.qsim.qnetsimengine.QVehicle;
import org.matsim.ptproject.qsim.qnetsimengine.QVehicleImpl;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleImpl;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleTypeImpl;

/**
 * @author nagel
 *
 */
public class MyMobsimFactory implements MobsimFactory {
	private static final Logger log = Logger.getLogger(MyMobsimFactory.class);

	private PersonalizableTravelCost travCostCalc;
	private PersonalizableTravelTime travTimeCalc;

	MyMobsimFactory( PersonalizableTravelCost travelCostCalculator, PersonalizableTravelTime travelTimeCalculator ) {
		this.travCostCalc = travelCostCalculator ;
		this.travTimeCalc = travelTimeCalculator ;
	}

	@Override
	public Simulation createMobsim(final Scenario sc, EventsManager events) {

		final QSim mobsim = new QSim( sc, events ) ;
		
		// add the taxi departure handler to the mobsim:
		DepartureHandler departureHandler = new TaxiModeDepartureHandler(mobsim) ;
		mobsim.addDepartureHandler(departureHandler) ;

		// add one taxi:
		AgentSource taxiCabSource = new AgentSource() {
			@Override
			public List<MobsimAgent> insertAgentsIntoMobsim() {
				List<MobsimAgent> agents = new ArrayList<MobsimAgent>() ;
				
				MobsimDriverAgent taxiagent = new TaxicabAgent(mobsim) ;
				agents.add(taxiagent) ;
				
				// the vehicle needs to be parked before agentDeparture!!
				VehicleType defaultVehicleType = new VehicleTypeImpl(new IdImpl("defaultVehicleType"));
				VehicleImpl vehicle = new VehicleImpl(new IdImpl("taxiVehicle"), defaultVehicleType);
				QVehicle veh = new QVehicleImpl(vehicle);
				veh.setDriver(taxiagent); // this line is currently only
															// needed for OTFVis to show
															// parked vehicles
				taxiagent.setVehicle(veh);


				mobsim.parkVehicleOnInitialLink(taxiagent);
				
				mobsim.arrangeAgentDeparture(taxiagent) ;
				
				return agents ;
			}
		} ;
		mobsim.addAgentSource(taxiCabSource) ;

		// add the taxicab dispatcher:
		Dispatcher dispatcher = new Dispatcher( mobsim.getEventsManager() ) ;
		mobsim.getEventsManager().addHandler(dispatcher ) ;
		
		// commented out but we may need this later
//		mobsim.addQueueSimulationListeners(new MyWithinDayMobsimListener(this.travCostCalc,this.travTimeCalc)) ;

		return mobsim ;
	}

}
