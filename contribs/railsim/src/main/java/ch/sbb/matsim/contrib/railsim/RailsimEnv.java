package ch.sbb.matsim.contrib.railsim;

//import ch.sbb.matsim.contrib.railsim.rl.RLClient;

import ch.sbb.matsim.contrib.railsim.qsimengine.RailsimQSimModule;
import ch.sbb.matsim.contrib.railsim.rl.RLClient;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RailsimEnv {
	RLClient rlClient; // RLClient would be needed by RailsimEngine.
	Controler controler;
	public RailsimEnv(RLClient rlClient){

//		TODO: How to pass this rlClient to the RailsimEngine from this class?
		this.rlClient = rlClient;

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
		controler.configureQSimComponents(components -> new RailsimQSimModule().configure(components));

		// get all train Ids in this scenario.
		//TODO: Fix Me: implement the method getAllTrainIds()
		List<String> trainIds = new ArrayList<>(); //getAllTrainIds();
		trainIds.add("train0");

		return trainIds;
	}

	void startSimulation(){
		controler.run();
	}


}
