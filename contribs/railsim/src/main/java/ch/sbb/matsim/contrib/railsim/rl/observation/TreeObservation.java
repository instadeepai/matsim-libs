package ch.sbb.matsim.contrib.railsim.rl.observation;

import ch.sbb.matsim.contrib.railsim.qsimengine.RailsimCalc;
import ch.sbb.matsim.contrib.railsim.qsimengine.TrainPosition;
import ch.sbb.matsim.contrib.railsim.qsimengine.TrainState;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailLink;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailResourceManager;
import org.apache.commons.lang3.NotImplementedException;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;

import java.util.*;
import java.util.stream.Collectors;

import ch.sbb.matsim.contrib.railsim.rl.utils.RLUtils;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.vehicles.Vehicle;

import static ch.sbb.matsim.contrib.railsim.rl.utils.RLUtils.getBufferTip;
import static ch.sbb.matsim.contrib.railsim.rl.utils.RLUtils.isSwitchable;

class OtherAgent {
	// Distance of agent to other agent's tail position if the train is moving in the same direction
	//	Otherwise, distance to BufferTip position if moving in opposite direction
	double distance;
	List<Double> bufferTipPosition;
	List<Double> tailPosition;
	double speed;

	public OtherAgent(double distance, List<Double> bufferTipPosition, List<Double> tailPosition, double speed, boolean sameDirection) {
		this.distance = distance;
		this.bufferTipPosition = bufferTipPosition;
		this.tailPosition = tailPosition;
		this.speed = speed;
		this.sameDirection = sameDirection;
	}

	boolean sameDirection;

}

public class TreeObservation {
	private final TrainState position;
	private final RailResourceManager resources;
	private final Network network;
	private List<ObservationTreeNode> observationList;
	private List<Double> flattenedObservation;
	int depth;
	Map<Id<TransitRoute>, List<Id<Link>>> transitRouteStops;


	public TreeObservation(TrainState position, RailResourceManager resources, Network network, int depth) {
		this.resources = resources;
		this.position = position;
		this.network = network;
		this.observationList = new ArrayList<>();
		this.flattenedObservation= new ArrayList<>();
		this.depth = depth;
		this.transitRouteStops = new HashMap<>();
		createTreeObs(this.depth);

	}

	private void getStopsOnTransitLine(TransitRoute transitRoute){
		// Calculate the stops on a particular transit line and store it in a map

		List<Id<Link>> stops = new ArrayList<>();
		// get any departure on the transitline
		Departure departure = transitRoute.getDepartures().values().stream().collect(Collectors.toList()).get(0);
		for (TransitRouteStop stop: transitRoute.getStops() ){
			stops.add(stop.getStopFacility().getLinkId());
		}
		this.transitRouteStops.put(transitRoute.getId(), stops);
	}
	private boolean isObsTreeNode(Node toNode){

		// Boolean to check if the toNode is halt position of train
		boolean isStop = false;

		// get vehicle Id
		Id<Vehicle> trainId = position.getPt().getPlannedVehicleId();

		// use transit route to get departures for the train
		TransitRoute transitRoute = position.getPt().getTransitRoute();

		// calculate stops in this route
		if (!transitRouteStops.containsKey(transitRoute.getId())){
			getStopsOnTransitLine(transitRoute);
		}
		List<Id<Link>> stopsList = transitRouteStops.get(transitRoute.getId());

		// Iterate through all the stops of the transitLine
		for (Id<Link> linkId : stopsList){
			// if toNode(stopLink) == toNode, mark the toNode to be a halt
			if (network.getLinks().get(linkId).getToNode().equals(toNode)){
				isStop = true;
				break;
			}
		}

//		if (position.getPt().getNextTransitStop() != null){
//			// ensure that there is a nextTransitStop. On the final destination
//			// nextTransitStop is null
//			Node nextHaltToNode = network.getLinks().get(position.getPt().getNextTransitStop().getLinkId()).getToNode();
//			if (nextHaltToNode.equals(toNode))
//				isStop = true;
//		}
//
//		// toNode is the end of the track with just one outLink in opposite direction
//		if (toNode.getOutLinks().size()==1)
//			isStop = true;

		// Check if the node is a switch node. A switch node must have more than .
		boolean switch_node = toNode.getOutLinks().size() > 2;
		if (isStop || switch_node){
			return true;
		}
		return false;
	}

	private ObservationTreeNode createObservatioNode(TrainPosition train, RailLink curLink, Node node, OtherAgent sameDirAgent, OtherAgent oppDirAgent){

		// Get coordinates of the nextNode
		List<Double> nodePosition  = new ArrayList<Double>(Arrays.asList(node.getCoord().getX(), node.getCoord().getY()));

		// Get coordinates of the train head
		Node toNodeCurLink = getToNode(curLink);
		List<Double> toNodeCurLinkPosition = new ArrayList<Double>(Arrays.asList(toNodeCurLink.getCoord().getX(), toNodeCurLink.getCoord().getY()));

		//calculate distance of train to nextNode
		double distNodeAgent = curLink.length - train.getHeadPosition()+RLUtils.calculateEuclideanDistance(nodePosition, toNodeCurLinkPosition);

		double distNextHalt = 0;
		if (position.getPt().getNextTransitStop() != null){
			// next transit stop will be null at the end of the route
			Node nextHaltToNode = network.getLinks().get(position.getPt().getNextTransitStop().getLinkId()).getToNode();
			List<Double> nextHaltToNodePosition = new ArrayList<Double>(Arrays.asList(nextHaltToNode.getCoord().getX(), nextHaltToNode.getCoord().getY()));
			distNextHalt = RLUtils.calculateEuclideanDistance(nodePosition, nextHaltToNodePosition);
		}


		int isSwitchable = isSwitchable(node, curLink, network) ? 1 : 0;

		int numParallelIncomingTracks = node.getInLinks().size();

		return new ObservationTreeNode(nodePosition,distNodeAgent, distNextHalt, isSwitchable, sameDirAgent, oppDirAgent, numParallelIncomingTracks, node.getId());

	}

