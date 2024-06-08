package ch.sbb.matsim.contrib.railsim.rl;

import ch.sbb.matsim.contrib.railsim.grpc.RailsimConnecterGrpc;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoActionMap;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoStepOutputMap;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoObservation;
import ch.sbb.matsim.contrib.railsim.grpc.ProtoStepOutput;
//import ch.sbb.matsim.contrib.railsim.grpc.*;

import ch.sbb.matsim.contrib.railsim.rl.observation.Observation;
import ch.sbb.matsim.contrib.railsim.rl.observation.StepOutput;
import io.grpc.*;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.Empty;


public class RLClient {
    private RailsimConnecterGrpc.RailsimConnecterBlockingStub blockingStub=null;
    private static final Logger logger = Logger.getLogger(RLClient.class.getName());
    public RLClient(int port){
		String target = "localhost:"+port;
		ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
			.build();
        blockingStub = RailsimConnecterGrpc.newBlockingStub(channel);
    }

    public void getAction(Map<String, Integer> resultActionMap){

        ProtoActionMap actionMap=null;

        try {
            // Call the original method on the server.
			Empty request = Empty.newBuilder().build();
			actionMap = blockingStub.getAction(request);
        } catch (StatusRuntimeException e) {
            // Log a warning if the RPC fails.
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
//            return null;
        }
		resultActionMap.putAll(actionMap.getDictActionMap());
//        return actionMap.getDictActionMap();
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

//    public static void  main(String args[]) throws InterruptedException {
//        // Access a service running on the local machine on port 50051
//		RLClient client = new RLClient(50051);
//		Observation ob = new Observation(2, true);
//		Map<String, Integer> actionMap = client.getAction();
//		System.out.println(actionMap);
//    }
}
