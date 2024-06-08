package ch.sbb.matsim.contrib.railsim.qsimengine.disposition;

import ch.sbb.matsim.contrib.railsim.qsimengine.RailsimCalc;
import ch.sbb.matsim.contrib.railsim.qsimengine.TrainPosition;
import ch.sbb.matsim.contrib.railsim.qsimengine.TrainState;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailLink;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailResource;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailResourceManager;
import ch.sbb.matsim.contrib.railsim.qsimengine.router.TrainRouter;
import ch.sbb.matsim.contrib.railsim.rl.RLClient;
import ch.sbb.matsim.contrib.railsim.rl.observation.Observation;
import ch.sbb.matsim.contrib.railsim.rl.observation.ObservationTreeNode;
import ch.sbb.matsim.contrib.railsim.rl.observation.StepOutput;
import ch.sbb.matsim.contrib.railsim.rl.observation.TreeObservation;
import jakarta.inject.Inject;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.vehicles.Vehicle;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static ch.sbb.matsim.contrib.railsim.rl.utils.RLUtils.*;
import static java.util.concurrent.Executors.*;

public class RLTrainDisposition implements TrainDisposition {
	RailResourceManager resources;
	TrainRouter router;
	Network network;
	RLClient rlClient;

	Map<String, StepOutput> bufferStepOutputMap;

	Map<Id<Vehicle>, Double> delays;

	Map<Id<Vehicle>, Map<Id<Link>, List<Double>>> departureSchedule;

	Map<Id<Vehicle>, TrainPosition> activeTrains;

	@Inject
	public RLTrainDisposition(RailResourceManager resources, TrainRouter router, Network network, RLClient rlClient) {
		this.resources = resources;
		this.router = router;
		this.network = network;
		this.rlClient = rlClient;
		this.bufferStepOutputMap = new HashMap<>();
		this.departureSchedule = new HashMap<>();
		this.activeTrains = new HashMap<>();
		this.delays = new HashMap<>();
		System.out.println("RLTrainDisposition created");
	}

	private void calculateScheduleOnDeparture(TrainState train){
        assert train.getPt() != null;

		// get vehicle Id
		Id<Vehicle> trainId = train.getPt().getPlannedVehicleId();

		// use trainsit route to get departures for the train
        TransitRoute transitRoute = train.getPt().getTransitRoute();
		List<Departure> departures = transitRoute.getDepartures().values().stream().collect(Collectors.toList());

		for (Departure departure: departures){
			if (departure.getVehicleId().equals(trainId)){
				double routeDepartureTime = departure.getDepartureTime();

				for (int i=0; i<transitRoute.getStops().size(); i++ ){

					TransitRouteStop stop = transitRoute.getStops().get(i);
					Id<Link> stopLinkId = stop.getStopFacility().getLinkId();

					double offset = 0.0;
					if (i==transitRoute.getStops().size()-1){
						// for the last stop, use arrival offset
						offset = stop.getArrivalOffset().seconds();
					}else{
						// this would work fine all stops except the last stop
						// as the last stop does not have any departure offset
						offset = stop.getDepartureOffset().seconds();
					}
					// calculate scheduled departure time
					double scheduledDepartureTime = routeDepartureTime + offset;

					// store time scheduled departure time corresponding to the train and halt
					if (!this.departureSchedule.containsKey(trainId)){
						Map<Id<Link>, List<Double>> mapLinkDepartureTime = new HashMap<>();
						departureSchedule.put(trainId, mapLinkDepartureTime);
					}
					if (!departureSchedule.get(trainId).containsKey(stopLinkId)){
						List<Double> listDepartureTimes = new ArrayList<>();
						departureSchedule.get(trainId).put(stopLinkId, listDepartureTimes);
					}
					departureSchedule.get(trainId).get(stopLinkId).add(scheduledDepartureTime);
				}

			}
		}
	}

	private Double getReward(){
		/**
		 * 1. Calculate the sum of delays incurred
		 * 2. Clear the delays
		 * */

		double reward = 0.0;
		List<Double> departureDelays= delays.values().stream().collect(Collectors.toList());
		for (double t : departureDelays){
			reward -= t;
		}

		this.delays.clear();
		return reward;
	}

	@Override
	public void onDeparture(double time, TrainPosition train, List<RailLink> route) {

		// Update route for the train until the next switch node.
		Link curLink = network.getLinks().get(train.getHeadLink());

		getPathToSwitchNodeOnTrack(curLink, null, route, resources, network);

		// calculate the scheduled departure times of the train
		calculateScheduleOnDeparture((TrainState) train);

		// update the active departure list
		activeTrains.put(train.getPt().getPlannedVehicleId(), train);
	}

