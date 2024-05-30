package ch.sbb.matsim.contrib.railsim.rl.observation;

import java.util.*;

public class Observation{
	double railsim_timestamp;

	List<Double> flattenedObsTree = new ArrayList<>();

	public Observation() {

	}

	public List<ObservationTreeNode> getObsTree() {
		return obsTree;
	}

	public void setObsTree(List<ObservationTreeNode> obsTree) {
		this.obsTree = obsTree;
	}

	List<ObservationTreeNode> obsTree= new ArrayList();
	//
	List<Double> trainState = new ArrayList<Double>(4);
	// x and y coordinate of the node
	List<Double> positionNextNode = new ArrayList<Double>(2);

	public double getRailsim_timestamp() {
		return railsim_timestamp;
	}

	public void setRailsim_timestamp(double railsim_timestamp) {
		this.railsim_timestamp = railsim_timestamp;
	}

	int timestamp;
	public List<Double> getFlattenedObsTree() {
		return flattenedObsTree;
	}

	public void setFlattenedObsTree(List<Double> flattenedObsTree) {
		this.flattenedObsTree = flattenedObsTree;
	}

	public List<Double> getTrainState() {
		return trainState;
	}

	public void setTrainState(List<Double> trainState) {
		this.trainState = trainState;
	}

	public List<Double> getPositionNextNode() {
		return positionNextNode;
	}

	public void setPositionNextNode(List<Double> positionNextNode) {
		this.positionNextNode = positionNextNode;
	}


	@Override
	public String toString() {
		return "Observation{" +
			"obsTree=" + flattenedObsTree +
			", trainState=" + trainState +
			", positionNextNode=" + positionNextNode +
			'}';
	}

	public void generateRandomObservation(double depthObservationTree){
		for (int i= 0; i<4; i++){
			this.trainState.add(i, Math.random());
		}
		for (int i= 0; i<2; i++){
			this.positionNextNode.add(i, Math.random());
		}
		int lenObsTree = (int)(Math.pow(2.0, depthObservationTree+1)-1)*17;
		for (int i=0; i<lenObsTree; i++){
			this.flattenedObsTree.add(Math.random());
		}
	}
	public Observation(double depthObservationTree, boolean random){

		if (random){
			this.generateRandomObservation(depthObservationTree);
		}
	}
}


