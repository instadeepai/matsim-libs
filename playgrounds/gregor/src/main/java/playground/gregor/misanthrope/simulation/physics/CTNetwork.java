package playground.gregor.misanthrope.simulation.physics;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.api.experimental.events.EventsManager;
import playground.gregor.misanthrope.debug.Debugger3;
import playground.gregor.misanthrope.run.CTRunner;
import playground.gregor.misanthrope.simulation.CTEvent;
import playground.gregor.misanthrope.simulation.CTEventsPaulPriorityQueue;
import playground.gregor.sim2d_v4.events.XYVxVyEventImpl;
import playground.gregor.sim2d_v4.events.debug.TextEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class CTNetwork {

    private static final Logger log = Logger.getLogger(CTNetwork.class);

    private final CTEventsPaulPriorityQueue events = new CTEventsPaulPriorityQueue();
    //	private final PriorityQueue<CTEvent> events = new PriorityQueue<>();
    private final CTNetsimEngine engine;
    private final int cores = Runtime.getRuntime().availableProcessors();
    private Map<Id<Link>, CTLink> links = new HashMap<>();
    private Map<Id<Node>, CTNode> nodes = new HashMap<>();
    private Network network;
    private EventsManager em;

    public CTNetwork(Network network, EventsManager em, CTNetsimEngine engine) {
        this.network = network;
        this.em = em;
        this.engine = engine;
        init();
    }

    private void init() {
        if (CTRunner.DEBUG) {
            TextEvent e1 = new TextEvent(0, "alpha: " + CTLink.WIDTH / 2, 10 + CTLink.WIDTH, -Debugger3.WIDTH / 2 - 3);

            this.em.processEvent(e1);
            TextEvent e2 = new TextEvent(0, "rho_m: " + CTCell.RHO_M, 10 + CTLink.WIDTH, -Debugger3.WIDTH / 2 - 3 - 2);
            this.em.processEvent(e2);
            TextEvent e3 = new TextEvent(0, "v_0: " + CTCell.V_0, 10 + CTLink.WIDTH, -Debugger3.WIDTH / 2 - 3 - 4);
            this.em.processEvent(e3);
            TextEvent e4 = new TextEvent(0, "gamma: " + CTCell.GAMMA, 10 + CTLink.WIDTH, -Debugger3.WIDTH / 2 - 3 - 6);
            this.em.processEvent(e4);
            TextEvent e5 = new TextEvent(0, "p_0: " + CTCell.P0, 10 + CTLink.WIDTH, -Debugger3.WIDTH / 2 - 3 - 8);
            this.em.processEvent(e5);
            TextEvent e6 = new TextEvent(0, "#left -> right: " + Debugger3.AGENTS_LR, 10 + CTLink.WIDTH, -Debugger3.WIDTH / 2 - 3 - 10);
            this.em.processEvent(e6);
            TextEvent e7 = new TextEvent(0, "#right -> left: " + Debugger3.AGENTS_RL, 10 + CTLink.WIDTH, -Debugger3.WIDTH / 2 - 3 - 12);
            this.em.processEvent(e7);
            TextEvent e8 = new TextEvent(0, "inflow [1/s]: left -> right: " + 1 / Debugger3.INV_INFLOW, 10 + CTLink.WIDTH, -Debugger3.WIDTH / 2 - 3 - 14);
            this.em.processEvent(e8);
            TextEvent e9 = new TextEvent(0, "inflow [1/s]: right -> left: " + 1 / Debugger3.INV_INFLOW, 10 + CTLink.WIDTH, -Debugger3.WIDTH / 2 - 3 - 16);
            this.em.processEvent(e9);
        }

//		log.info("initializing network; using " + this.cores + " threads.");
//		List<Worker> workers = new ArrayList<>();
//		List<Thread> threads = new ArrayList<>();
//		for (int i = 0; i < this.cores; i++) {
//			Worker w = new Worker();
//			workers.add(w);
//			Thread t = new Thread(w);
//			t.start();
//			threads.add(t);
//		}

        log.info("creating and initializing links and nodes");
        for (Node n : this.network.getNodes().values()) {
            double mxCap = 0;
            for (Link l : n.getInLinks().values()) {
                if (l.getCapacity() > mxCap) {
                    mxCap = l.getCapacity();
                }
            }
            for (Link l : n.getOutLinks().values()) {
                if (l.getCapacity() > mxCap) {
                    mxCap = l.getCapacity();
                }
            }

            mxCap = Math.min(mxCap, 133);

            CTNode ct = new CTNode(n.getId(), n, this, mxCap / 1.33);
            this.nodes.put(n.getId(), ct);
        }

//		int cnt = 0;
        for (Link l : this.network.getLinks().values()) {


            if (links.get(l.getId()) != null) {
                continue;
            }
            Link rev = getRevLink(l);
            CTLink ct = new CTLink(l, rev, em, this, this.nodes.get(l.getFromNode().getId()), this.nodes.get(l.getToNode().getId()));
//			workers.get(cnt++ % this.cores).add(ct);
            ct.init();
            links.put(l.getId(), ct);
            if (rev != null) {
                links.put(rev.getId(), ct);
            }

        }

        for (CTNode ctNode : this.nodes.values()) {
            ctNode.init();
//            if (cnt++ % 10 == 0) {
//       				log.info(cnt);
//       			}
//			workers.get(cnt++ % this.cores).add(ctNode);

        }
//		for (Worker w : workers) {
//			w.add(new CTNetworkEntity() {
//				@Override
//				public void init() {
//				}
//			});
//		}
//		for (Thread t : threads) {
//			try {
//				t.join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//
//		}
        log.info("verifying network");
        checkNetwork();
        log.info("done.");
    }

    private Link getRevLink(Link l) {
        for (Link rev : l.getToNode().getOutLinks().values()) {
            if (rev.getToNode() == l.getFromNode()) {
                return rev;
            }
        }
        return null;
    }

    private void checkNetwork() {
        for (CTNode n : this.nodes.values()) {
            for (CTCellFace face : n.getCTCell().getFaces()) {
                if (face.nb == null) {
                    throw new RuntimeException("node cell face is null!");
                }
            }
            for (CTCell ctCell : n.getCTCell().getNeighbors()) {
                if (!ctCell.getNeighbors().contains(n.getCTCell())) {
                    throw new RuntimeException("missing backward pointer!");
                }
            }
        }
        for (CTLink l : this.links.values()) {
            for (CTCell c : l.getCells()) {
                for (CTCellFace face : c.getFaces()) {
                    if (face.nb == null) {
                        throw new RuntimeException("link cell face is null!");
                    }
                }
                for (CTCell ctCell : c.getNeighbors()) {
                    if (!ctCell.getNeighbors().contains(c)) {
                        throw new RuntimeException("missing backward pointer!");
                    }
                }
            }
        }
    }

    public CTNetsimEngine getEngine() {
        return this.engine;
    }

    public EventsManager getEventsManager() {
        return this.em;
    }

    public void doSimStep(double time) {
        if (CTRunner.DEBUG) {
            draw(time);
        }
//
//        if (((int) time) % 60 == 0) {
//            this.nodes.values().parallelStream().forEach(n -> {
//                if (n.getCTCell().getN() > 0 && (n.getCTCell().getCurrentEvent() == null || n.getCTCell().getCurrentEvent().isInvalid()))
//                    n.getCTCell().updateIntendedCellJumpTimeAndChooseNextJumper(time);
//            });
//            this.links.values().parallelStream().flatMap(l -> l.getCells().stream()).forEach(c -> {
//                if (c.getN() > 0 && (c.getCurrentEvent() == null || c.getCurrentEvent().isInvalid()))
//                    c.updateIntendedCellJumpTimeAndChooseNextJumper(time);
//            });
//        }

        while (this.events.peek() != null && events.peek().getExecTime() < time + 1) {
            CTEvent e = events.poll();


            if (e.isInvalid()) {
                continue;
            }
            e.execute();
        }


    }

    private void draw(double time) {
        for (CTLink link : getLinks().values()) {
            Link ll = link.getDsLink();
//			if (!accept(ll)){
//			    continue;
//            }
            double dx = ll.getToNode().getCoord().getX() - ll.getFromNode().getCoord().getX();
            double dy = ll.getToNode().getCoord().getY() - ll.getFromNode().getCoord().getY();
            dx /= ll.getLength();
            dy /= ll.getLength();
            for (CTCell cell : link.getCells()) {
                drawCell(cell, time, dx, dy);
            }
        }

//		for (CTNode n : this.nodes.values())  {
//		    drawCell(n.getCTCell(),time,0,0);
//        }
    }

    private boolean accept(Link ll) {
        if (ll.getId().toString().contains("el")) {
            return true;
        }
        for (Link l : ll.getToNode().getOutLinks().values()) {
            if (l.getId().toString().contains("el")) {
                return true;
            }
        }

        return false;
//        for (Link l : ll.getToNode().getOutLinks().values() ) {
//        	        if (l.getId().toString().contains("el")) {
//        	            return true;
//                    }
//                }
    }

    private void drawCell(CTCell cell, double time, double dx, double dy) {
        for (CTPed ped : cell.getPeds()) {
            double oX = (5 - (ped.hashCode() % 10)) / (20. / CTLink.WIDTH);
            double oY = (5 - ((23 * ped.hashCode()) % 10)) / (20. / CTLink.WIDTH);

            double x = cell.getX() + oX / 2.;
            double y = cell.getY() + oY / 2.;

            XYVxVyEventImpl e = new XYVxVyEventImpl(ped.getDriver().getId(), x, y, dx * ped.getDesiredDir(), dy * ped.getDesiredDir(), time);
            this.em.processEvent(e);
        }
    }

    public Map<Id<Link>, CTLink> getLinks() {
        return this.links;
    }

    public void run() {
        double time = 0;
        while (events.peek() != null && events.peek().getExecTime() < 3600 * 240) {

            CTEvent e = events.poll();

            if (CTRunner.DEBUG && e.getExecTime() > time + 1) {
                time = e.getExecTime();
                draw(time);

            }
            if (e.isInvalid()) {
                continue;
            }
            e.execute();
        }

    }

    public void addEvent(CTEvent e) {
        this.events.add(e);
    }

    CTNode getCTNode(Id<Node> id) {
        return this.nodes.get(id);
    }


    public void afterSim() {

//                    this.nodes.values().parallelStream().forEach(n -> {
//                        if (n.getCTCell().getN() > 0)
//                            log.error("still " + n.getCTCell().getN() + " peds in cell");
//                    });
//                    this.links.values().parallelStream().flatMap(l -> l.getCells().stream()).forEach(c -> {
//                        if (c.getN() > 0)
//                            log.error("still " + c.getN() + " peds in cell");
//                    });
    }

    private final class Worker implements Runnable {

        private LinkedBlockingQueue<CTNetworkEntity> q = new LinkedBlockingQueue<>();

        @Override
        public void run() {
            while (true) {
                try {
                    CTNetworkEntity e = q.take();
                    if (e instanceof CTLink) {
                        e.init();
                    } else {
                        if (e instanceof CTNode) {
                            e.init();
                            ((CTNode) e).getCTCell().debug(em);
                        } else {
                            break;
                        }
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

        }

        public void add(CTNetworkEntity e) {
            q.offer(e);
        }
    }


}