	@Override
	public DispositionResponse requestNextSegment(double time, TrainPosition position, double dist) {

		Id<Vehicle> trainId = position.getPt().getPlannedVehicleId();
		RailLink bufferTipLink = getBufferTip(resources, (TrainState) position);

		// outTracks contain list of outLinks from bufferTipLink. The list does not contain the reverse link of bufferTipLink
		List<Link> outTracks = getOutTracks(network.getLinks().get(bufferTipLink.getLinkId()), network);

//		if(outTracks.size()>=2){
		if(true){
			// RL should be called only when the train's safety buffer is about to reach the decision node

			// calculate and send StepOutput to rl
			Observation ob = getObservation(time, position);
			Map<String, StepOutput> stepOutputMap = getStepOutput(position, ob, getReward(), false);
			if (activeTrains.containsKey(trainId)){
				bufferStepOutputMap.putAll(stepOutputMap);
				rlClient.sendObservation(bufferStepOutputMap);


				// start a new thread and send observation through rlClient
//				Thread rlSendObservationThread = new Thread(() -> rlClient.sendObservation(bufferStepOutputMap));
//				rlSendObservationThread.start();
//				try {
//					// Wait for the rlSendObservationThread to finish
//					rlSendObservationThread.join();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
				// clear the buffer after sending the stepOutput
				bufferStepOutputMap.clear();

				// start a new thread ot get action from rl
				Map<String, Integer> actionMap = new HashMap<>();
				rlClient.getAction(actionMap);
//				Thread rlgetActionThread = new Thread(() -> rlClient.getAction(actionMap));
//				rlgetActionThread.start();
//				try {
//					// Wait for the rlgetActionThread to finish
//					rlgetActionThread.join();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}

//			rlClient.getAction(actionMap);
				int action = actionMap.get(trainId);
//				int action = actionMap.values().iterator().next();


//			// Create a single-threaded executor
//			ExecutorService executorService = newSingleThreadExecutor();
//
//			// Create a Callable task
//			Callable<Map<String, Integer>> task = () -> rlClient.getAction();
//
//			// Submit the task to the executor service and get a Future object
//			Future<Map<String, Integer>> futureResult = executorService.submit(task);
//			Map<String, Integer> result = null;
//			try {
//				// Wait for the task to complete and get the result
//				result = futureResult.get();
//				System.out.println("task returned: " + result);
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
////				executorService.shutdown();
//			}
//			finally {
//				// Shutdown the executor service
//				executorService.shutdown();
//			}

			//update route based on the action from rl
//			int action = result.get(trainId);

//			StepOutput out = stepOutputMap.get(position.getPt().getPlannedVehicleId().toString());
				StepOutput out = stepOutputMap.values().iterator().next();
//			switch (action){
//				case 0:{
//					// update route in the query direction
//					Node nextSwitchNodePos = network.getNodes().get(out.getObservation().getObsTree().get(1).getNodeId());
//					updateRoute(network, (TrainState) position, nextSwitchNodePos, resources);
//					break;
//				}
//				case 1:{
//					// update route in the other direction
//					Node nextSwitchNodePos = network.getNodes().get(out.getObservation().getObsTree().get(2).getNodeId());
//					updateRoute(network, (TrainState) position, nextSwitchNodePos, resources);
//					break;
//				}
//				case 2:{
//					// stop the train
//					return new DispositionResponse(0, 0, null);
//				}
//				default:{
//					System.out.println("Illegal action");
//				}
//
//			}
			}
		}

		RailLink currentLink = resources.getLink(position.getHeadLink());
		List<RailLink> segment = RailsimCalc.calcLinksToBlock(position, currentLink, dist);

		// NOTE: Check for rerouting is omitted in this implementation

		double reserveDist = resources.tryBlockLink(time, currentLink, RailResourceManager.ANY_TRACK_NON_BLOCKING, position);

		if (reserveDist == RailResource.NO_RESERVATION)
			return new DispositionResponse(0, 0, null);

		// current link only partial reserved
		if (reserveDist < currentLink.length) {
			return new DispositionResponse(reserveDist - position.getHeadPosition(), 0, null);
		}

		// remove already used distance
		reserveDist -= position.getHeadPosition();

		boolean stop = false;
		// Iterate all links that need to be blocked
		for (RailLink link : segment) {

			// first link does not need to be blocked again
			if (link == currentLink)
				continue;

			dist = resources.tryBlockLink(time, link, RailResourceManager.ANY_TRACK_NON_BLOCKING, position);

			if (dist == RailResource.NO_RESERVATION) {
				stop = true;
				break;
			}

			// partial reservation
			reserveDist += dist;

			// If the link is not fully reserved then stop
			// there might be a better advised speed (speed of train in-front)
			if (dist < link.getLength()) {
				stop = true;
				break;
			}
		}
		return new DispositionResponse(reserveDist, stop ? 0 : Double.POSITIVE_INFINITY, null);

	}

