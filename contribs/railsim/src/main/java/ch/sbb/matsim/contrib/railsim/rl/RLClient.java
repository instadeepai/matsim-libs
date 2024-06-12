package ch.sbb.matsim.contrib.railsim.rl;

import ch.sbb.matsim.contrib.railsim.grpc.RailsimConnecterGrpc;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoActionMap;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoStepOutputMap;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoObservation;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoStepOutput;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoGetActionRequest;
//import ch.sbb.matsim.contrib.railsim.grpc.*;

import ch.sbb.matsim.contrib.railsim.rl.observation.Observation;
import ch.sbb.matsim.contrib.railsim.rl.observation.StepOutput;

import io.grpc.*;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.Empty;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;


public class RLClient {
    private RailsimConnecterGrpc.RailsimConnecterBlockingStub blockingStub=null;
    private static final Logger logger = Logger.getLogger(RLClient.class.getName());

	ManagedChannel channel;
	ExecutorService executorService;
	private final EventLoopGroup eventLoopGroup;


	public RLClient(int port){
		// Create a single-threaded executor
		executorService = Executors.newSingleThreadExecutor();

		// Create a single-threaded executor
		this.executorService = Executors.newSingleThreadExecutor();

		// Create a single-threaded event loop group
		this.eventLoopGroup = new NioEventLoopGroup(1);

		// Build the channel with the custom executor and event loop group
		this.channel = NettyChannelBuilder.forAddress("localhost", port)
			.usePlaintext()  // Use plaintext for simplicity; switch to TLS in production
			.executor(executorService)
			.eventLoopGroup(eventLoopGroup)
			.channelType(NioSocketChannel.class)
			.build();

		blockingStub = RailsimConnecterGrpc.newBlockingStub(channel);
    }

    public void getAction(double time, String trainId, Map<String, Integer> resultActionMap){

        ProtoActionMap actionMap=null;
		ProtoGetActionRequest request = ProtoGetActionRequest.newBuilder()
			.setTimestamp((time))
			.setTrainId(trainId)
			.build();
        try {
            // Call the original method on the server.
			actionMap = blockingStub.getAction(request);
        } catch (StatusRuntimeException e) {
            // Log a warning if the RPC fails.
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
		resultActionMap.putAll(actionMap.getDictActionMap());
    }

    public String sendObservation(Map<String, StepOutput> stepOutputMap){

        ch.sbb.matsim.contrib.railsim.grpc.ProtoConfirmationResponse msg;

		ProtoStepOutputMap.Builder protoStepOutputMapBuilder = ProtoStepOutputMap.newBuilder();
		for (Map.Entry<String, StepOutput> entry: stepOutputMap.entrySet()){

			StepOutput stepOutput = entry.getValue();

			//Build the Observation object - this will be used inside the StepOutput class
			ProtoObservation protoObservation = ProtoObservation.newBuilder()
				.addAllObsTree(stepOutput.getObservation().getFlattenedObsTree())
				.addAllPositionNextNode(stepOutput.getObservation().getPositionNextNode())
				.addAllTrainState(stepOutput.getObservation().getTrainState())
				.setTimestamp(stepOutput.getObservation().getRailsim_timestamp())
				.build();

			// build StepOutput
			ProtoStepOutput protoStepOutput = ProtoStepOutput.newBuilder()
				.setObservation(protoObservation)
				.setReward(stepOutput.getReward())
				.setTerminated(stepOutput.isTerminated())
				.setTruncated(stepOutput.isTruncated())
				.build();

			// add stepOutput in the map
			protoStepOutputMapBuilder.putDictStepOutput(entry.getKey(), protoStepOutput);
		}

		ProtoStepOutputMap protoStepOutputMap = protoStepOutputMapBuilder.build();
        try {
            // Call the original method on the server.
            msg = blockingStub.updateState(protoStepOutputMap);
        } catch (StatusRuntimeException e) {
            // Log a warning if the RPC fails.
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return null;
        }

        return msg.getAck();
    }

	public void shutdown() {
		channel.shutdown();
		executorService.shutdown();
		eventLoopGroup.shutdownGracefully();
	}



}
