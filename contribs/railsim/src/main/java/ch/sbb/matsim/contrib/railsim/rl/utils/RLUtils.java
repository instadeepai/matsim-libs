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
	private boolean getSwitchNodeOnTrack(Network network, Node start, Node target, List<RailLink> path){

		/*
		get links from a node to the switch node on the same track
		l0 St l1 l2 l3 l4 Sw
		path = l1, l2, l3, l4
		 */

		while (!start.equals(target) || (start.getOutLinks().values().size() > 2)){

			// Get outgoing node from the start node
			List<Link> outLinks = start.getOutLinks().values().stream().collect(Collectors.toList());

			//
			for (Link link: outLinks){
				if new RailLink(link) in path
			}
			path.add(new RailLink(temp));
			start = temp.getToNode();
			if (start.equals(target))
				return true;
		}

	}
	public static void updateRoute(Network network, TrainState train, Node nextObsNode){


		// Calculate path until nextObsNode

		// Get the last link in the route
		RailLink lastLinkInRoute = train.route.get(train.route.size() -1);

		// get the toNode of the lastLinkInRoute
		Node nextNode = getToNode(network, lastLinkInRoute);

		// To store the path from lastLinkInRoute to node connecting the nextObsNode
		List<RailLink> path = new ArrayList<>();


		if (isSwitchable(nextNode, lastLinkInRoute, network)){
			// the nextNode is not a switch Node

		}
		else{
			// iterate through all possible directions searching for the nextObsNode

		}


		// update route of the current train
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
			List<? extends Link> outLinks = switchNode.getOutLinks().values().stream().toList();
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