	@Override
	public void unblockRailLink(double time, MobsimDriverAgent driver, RailLink link) {
		// put resource handling into release track
		resources.releaseLink(time, link, driver);

	}

	public void onTermination(double time, TrainPosition position){
		// Store the StepOutput in bufferStepOutput.
		// bufferStepOutput is not sent to RL until there is an observation for a train whose done=false
		bufferStepOutputMap.putAll(getStepOutput(position, getObservation(time, position), getReward(), true));

		// remove the train from active trains
		activeTrains.remove(position.getPt().getPlannedVehicleId());
	}
	@Override
	public void onArrival(double time, TrainPosition position, Boolean terminated) {
		if (Boolean.TRUE.equals(terminated)){
			onTermination(time, position);
		}
	}

	@Override
	public void onStopDeparture(double time, TrainPosition position){
		// Calculate delays incurred by the system
		// this function should be called in leaveLink() method when the train departs from a  halt
		Id<Vehicle> trainId = position.getPt().getPlannedVehicleId();
		assert position.isStop(position.getHeadLink());
		List<Double> scheduledDepartureTimes = departureSchedule.get(trainId).get(position.getHeadLink());

		Double delay = 0.0;
		for (Double depTime : scheduledDepartureTimes){
			if (time - depTime > 0) {
				delay = Math.min(delay, time - depTime);
			}
		}
		delays.put(trainId, delay);
	}

	@Override
	public void onSimulationEnd(double now){
		// get observation for all active trains and
		// set all the trains having heavy negative reward

		for(TrainPosition train: activeTrains.values()){
			Map<String, StepOutput> stepOutputMap = getStepOutput(train, getObservation(now, train), -100.0, true);
			bufferStepOutputMap.putAll(stepOutputMap);
		}
		rlClient.sendObservation(bufferStepOutputMap);
		bufferStepOutputMap.clear();

	}

	Observation getObservation(double time, TrainPosition train){

		Observation ob = new Observation();

		// get observation for the train
		TreeObservation treeObs = new TreeObservation((TrainState) train, this.resources, this.network, 2);
		List<Double> treeObsFlattened = treeObs.getFlattenedObservationTree();
		List<ObservationTreeNode> listObsNodes = treeObs.getObservationTree();

		// set ObsTree field of observation
		ob.setObsTree(listObsNodes);
		ob.setFlattenedObsTree(treeObsFlattened);


		Node nextNode = null;
		if (listObsNodes.size()>1){
			// choose the 1st child of the root node as the nextNode for query direction
			nextNode = network.getNodes().get(listObsNodes.get(1).getNodeId());
		}else{
			// if there are no children of the root node then make root as the nextNode for query
			nextNode = network.getNodes().get(listObsNodes.get(0).getNodeId());
		}

		// get coords of the query node
		List<Double> positionNextNode  = new ArrayList<>();
		positionNextNode.add(nextNode.getCoord().getX());
		positionNextNode.add(nextNode.getCoord().getY());

		// set the PositionNextNode of the observation
		ob.setPositionNextNode(positionNextNode);

		// set the state of the train - headlink fromNode Coords, headPosition, speed
		List<Double> extractedTrainState = new ArrayList<>();

		// Add headlink fromNode Coords
		List<Double> headLinkFromNodePosition = new ArrayList<>();
		Coord headLinkFromNodeCoord = network.getLinks().get(train.getHeadLink()).getFromNode().getCoord();
		headLinkFromNodePosition.add(headLinkFromNodeCoord.getX());
		headLinkFromNodePosition.add(headLinkFromNodeCoord.getY());

		extractedTrainState.addAll(headLinkFromNodePosition);

		// TODO: Add train speed
		extractedTrainState.add(train.getTrain().maxVelocity());

		// Add headPosition
		extractedTrainState.add(train.getHeadPosition());

		ob.setTrainState(extractedTrainState);

		// Add railsim timestep
		ob.setRailsim_timestamp(time);

		return ob;
	}

	private Map<String, StepOutput> getStepOutput
		(TrainPosition train, Observation observation, double reward, Boolean done){

		StepOutput stepOutput = new StepOutput();

		stepOutput.setInfo(null);
		stepOutput.setReward(reward);
		stepOutput.setTerminated(done);
		stepOutput.setTruncated(done);
		stepOutput.setObservation(observation);

		Map<String, StepOutput> stepOutputMap= new HashMap<>();
		stepOutputMap.put(train.getPt().getPlannedVehicleId().toString(), stepOutput);

		return stepOutputMap;
	}
}




