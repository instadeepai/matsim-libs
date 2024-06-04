package ch.sbb.matsim.contrib.railsim.rl.utils;

import ch.sbb.matsim.contrib.railsim.qsimengine.TrainState;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailLink;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.events.handler.BasicEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RLUtils {

	public static Node getToNode(Network network, RailLink link) {
		return network.getLinks().get(link.getLinkId()).getToNode();
	}

//	TODO: implement the functions: getSwitchNodeOnTrack and updateRoute
	public static boolean getPathToSwitchNodeOnTrack(Link curLink,  Node target, List<RailLink> path){

		/*
		get links from a node to the next switch node on the same track.
		l0 St l1 l2 l3 l4 Sw
		path = l1, l2, l3, l4

		If target node found in the path, return true else false
		 */
		Node start = curLink.getToNode();
		while ( start.getOutLinks().values().size() <= 2){
			Link nextLink = null;

			// get outLinks from the start node
			List<Link> outLinks = start.getOutLinks().values().stream().collect(Collectors.toList());
			assert (outLinks.size() == 2);

			// get the fromNode of the curLink
			Node fromNodeCurLink = curLink.getFromNode();

			for (Link outLink : outLinks){
				if (outLink.getToNode().equals(fromNodeCurLink)){
					// Ignore the link where outLink(start) == curLink
					continue;
				}
				else {
					nextLink = outLink;
					break;
				}
			}
			path.add(new RailLink(nextLink));

			// update start node and curLink
			start = nextLink.getToNode();
			curLink = nextLink;
			if (target != null && start.equals(target)) {
				// target found in the path
				return true;
			}
		}
		// no path found to the target
		return false;
	}
	public static void updateRoute(Network network, TrainState train, Node nextObsNode){

		// Get the last link in the route
		RailLink lastLinkInRoute = train.route.get(train.route.size() -1);

		// get the toNode of the lastLinkInRoute
		Node toNodeLastLinkInRoute = getToNode(network, lastLinkInRoute);

		// get the fromNode of the lastLinkInRoute
		Node fromNodeLastLinkInRoute = network.getLinks().get(lastLinkInRoute.getLinkId()).getFromNode();

		// get the outLinks from the toNode of the lastLinkInRoute
		List<Link> nextLinks = toNodeLastLinkInRoute.getOutLinks().values().stream().collect(Collectors.toList());

		//path to the nextObsNode
		List<RailLink> path = null;
		for (Link nextLink : nextLinks){

			// skip the link that takes back on the same track
			if (nextLink.getToNode().equals(fromNodeLastLinkInRoute))
				continue;

			// To store the path from lastLinkInRoute to node connecting the nextObsNode
			 path = new ArrayList<>();
			if (getPathToSwitchNodeOnTrack(network.getLinks().get(lastLinkInRoute.getLinkId()), nextObsNode, path))
				break;
		}

		assert path != null;
		train.route.addAll(path);
	}

	public static double calculateEuclideanDistance(List<Double> point1, List<Double> point2) {
		if (point1.size() != point2.size()) {
			throw new IllegalArgumentException("Points must have the same number of dimensions");
		}

		double sumOfSquares = 0.0;
		for (int i = 0; i < point1.size(); i++) {
			double diff = point1.get(i) - point2.get(i);
			sumOfSquares += diff * diff;
		}

		return Math.sqrt(sumOfSquares);
	}

	public static double calculateAngle(Node point1, Node point2, Node point3) {
		double x1 = point1.getCoord().getX();
		double y1 = point1.getCoord().getY();
		double x2 = point2.getCoord().getX();
		double y2 = point2.getCoord().getY();
		double x3 = point3.getCoord().getX();
		double y3 = point3.getCoord().getY();

		// Calculate the vectors between the points
		double vector1x = x2 - x1;
		double vector1y = y2 - y1;
		double vector2x = x3 - x2;
		double vector2y = y3 - y2;

		// Calculate the dot product
		double dotProduct = (vector1x * vector2x) + (vector1y * vector2y);

		// Calculate the magnitudes of the vectors
		double magnitude1 = Math.sqrt(vector1x * vector1x + vector1y * vector1y);
		double magnitude2 = Math.sqrt(vector2x * vector2x + vector2y * vector2y);

		// Calculate the angle in radians
		double angleInRadians = Math.acos(dotProduct / (magnitude1 * magnitude2));

		// Convert radians to degrees
		double angleInDegrees = Math.toDegrees(angleInRadians);

		return angleInDegrees;
	}



	public static Boolean isSwitchable(Node switchNode, RailLink curLink, Network network){

		int numOutGoingLinks = switchNode.getOutLinks().size();
		if (numOutGoingLinks <= 2){
			return false;
		}
		else{
			List<Link> outLinks = switchNode.getOutLinks().values().stream().collect(Collectors.toList());
			List<Node> toNodeOfOutLinkList = new ArrayList<>();
			for (Link link: outLinks){
				toNodeOfOutLinkList.add(link.getToNode());
			}

			Node fromNodeCurLink = network.getLinks().get(curLink.getLinkId()).getFromNode();

			int possibleDirections = 0;
			for (Node toNodeOfOutLink: toNodeOfOutLinkList){
				double angle = calculateAngle(fromNodeCurLink, switchNode, toNodeOfOutLink);
				if (angle < 90.0 && angle > -90.0)
					possibleDirections ++;
			}
			if (possibleDirections > 1)
				return true;
			else
				return false;
		}
	}

}
