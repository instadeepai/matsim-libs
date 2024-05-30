package ch.sbb.matsim.contrib.railsim;

//import ch.sbb.matsim.contrib.railsim.rl.RLClient;

import ch.sbb.matsim.contrib.railsim.qsimengine.RailsimQSimModule;
import ch.sbb.matsim.contrib.railsim.qsimengine.RailsimRLQSimModule;
import ch.sbb.matsim.contrib.railsim.rl.RLClient;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.vehicles.Vehicle;
import org.matsim.visum.VisumNetwork;
import org.matsim.pt.transitSchedule.api.TransitLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RailsimEnv {
	RLClient rlClient; // RLClient would be needed by RailsimEngine.
	Controler controler;
	public RailsimEnv(RLClient rlClient){
//		TODO: Pass this to RLDisposition
		this.rlClient = rlClient;

	}

	private List<String> getAllTrainIds(Scenario scenario){

		List<String> trainIds = new ArrayList<>();

		List<TransitLine> transitLines = scenario.getTransitSchedule().getTransitLines().values().stream().collect(Collectors.toList());
		for (TransitLine trainLine : transitLines){
			List<TransitRoute> transitRoutes = trainLine.getRoutes().values().stream().collect(Collectors.toList());
			for (TransitRoute transitRoute: transitRoutes){
				List<Departure> departures= transitRoute.getDepartures().values().stream().collect(Collectors.toList());
				for(Departure departure: departures){
					Id<Vehicle> ID = departure.getVehicleId();
					trainIds.add(ID.toString());
				}
			}
		}
		return trainIds;
	}

	public List<String> reset(){

		// start the simulation
		// pass the observation to the RLClient

		String configFilename = "/Users/akashsinha/Documents/SBB/matsim-libs/contribs/railsim/test/input/ch/sbb/matsim/contrib/railsim/integration/microTrackOppositeTrafficMany/config.xml";

		Config config = ConfigUtils.loadConfig(configFilename);
		config.controller().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

		Scenario scenario = ScenarioUtils.loadScenario(config);
		controler = new Controler(scenario);

		controler.addOverridingModule(new RailsimModule());

		// if you have other extensions that provide QSim components, call their configure-method here
		controler.configureQSimComponents(components -> new RailsimRLQSimModule().configure(components));
		//TODO: Fix Me: implement the method getAllTrainIds()
		// get all train Ids in this scenario.
		return getAllTrainIds(scenario);
	}

	void startSimulation(){
		controler.run();
	}


}
