package ch.sbb.matsim.contrib.railsim.rl.utils;

import ch.sbb.matsim.contrib.railsim.qsimengine.RailsimCalc;
import ch.sbb.matsim.contrib.railsim.qsimengine.TrainPosition;
import ch.sbb.matsim.contrib.railsim.qsimengine.TrainState;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailLink;
import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailResourceManager;
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


	public static boolean getPathToSwitchNodeOnTrack(Link curLink, Node target, List<RailLink> path, RailResourceManager resources, Network network){
		/**
		get links from a node to the next switch node on the same track.
		l0 St l1 l2 l3 l4 Sw
		path = l1, l2, l3, l4

		If target node found in the path, return true else false

		**/

		// In the beginning there will be just one link (corresponding to the start link)
		// specified in the route of each train. However, the route of the train has a duplicated entry
		// of the entry link. Therefore, min(path.size()) = 2 at the time of start of departure.

		if (curLink.getToNode().equals(target))
			return true;

		if (path.size()==2 && path.get(0).equals(path.get(1))){
			// remove the duplicate entry link for the train
			path.remove(1);
		}

		List<Link> outTracks = getOutTracks(curLink, network);
		while (outTracks.size()==1){
			Link nextTrackLink = outTracks.get(0);

			// convert Link to Raillink and append the link to path
			RailLink convertedTrackLink = resources.getLink(nextTrackLink.getId());
			path.add(convertedTrackLink);

			outTracks = getOutTracks(nextTrackLink, network);

			if (nextTrackLink.getToNode().equals(target))
				return true;

		}
		return false;
	}

	public static void updateRoute(Network network, TrainState train, Node nextObsNode, RailResourceManager resources){

		// Get the last link in the route
		RailLink lastLinkInRoute = train.route.get(train.route.size() -1);

		// get the outLinks from the toNode of the lastLinkInRoute
		List<Link> nextTracks = getOutTracks(network.getLinks().get(lastLinkInRoute.getLinkId()), network);

		//path to the nextObsNode
		List<RailLink> path = null;
		for (Link nextLink : nextTracks){
			// To store the path from lastLinkInRoute to node connecting the nextObsNode
			 path = new ArrayList<>();
			if (getPathToSwitchNodeOnTrack(network.getLinks().get(lastLinkInRoute.getLinkId()), nextObsNode, path, resources, network))
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

		int numOutGoingTracks = getOutTracks(network.getLinks().get(curLink.getLinkId()), network).size();
		if (numOutGoingTracks <= 1){
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

	public static RailLink getBufferTip(RailResourceManager resources, TrainState position ) {
		double reserveDist = RailsimCalc.calcReservationDistance(position, resources.getLink(position.getHeadLink()));
		RailLink currentLink = resources.getLink(position.getHeadLink());
		List<RailLink> reservedSegment = RailsimCalc.calcLinksToBlock(position, currentLink, reserveDist);
		// if no track could be reserved return null
		if (reservedSegment.size()==0)
			return null;
		else{
			RailLink bufferTip = reservedSegment.get(reservedSegment.size() - 1);
			return bufferTip;
		}
	}

	public static List<Link> getOutTracks(Link link, Network network){

		List<Link> outTracks =  new ArrayList<>();

		Node toNode = link.getToNode();
		if (toNode.getOutLinks() != null){
			for (Link l : toNode.getOutLinks().values()){
				if (l.getToNode().equals(link.getFromNode())){
					// skip the outlink that leads backward in a bi-directional network
					continue;
				}else{
					outTracks.add(l);
				}
			}
		}
		return outTracks;
	}

	public static List<Link> getMergingTracks(Link link){
		List<Link> inTracks = new ArrayList<>();

		Node toNode = link.getToNode();
		if (toNode.getInLinks() != null){
			for(Link l: toNode.getInLinks().values()){
				if (l.equals(link)){
					continue;
				}else{
					inTracks.add(l);
				}
			}
		}

		return inTracks;
	}

}
