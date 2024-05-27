package ch.sbb.matsim.contrib.railsim.rl.observation;

import java.util.*;

public class Observation{
	//TODO: Add timestamp as well in the observation
	List<Double> obsTree = new ArrayList<>();
	//
	List<Double> trainState = new ArrayList<Double>(4);
	// x and y coordinate of the node
	List<Double> positionNextNode = new ArrayList<Double>(2);

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	int timestamp;
	public List<Double> getObsTree() {
		return obsTree;
	}

	public void setObsTree(List<Double> obsTree) {
		this.obsTree = obsTree;
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
			"obsTree=" + obsTree +
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
			this.obsTree.add(Math.random());
		}
	}
	public Observation(double depthObservationTree, boolean random){

		if (random){
			this.generateRandomObservation(depthObservationTree);
		}
	}
}


