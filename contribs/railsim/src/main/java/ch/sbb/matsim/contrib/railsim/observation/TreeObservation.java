//package ch.sbb.matsim.contrib.railsim.qsimengine.observation;
//import ch.sbb.matsim.contrib.railsim.qsimengine.RailsimCalc;
//import ch.sbb.matsim.contrib.railsim.qsimengine.TrainPosition;
//import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailLink;
//import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailResource;
//import ch.sbb.matsim.contrib.railsim.qsimengine.resources.RailResourceManager;
//import org.matsim.api.core.v01.network.Node;
//// TODO: What is the time resolution of the environment?
//// TODO: Is there any EVENT already created when the RL agent should be called? If not then we need to crate this event.
//// TODO: Need help with creating these additional APIs
//import java.util.List;
//import java.util.ArrayList;
//class OtherAgent{
//	// Distance of agent to other agent's tail position if the train is moving in the same direction
//	//	Otherwise, distance to BufferTip position if moving in opposite direction
//	double distance;
//	List<Double> bufferTipPosition;
//	List<Double> tailPosition;
//	double speed;
//
//	public OtherAgent(double distance, List<Double> bufferTipPosition, List<Double> tailPosition, double speed, boolean sameDirection) {
//		this.distance = distance;
//		this.bufferTipPosition = bufferTipPosition;
//		this.tailPosition = tailPosition;
//		this.speed = speed;
//		this.sameDirection = sameDirection;
//	}
//
//	boolean sameDirection;
//
//}
//class ObservationNode{
//	Node node;
//	double distNodeAgent;
//	double distNodeStop;
//	boolean isSwitchable;
//	OtherAgent sameDirAgent;
////	OtherAgent oppDirAgent;
//}
//
//public class TreeObservation {
//	private TrainPosition position;
//	private RailResourceManager resources;
//	private List<ObservationNode> observation;
//	public TreeObservation(TrainPosition position, RailResourceManager resources){
//		this.resources = resources;
//		this.position = position;
//		createTreeObs();
//	}
//
//	public RailLink getBufferTip(){
//		double reserveDist = RailsimCalc.calcReservationDistance(position, resources.getLink(position.getHeadLink()));
//		RailLink currentLink = resources.getLink(position.getHeadLink());
//		List<RailLink> reservedSegment = RailsimCalc.calcLinksToBlock(position, currentLink, reserveDist);
//		// TODO Verify if the links are added in the list in the sequence of occurence.
//		RailLink bufferTip  = reservedSegment.get(reservedSegment.size() -1);
//
//		return bufferTip;
//	}
//
//	private void createTreeObs(){
//		int depth =3;
//		createTreeObs(depth);
//	}
//
//// TODO: Handle cases when the agent should also see the next 2 halts. - But this may not be needed as the agent ca be penalised
////  if the agent is unable to reach the next station.
//	private void createTreeObs(int depth){
//		// Get the link of the tip of the buffer
//		RailLink bufferTipLink = getBufferTip();
//
//		// Get the toNode of the bufferTipLink
//		Node toNode = getToNode(bufferTipLink);
//		List<Node> exploreQueue = new ArrayList<Node> ();
//
//		exploreQueue.addLast(toNode);
//		for (int i=0; i< depth; i++){
//			// Depth traversal: Making an observation Tree of fixed depth
//			int lenExploreQueue = exploreQueue.size();
//			while (lenExploreQueue > 0){
//				// Level traversal
//				Node curNode = exploreQueue.getFirst();
//				exploreQueue.remove(0);
//
//				// Look for switches/intersections/stops on the branches stemming out of the current switch
//				List<Node> nextNodes = getNextNodes(curNode);
//
//				for (Node nextNode: nextNodes){
//					exploreQueue.addLast(nextNode);
//					TrainPosition trainF = getClosestTrainOnPathF(curNode, nextNode);
//					TrainPosition trainR = getClosestTrainOnPathR(curNode, nextNode);
//					observation.add(createObservatioNode(nextNode, trainF, trainR));
//				}
//				lenExploreQueue -= 1;
//			}
//		}
//	}
//
//	public List<ObservationNode> getObservation(){
//		return observation;
//	}
//}
