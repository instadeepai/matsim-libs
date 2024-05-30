package ch.sbb.matsim.contrib.railsim.rl.observation;

import ch.sbb.matsim.contrib.railsim.qsimengine.RailsimCalc;
import ch.sbb.matsim.contrib.railsim.qsimengine.TrainPosition;
import ch.sbb.matsim.contrib.railsim.qsimengine.TrainState;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailLink;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailResourceManager;
import org.apache.commons.lang3.NotImplementedException;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import ch.sbb.matsim.contrib.railsim.rl.utils.RLUtils;

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
	public TreeObservation(TrainState position, RailResourceManager resources, Network network, int depth) {
		this.resources = resources;
		this.position = position;
		this.network = network;
		this.observationList = new ArrayList<>();
		this.flattenedObservation= new ArrayList<>();
		this.depth = depth;
		createTreeObs(this.depth);
	}

	private RailLink getBufferTip() {
		double reserveDist = RailsimCalc.calcReservationDistance(position, resources.getLink(position.getHeadLink()));
		RailLink currentLink = resources.getLink(position.getHeadLink());
		List<RailLink> reservedSegment = RailsimCalc.calcLinksToBlock(position, currentLink, reserveDist);
		// TODO Verify if the links are added in the list in the sequence of occurence.
		RailLink bufferTip = reservedSegment.get(reservedSegment.size() - 1);

		return bufferTip;
	}


	private boolean isObsTreeNode(Node toNode){

		// Check if the node is halt position of train
		boolean isStop = false;
		//TODO: This would fail if more than one halts lie in the observation tree. The code would recognise just one.
		Node nextHaltToNode = network.getLinks().get(position.getPt().getNextTransitStop().getLinkId()).getToNode();
		if (nextHaltToNode.equals(toNode))
			isStop = true;

		// Check if the node is a switch node. Each node will have minimum two outgoing nodes.
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
		double distNodeAgent = train.getHeadPosition()+RLUtils.calculateEuclideanDistance(nodePosition, toNodeCurLinkPosition);

		Node nextHaltToNode = network.getLinks().get(position.getPt().getNextTransitStop().getLinkId()).getToNode();
		List<Double> nextHaltToNodePosition = new ArrayList<Double>(Arrays.asList(nextHaltToNode.getCoord().getX(), nextHaltToNode.getCoord().getY()));

		double distNextHalt = RLUtils.calculateEuclideanDistance(nodePosition, nextHaltToNodePosition);

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
		RailLink bufferTipLink = getBufferTip();

		// Get the toNode of the bufferTipLink
		Node toNodeBufferTipLink = getToNode(bufferTipLink);
		List<Node> exploreQueue = new ArrayList<>();

		// store a list of visitedNodes to avoid infinite loop in case of cycles in the network.
		List<Node> visitedNodes = new ArrayList<>();

		while(!isObsTreeNode(toNodeBufferTipLink)){
			toNodeBufferTipLink = toNodeBufferTipLink.getOutLinks().values().iterator().next().getToNode();
		}
		exploreQueue.add(toNodeBufferTipLink);

		for (int i = 0; i < depth; i++) {
			// Level Traversal algorithm
			int lenExploreQueue = exploreQueue.size();
			while (lenExploreQueue > 0) {
				// Level traversal
				Node curNode = exploreQueue.get(0);

				// Create observationTreeNode from the curNode
//				TrainPosition trainF = getClosestTrainOnPathF(curNode, nextNode);
//				TrainPosition trainR = getClosestTrainOnPathR(curNode, nextNode);
				ObservationTreeNode obsNode = createObservatioNode(position, resources.getLink(position.getHeadLink()), curNode, null, null);
				this.observationList.add(obsNode);
				this.flattenedObservation.addAll(flattenObservationNode(obsNode));

				exploreQueue.remove(0);
				visitedNodes.add(curNode);

				// Look for switches/intersections/stops on the branches stemming out of the current switch
				List<Node> nextNodes = getNextNodes(toNodeBufferTipLink, curNode);

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

 	private List<Node> getNextNodes(Node toNodeBufferTipLink, Node obsNode) {
		// next switches
		List<Node> obsTreeNodes = new ArrayList<>();

		// check all outgoing links from the current obsNode
		for (Link outLink : obsNode.getOutLinks().values()) {

			boolean reverseDirection = false;
			Node nextNode = outLink.getToNode();

			Node prevNode = obsNode;
			// follow nodes with only one outgoing link until a switch or a halt is reached
			while (!isObsTreeNode(nextNode)) {
				// get the single outgoing link and follow it
				// TODO: Handle end of network, will throw NoSuchElementException at the moment

				List<Link> outLinks = nextNode.getOutLinks().values().stream().collect(Collectors.toList());
				Link nextLink = null;
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

				if (nextNode.equals(toNodeBufferTipLink)){
					reverseDirection =true;
					break;
				}
			}

			if (reverseDirection){
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