	private List<Double> flattenObservationNode(ObservationTreeNode obsNode) {

		List<Double> flattenedNode = new ArrayList<>();

		flattenedNode.addAll(obsNode.position);
		flattenedNode.add((double) obsNode.numParallelTracks);
		flattenedNode.add(obsNode.distNodeStop);
		flattenedNode.add(obsNode.distNodeAgent);
		flattenedNode.add((double)obsNode.isSwitchable);

		return flattenedNode;
	}

	private void createTreeObs(int depth){
		// Get the link of the tip of the buffer
		RailLink bufferTipLink = getBufferTip(resources, position);
		if (bufferTipLink == null){
			// if there is no buffer because there is no reserved distance then
			// assume bufferTipLink to be the headLink
			bufferTipLink = resources.getLink(position.getHeadLink());
		}

		// Get the toNode and fromNode of the bufferTipLink
		Node toNodeBufferTipLink = getToNode(bufferTipLink);
		Node fromNodeBufferTipLink = network.getLinks().get(bufferTipLink.getLinkId()).getFromNode();

		List<Node> exploreQueue = new ArrayList<>();

		// store a list of visitedNodes to avoid infinite loop in case of cycles in the network.
		List<Node> visitedNodes = new ArrayList<>();

		// the added node may not necessarily be an observation tree node
		exploreQueue.add(toNodeBufferTipLink);

		// If the "toNodeBufferTipLink" is not an observation tree node then increase the depth by 1
		if(!isObsTreeNode(toNodeBufferTipLink))
			depth +=1;

		for (int i = 0; i < depth; i++) {
			// Level Traversal algorithm
			int lenExploreQueue = exploreQueue.size();
			while (lenExploreQueue > 0) {
				// Level traversal
				Node curNode = exploreQueue.get(0);
				exploreQueue.remove(0);

				// Create observationTreeNode from the curNode
				if(isObsTreeNode(curNode)){
					// if the exploredNode fits the criteria of observation tree node, then
					// add the node in the ObservationTree list
					ObservationTreeNode obsNode = createObservatioNode(position, resources.getLink(position.getHeadLink()), curNode, null, null);
					this.observationList.add(obsNode);
					this.flattenedObservation.addAll(flattenObservationNode(obsNode));
				}
				visitedNodes.add(curNode);

				// Look for switches/intersections/stops on the branches stemming out of the current switch
				List<Node> nextNodes = getNextNodes(fromNodeBufferTipLink, curNode);

				// Add nextNode only if it's not already visited
				for (Node nextNode : nextNodes) {
					if (!visitedNodes.contains(nextNode)){
						exploreQueue.add(nextNode);
					}
				}
				lenExploreQueue -= 1;
			}
		}
	}

	private TrainPosition getClosestTrainOnPathR(Node curNode, Node nextNode) throws NotImplementedException{
		throw new NotImplementedException();
	}
	private TrainPosition getClosestTrainOnPathF(Node curNode, Node nextNode) throws NotImplementedException {
		throw new NotImplementedException();
	}

 	private List<Node> getNextNodes(Node fromNodeBufferTipLink, Node obsNode) {
		// next switches
		List<Node> obsTreeNodes = new ArrayList<>();

		// check all outgoing links from the current obsNode
		for (Link outLink : obsNode.getOutLinks().values()) {

			boolean reverseDirection = false;
			Node nextNode = outLink.getToNode();

			Node prevNode = obsNode;

			// traverse the track until a halt/final destination/switch is reached
			while (!isObsTreeNode(nextNode)) {
				// get the single outgoing link and follow it
				// TODO: Handle end of network, will throw NoSuchElementException at the moment
				if (nextNode.equals(fromNodeBufferTipLink)){
					reverseDirection =true;
					break;
				}
				List<Link> outLinks = nextNode.getOutLinks().values().stream().collect(Collectors.toList());

				// At the final stop, there will be just 1 outlink
				Link nextLink = outLinks.get(0);

				if (outLinks.size()>1){
					// update the nextLink to point the right direction if there are more than 1 outlink
					for(Link link : outLinks){
						// skip the link that leads to prevNode to avoid an infinite loop
						if (link.getToNode().equals(prevNode)){
							continue;
						}
						else{
							nextLink = link;
							break;
						}
					}
					// update prevNode
					prevNode = nextNode;

					// update nextNode
					nextNode = nextLink.getToNode();

					if (nextNode.equals(fromNodeBufferTipLink)){
						reverseDirection =true;
						break;
					}
				}else{
					// nextNode is the final stop with just 1 outgoing link
					break;
				}
			}

			if (reverseDirection || nextNode.equals(fromNodeBufferTipLink)){
				// skip the current outLink as this link from the switchNode leads to the observing train
				continue;
			}
			obsTreeNodes.add(nextNode);
		}
		return obsTreeNodes;
	}

	private Node getToNode(RailLink link) {
		return network.getLinks().get(link.getLinkId()).getToNode();
	}

	public List<ObservationTreeNode> getObservationTree() {
		return observationList;
	}

	public List<Double> getFlattenedObservationTree() {
		return this.flattenedObservation;
	}
}
