package ch.sbb.matsim.contrib.railsim.qsimengine;

import ch.sbb.matsim.contrib.railsim.qsimengine.deadlocks.DeadlockAvoidance;
import ch.sbb.matsim.contrib.railsim.qsimengine.deadlocks.SimpleDeadlockAvoidance;
import ch.sbb.matsim.contrib.railsim.qsimengine.disposition.RLTrainDisposition;
import ch.sbb.matsim.contrib.railsim.qsimengine.disposition.SimpleDisposition;
import ch.sbb.matsim.contrib.railsim.qsimengine.disposition.TrainDisposition;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailResourceManager;
import ch.sbb.matsim.contrib.railsim.qsimengine.router.TrainRouter;
import ch.sbb.matsim.contrib.railsim.rl.RLClient;
import com.google.inject.Provides;
import com.google.inject.multibindings.OptionalBinder;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfig;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfigurator;
import org.matsim.core.mobsim.qsim.pt.TransitDriverAgentFactory;

public class RailsimRLQSimModule extends AbstractQSimModule implements QSimComponentsConfigurator{

		public static final String COMPONENT_NAME = "Railsim";

		RLClient rlClient;
		public RailsimRLQSimModule(RLClient rlClient){
			this.rlClient = rlClient;
		}
		@Override
		public void configure(QSimComponentsConfig components) {
			components.addNamedComponent(COMPONENT_NAME);
		}

		@Override
		protected void configureQSim() {
			bind(RailsimQSimEngine.class).asEagerSingleton();

			bind(TrainRouter.class).asEagerSingleton();
			bind(RailResourceManager.class).asEagerSingleton();

			bind(RLClient.class).toInstance(this.rlClient);

			// These interfaces might be replaced with other implementations
			bind(TrainDisposition.class).to(RLTrainDisposition.class).asEagerSingleton();
//			bind(TrainDisposition.class).to(SimpleDisposition.class).asEagerSingleton();
			bind(DeadlockAvoidance.class).to(SimpleDeadlockAvoidance.class).asEagerSingleton();

			addQSimComponentBinding(COMPONENT_NAME).to(RailsimQSimEngine.class);

			OptionalBinder.newOptionalBinder(binder(), TransitDriverAgentFactory.class)
				.setBinding().to(RailsimDriverAgentFactory.class);
		}

//		@Provides
//		RLClient provideRLClient() {
//			// Create and return the instance of RLClient
//			return this.rlClient;
//		}
}
