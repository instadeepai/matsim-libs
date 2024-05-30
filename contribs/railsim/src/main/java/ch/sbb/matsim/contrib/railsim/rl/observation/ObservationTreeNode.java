package ch.sbb.matsim.contrib.railsim.rl.observation;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Node;

import java.util.List;

public class ObservationTreeNode {

	Id<Node> nodeId;
	List<Double> position;
	double distNodeAgent;
	double distNodeStop;
	int isSwitchable;
	OtherAgent sameDirAgent;
	OtherAgent oppDirAgent;
	int numParallelTracks;

	public Id<Node> getNodeId() {
		return nodeId;
	}

	public void setNodeId(Id<Node> nodeId) {
		this.nodeId = nodeId;
	}

	public List<Double> getPosition() {
		return position;
	}

	public void setPosition(List<Double> position) {
		this.position = position;
	}

	public double getDistNodeAgent() {
		return distNodeAgent;
	}

	public void setDistNodeAgent(double distNodeAgent) {
		this.distNodeAgent = distNodeAgent;
	}

	public double getDistNodeStop() {
		return distNodeStop;
	}

	public void setDistNodeStop(double distNodeStop) {
		this.distNodeStop = distNodeStop;
	}

	public int getIsSwitchable() {
		return isSwitchable;
	}

	public void setIsSwitchable(int isSwitchable) {
		this.isSwitchable = isSwitchable;
	}

	public OtherAgent getSameDirAgent() {
		return sameDirAgent;
	}

	public void setSameDirAgent(OtherAgent sameDirAgent) {
		this.sameDirAgent = sameDirAgent;
	}

	public OtherAgent getOppDirAgent() {
		return oppDirAgent;
	}

	public void setOppDirAgent(OtherAgent oppDirAgent) {
		this.oppDirAgent = oppDirAgent;
	}

	public int getNumParallelTracks() {
		return numParallelTracks;
	}

	public void setNumParallelTracks(int numParallelTracks) {
		this.numParallelTracks = numParallelTracks;
	}

	public ObservationTreeNode(List<Double> position, double distNodeAgent, double distNodeStop, int isSwitchable, OtherAgent sameDirAgent, OtherAgent oppDirAgent, int numParallelTracks, Id<Node> nodeId) {
		this.position = position;
		this.distNodeAgent = distNodeAgent;
		this.distNodeStop = distNodeStop;
		this.isSwitchable = isSwitchable;
		this.sameDirAgent = sameDirAgent;
		this.oppDirAgent = oppDirAgent;
		this.numParallelTracks = numParallelTracks;
		this.nodeId = nodeId;
	}
}
