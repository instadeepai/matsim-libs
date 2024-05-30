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
import org.apache.commons.jxpath.ri.compiler.Step;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.sbb.matsim.contrib.railsim.rl.utils.RLUtils.getPathToSwitchNodeOnTrack;
import static ch.sbb.matsim.contrib.railsim.rl.utils.RLUtils.updateRoute;

public class RLTrainDisposition implements TrainDisposition {
	RailResourceManager resources;
	TrainRouter router;
	Network network;
	RLClient rlClient;

	Map<String, StepOutput> bufferStepOutputMap;

	@Inject
	public RLTrainDisposition(RailResourceManager resources, TrainRouter router, Network network, RLClient rlClient) {
		this.resources = resources;
		this.router = router;
		this.network = network;
		this.rlClient = rlClient;
		this.bufferStepOutputMap = new HashMap<>();
	}

	private Double getReward(TrainState train){
		/**
		 * 1. check if the train is departing from one of the halts
		 * 2. Get the actual departure time
		 * 3. Get the scheduled departure time
		 * 4. calculate reward
		 *
		 * TODO: What happens when the train arrives late at a halt? Does it wait for fixed amount of time as scheduled or it leaves as per the scheduled departure time if possible?
		 *
		 * TODO: What happens if the train arrives at a halt later than it's departure time? Does the train stop at all?
		 */


		return -1.0;
	}

	@Override
	public void onDeparture(double time, MobsimDriverAgent driver, List<RailLink> route) {
		// Update route for the train until the next switch node.
		Link curLink = network.getLinks().get(driver.getCurrentLinkId());
		getPathToSwitchNodeOnTrack(curLink, null, route);
	}

	@Override
	public DispositionResponse requestNextSegment(double time, TrainPosition position, double dist) {
		// calculate and send StepOutput to rl
		Map<String, StepOutput> stepOutputMap = getStepOutput(position, false, time);

		if (bufferStepOutputMap.size()==0){
			rlClient.sendObservation(stepOutputMap);
		}
		else{
			bufferStepOutputMap.putAll(stepOutputMap);
			bufferStepOutputMap.clear();
		}

		// get action from rl
		Map<String, Integer> actionMap = rlClient.getAction();

		//update route based on the action from rl
		int action = actionMap.get(position.getTrain().id().toString());

		StepOutput out = stepOutputMap.get(position.getTrain().id().toString());
		switch (action){
			case 0:{
				// update route in the query direction
				Node nextSwitchNodePos = network.getNodes().get(out.getObservation().getObsTree().get(1).getNodeId());
				updateRoute(network, (TrainState) position, nextSwitchNodePos);
				break;
			}
			case 1:{
				// update route in the other direction
				Node nextSwitchNodePos = network.getNodes().get(out.getObservation().getObsTree().get(2).getNodeId());
				updateRoute(network, (TrainState) position, nextSwitchNodePos);
				break;
			}
			case 2:{
				// stop the train
				return new DispositionResponse(0, 0, null);
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

	@Override
	public void onArrival(double time, TrainPosition position) {
		// Store the StepOutput in bufferStepOutput.
		// bufferStepOutput is not sent to RL until there is an observation for a train whose done=false
		bufferStepOutputMap.putAll(getStepOutput(position, true, time));

	}


	Observation getObservation(double time, TrainPosition train){

		Observation ob = new Observation();

		// get observation for each train
		TreeObservation treeObs = new TreeObservation((TrainState) train, this.resources, this.network, 2);
		List<Double> treeObsFlattened = treeObs.getFlattenedObservationTree();
		List<ObservationTreeNode> listObsNodes = treeObs.getObservationTree();

		// set ObsTree field of observation
		ob.setObsTree(listObsNodes);
		ob.setFlattenedObsTree(treeObsFlattened);

		// choose the left child of the root node as the nextNode
		Node nextNode = network.getNodes().get(listObsNodes.get(1).getNodeId());
		List<Double> positionNextNode  = new ArrayList<>();
		positionNextNode.add(nextNode.getCoord().getX());
		positionNextNode.add(nextNode.getCoord().getY());

		// set the PositionNextNode of the observation
		ob.setPositionNextNode(positionNextNode);

		// set the state of the train - headlink fromNode Coords, headPostion, speed
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

	private Map<String, StepOutput> getStepOutput(TrainPosition train, Boolean done, double time){

		StepOutput stepOutput = new StepOutput();

		stepOutput.setInfo(null);
		stepOutput.setReward(getReward((TrainState) train));
		stepOutput.setTerminated(done);
		stepOutput.setTruncated(done);
		stepOutput.setObservation(getObservation(time, train));

		Map<String, StepOutput> stepOutputMap= new HashMap<>();
		stepOutputMap.put(train.getTrain().id().toString(), stepOutput);

		return stepOutputMap;
	}
}




