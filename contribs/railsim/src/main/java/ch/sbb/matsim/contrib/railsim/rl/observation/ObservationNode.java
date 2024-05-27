package ch.sbb.matsim.contrib.railsim.rl.observation;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Node;

import java.util.List;

public class ObservationNode {

	Id<Node> nodeId;
	List<Double> position;
	double distNodeAgent;
	double distNodeStop;

	public ObservationNode(List<Double> position, double distNodeAgent, double distNodeStop, int isSwitchable, OtherAgent sameDirAgent, OtherAgent oppDirAgent, int numParallelTracks, Id<Node> nodeId) {
		this.position = position;
		this.distNodeAgent = distNodeAgent;
		this.distNodeStop = distNodeStop;
		this.isSwitchable = isSwitchable;
		this.sameDirAgent = sameDirAgent;
		this.oppDirAgent = oppDirAgent;
		this.numParallelTracks = numParallelTracks;
		this.nodeId = nodeId;
	}

	int isSwitchable;
	OtherAgent sameDirAgent;
	OtherAgent oppDirAgent;
	int numParallelTracks;
}
