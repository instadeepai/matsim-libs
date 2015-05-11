/* *********************************************************************** *
 * project: org.matsim.*
 * RunMyHybridSim.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
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

package playground.gregor.hybridsim.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.HybridNetworkFactory;
import org.matsim.core.scenario.ScenarioUtils;

import playground.gregor.hybridsim.factories.HybridMobsimProvider;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;



public class RunMyHybridSim {

	public static void main(String [] args) {
		String config = "/Users/laemmel/arbeit/papers/2015/trgindia2015/hhwsim/input/config.xml";
		Config c = ConfigUtils.createConfig();
		ConfigUtils.loadConfig(c, config);
		c.controler().setWriteEventsInterval(1);

		c.qsim().setEndTime(3600);
		
		final Scenario sc = ScenarioUtils.loadScenario(c);

		final Controler controller = new Controler(sc);
		controller.setOverwriteFiles(true);
		

		final HybridNetworkFactory netFac = new HybridNetworkFactory();
//		netFac.putNetsimNetworkFactory("2ext", netFac);
		
		
		Injector mobsimProviderInjector = Guice.createInjector(new com.google.inject.AbstractModule(){
			@Override
			protected void configure() {
				bind(Scenario.class).toInstance(sc);
				bind(EventsManager.class).toInstance(controller.getEvents());
				bind(HybridNetworkFactory.class).toInstance(netFac);
			}
			
		});
		final Provider<Mobsim> mobsimProvider = mobsimProviderInjector.getInstance(HybridMobsimProvider.class);
		controller.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				bind(Mobsim.class).toProvider(mobsimProvider);
				
			}
		});
		controller.run();
	}
}