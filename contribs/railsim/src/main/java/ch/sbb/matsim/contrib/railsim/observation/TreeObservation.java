package ch.sbb.matsim.contrib.railsim.observation;

import ch.sbb.matsim.contrib.railsim.qsimengine.RailsimCalc;
import ch.sbb.matsim.contrib.railsim.qsimengine.TrainPosition;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailLink;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailResource;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailResourceManager;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.ResourceState;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
// TODO: What is the time resolution of the environment? 1 iteration is equal to home many seconds in rlsim?
// The time resolution of matsim is seconds, same for railsim. But there are seconds where nothing happens, no events in update queue.
import java.util.List;
import java.util.ArrayList;

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

class ObservationNode {
	Node node;
	double distNodeAgent;
	double distNodeStop;
	boolean isSwitchable;
	OtherAgent sameDirAgent;
	OtherAgent oppDirAgent;
	int numParallelTracks;
}

public class TreeObservation {
	private final TrainPosition position;
	private final RailResourceManager resources;
	private final Network network;
	private List<ObservationNode> observation;

	public TreeObservation(TrainPosition position, RailResourceManager resources, Network network) {
		this.resources = resources;
		this.position = position;
		this.network = network;
		createTreeObs();
	}

	public RailLink getBufferTip() {
		double reserveDist = RailsimCalc.calcReservationDistance(position, resources.getLink(position.getHeadLink()));
		RailLink currentLink = resources.getLink(position.getHeadLink());
		List<RailLink> reservedSegment = RailsimCalc.calcLinksToBlock(position, currentLink, reserveDist);
		// TODO Verify if the links are added in the list in the sequence of occurence.
		RailLink bufferTip = reservedSegment.get(reservedSegment.size() - 1);

		return bufferTip;
	}

	private void createTreeObs() {
		int depth = 3;
		createTreeObs(depth);
	}

	// TODO: Handle cases when the agent should also see the next 2 halts. - But this may not be needed as the agent can be penalised
	//  if the agent is unable to reach the next station.
	private void createTreeObs(int depth) {
//		TODO: This has to be fixed such that the next observation node is calculated from the current position

		// Get the link of the tip of the buffer
		RailLink bufferTipLink = getBufferTip();

		// Get the toNode of the bufferTipLink
		Node toNode = getToNode(bufferTipLink);
		List<Node> exploreQueue = new ArrayList<Node>();

		List<Node> obsNode = getNextNodes(toNode);
		exploreQueue.addLast(obsNode);

		for (int i = 0; i < depth; i++) {
			// Depth traversal: Making an observation Tree of fixed depth
			int lenExploreQueue = exploreQueue.size();
			while (lenExploreQueue > 0) {
				// Level traversal
				Node curNode = exploreQueue.getFirst();
				exploreQueue.remove(0);

				// Look for switches/intersections/stops on the branches stemming out of the current switch
				List<Node> nextNodes = getNextNodes(curNode);

				for (Node nextNode : nextNodes) {
					exploreQueue.addLast(nextNode);
					TrainPosition trainF = getClosestTrainOnPathF(curNode, nextNode);
					TrainPosition trainR = getClosestTrainOnPathR(curNode, nextNode);
					observation.add(createObservatioNode(nextNode, trainF, trainR));
				}
				lenExploreQueue -= 1;
			}
		}
	}

	private TrainPosition getClosestTrainOnPathR(Node curNode, Node nextNode) {
		// complete route to current position of the train from schedule, if needed?
		List<RailLink> previousRoute = position.getRoute(0, position.getRouteIndex());

		// TODO: If all opposite trains are needed, follow the inLinks of curNode until the nextNode is reached. Store the of the path links.
		List<Link> path = null;

		// check for each link if there is capacity
		for (Link link : path) {
			RailLink railLink = resources.getLink(link.getId());
			RailResource resource = railLink.getResource();
			ResourceState state = resource.getState(railLink);
			// TODO: Ask Christian how we get the train position of the nearest train.
		}

		return null;
	}

	private TrainPosition getClosestTrainOnPathF(Node curNode, Node nextNode) {
		// complete route from current position of the train from schedule, if needed?
		List<RailLink> upcomingRoute = position.getRoute(position.getRouteIndex(), position.getRouteSize());

		// TODO: If all opposite trains are needed, follow the outLinks of curNode until the nextNode is reached. Store the of the path links.
		List<Link> path = null;

		// same procedure as above...

		return null;
	}

//	TODO: Add the logic to also consider halts/stops
 	private List<Node> getNextNodes(Node curNode) {
		// next switches
		List<Node> switchNodes = new ArrayList<>();

		// check all outgoing links from the current node
		for (Link outLink : curNode.getOutLinks().values()) {
			Node nextNode = outLink.getToNode();

			// follow nodes with only one outgoing link until a switch is reached
			while (nextNode.getOutLinks().size() == 1) {
				// get the single outgoing link and follow it
				// TODO: Handle end of network, will throw NoSuchElementException at the moment
				nextNode = nextNode.getOutLinks().values().iterator().next().getToNode();
			}

			// if the next node has more than one outgoing link, it's a switch
			if (nextNode.getOutLinks().size() > 1) {
				switchNodes.add(nextNode);
			}
		}

		return switchNodes;
	}

	private Node getToNode(RailLink bufferTipLink) {
		return network.getLinks().get(bufferTipLink.getLinkId()).getToNode();
	}

	public List<ObservationNode> getObservation() {
		return observation;
	}
}
