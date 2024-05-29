package ch.sbb.matsim.contrib.railsim;
import ch.sbb.matsim.contrib.railsim.rl.RLClient;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoConfirmationResponse;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoGrpcPort;
import ch.sbb.matsim.contrib.railsim.grpc.RailsimFactoryGrpc;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoAgentIDs;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
public class EnvironmentFactoryServer {

    private static final Logger logger = Logger.getLogger(EnvironmentFactoryServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int factoryServerPort = 50051;
        server = Grpc.newServerBuilderForPort(factoryServerPort, InsecureServerCredentials.create())
                .addService(new RailsimFactory())
                .build()
                .start();
        logger.info("Server started, listening on " + factoryServerPort);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    EnvironmentFactoryServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final EnvironmentFactoryServer server = new EnvironmentFactoryServer();
        server.start();
        server.blockUntilShutdown();
    }


    // Implementation of the gRPC service on the server-side.
    private class RailsimFactory extends RailsimFactoryGrpc.RailsimFactoryImplBase {

        Map<Integer, RailsimEnv> envMap = new HashMap<>();

        @Override
        public void getEnvironment(ProtoGrpcPort grpcPort, StreamObserver<ProtoConfirmationResponse> responseObserver) {
            //     Create an instance of Railsim environment and store it in a map
            System.out.println("getEnvironment() -> Create env with id: "+grpcPort);

			RLClient rlClient = new RLClient(grpcPort.getGrpcPort());
            RailsimEnv env = new RailsimEnv(rlClient);

            // Store the environment created with it's key being the port
            this.envMap.put(grpcPort.getGrpcPort(), env);

			// Send the Ack message back to the client.
			ProtoConfirmationResponse response = ProtoConfirmationResponse.newBuilder()
				.setAck("OK")
				.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
        }

        public void resetEnv(ProtoGrpcPort grpcPort, StreamObserver<ProtoAgentIDs> responseObserver) {
            System.out.println("Reset env id: "+grpcPort);

			// fetch the object from map and reset it
			RailsimEnv env = this.envMap.get(grpcPort.getGrpcPort());
			List<String> agentIds = env.reset();

			//Create response using agentIds
			ProtoAgentIDs response = ProtoAgentIDs.newBuilder()
				.addAllAgentId(agentIds)
				.build();

			// Send the reply back to the client.
            responseObserver.onNext(response);
            // Indicate that no further messages will be sent to the client.
            responseObserver.onCompleted();

			// Start the simulation
			env.startSimulation();

        }
    }

}
