package ch.sbb.matsim.contrib.railsim.rl.observation;

import java.util.Collections;
import java.util.Map;

public class StepOutput{
	Observation observation;
	double reward;
	boolean terminated;

	public Observation getObservation() {
		return observation;
	}

	public void setObservation(Observation observation) {
		this.observation = observation;
	}

	public double getReward() {
		return reward;
	}

	public void setReward(double reward) {
		this.reward = reward;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

	public boolean isTruncated() {
		return truncated;
	}

	public void setTruncated(boolean truncated) {
		this.truncated = truncated;
	}

	public Map<String, String> getInfo() {
		return info;
	}

	public void setInfo(Map<String, String> info) {
		this.info = info;
	}

	boolean truncated;
	Map<String, String> info;

	@Override
	public String toString() {
		return "StepOutput{" +
			"observation=" + observation +
			", reward=" + reward +
			", terminated=" + terminated +
			", truncated=" + truncated +
			", info=" + info +
			'}';
	}

	public StepOutput(int depthObservationTree, boolean random){
		if (random){
			this.observation = new Observation(depthObservationTree, random);
			this.reward = 0;
			this.terminated = false;
			this.truncated = false;
			this.info = Collections.emptyMap();
		}
	}

	public StepOutput(){

	}


}
