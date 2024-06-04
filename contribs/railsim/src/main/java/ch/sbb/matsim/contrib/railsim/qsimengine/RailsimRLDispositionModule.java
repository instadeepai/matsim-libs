package ch.sbb.matsim.contrib.railsim.qsimengine;

import ch.sbb.matsim.contrib.railsim.qsimengine.disposition.RLTrainDisposition;
import ch.sbb.matsim.contrib.railsim.qsimengine.disposition.TrainDisposition;
import ch.sbb.matsim.contrib.railsim.rl.RLClient;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfig;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfigurator;

public class RailsimRLDispositionModule extends AbstractQSimModule implements QSimComponentsConfigurator {

	private final RLClient rlClient;

	public RailsimRLDispositionModule(RLClient rlClient) {

		this.rlClient = rlClient;
	}

	@Override
	public void configure(QSimComponentsConfig components) {
	}

	@Override
	protected void configureQSim() {

		bind(RLClient.class).toInstance(rlClient);
		bind(TrainDisposition.class).to(RLTrainDisposition.class).asEagerSingleton();


	}
}
