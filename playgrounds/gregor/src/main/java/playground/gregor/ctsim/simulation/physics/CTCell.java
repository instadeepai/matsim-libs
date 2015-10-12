package playground.gregor.ctsim.simulation.physics;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.gbl.MatsimRandom;
import playground.gregor.ctsim.simulation.CTEvent;
import playground.gregor.sim2d_v4.cgal.LineSegment;
import playground.gregor.sim2d_v4.events.debug.LineEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CTCell {

    private static final double RHO_M = 6.6687;
    private static final double V_0 = 1.5;
    private static final double GAMMA = 0.3;
    private static double Q;

    static {
        Q = (V_0 * RHO_M) / ((V_0 / GAMMA) + 1);
    }


    private final List<CTCellFace> faces = new ArrayList<>();
    private final CTNetwork net;
    private final CTNetworkEntity parent;
    private final HashSet<CTPed> peds = new HashSet<>();
    int r = 0;
    int g = 0;
    int b = 192;
    private double x;
    private double y;
    private double alpha; //area
    private int N; //max number of peds
    private double rho; //current density
    private CTPed next = null;
    private double nextCellJumpTime;

    private CTEvent currentEvent = null;

    private int pedCnt = 0;

    public CTCell(double x, double y, CTNetwork net, CTNetworkEntity parent) {
        this.x = x;
        this.y = y;
        this.net = net;
        this.parent = parent;
    }


    public void addFace(CTCellFace face) {
        faces.add(face);
    }

    public void debug(EventsManager em) {
        for (CTCellFace f : faces) {
            debug(f, em);
        }

    }

    private void debug(CTCellFace f, EventsManager em) {
//		if (MatsimRandom.getRandom().nextDouble() > 0.1 ){
//			return;
//		}

        {
            LineSegment s = new LineSegment();
            s.x0 = f.x0;
            s.y0 = f.y0;
            s.x1 = f.x1;
            s.y1 = f.y1;
            LineEvent le = new LineEvent(0, s, true, r, g, b, 255, 50);
            em.processEvent(le);
        }
    }

    public void updateIntendedCellJumpTimeAndChooseNextJumper(double now) {
        if (this.currentEvent != null) {
            this.currentEvent.invalidate();
        }
        if (peds.size() == 0) {
            this.nextCellJumpTime = Double.NaN;
            return;
        }
        double minJumpTime = Double.POSITIVE_INFINITY;
        CTPed nextJumper = null;
        for (CTPed ped : peds) {
            double rate = ped.chooseNextCellAndReturnJumpRate();

            double rnd = -Math.log(1 - MatsimRandom.getRandom().nextDouble());
            double jumpTime = now + rnd / rate;
            if (jumpTime < minJumpTime) {
                minJumpTime = jumpTime;
                nextJumper = ped;
            }
        }

        if (nextJumper == null) {
            return;
        }
        this.next = nextJumper;

        this.nextCellJumpTime = minJumpTime;
        CTEvent e = new CTEvent(this, nextCellJumpTime);
        this.currentEvent = e;
        this.net.addEvent(e);
    }

    public double getIntendedNextCellJumpTime() {
        return this.nextCellJumpTime;
    }


    public void setArea(double a) {
        this.alpha = a;
        this.N = (int) (RHO_M * this.alpha + 0.5);
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public int getN() {
        return this.N;
    }

    public int getNumOfPeds() {
        return this.peds.size();
    }


    private double getDelta() { //demand function
        return Math.min(Q, V_0 * this.rho);
    }

    private double getSigma() { //supply function
        return Math.min(Q, GAMMA * (RHO_M - this.rho));
    }

    private double getRho() { //current density
        return this.rho;
    }

    public double getJ(CTCell n_i) { //flow to cell n_i
        double demand = getDelta();
        double supply = n_i.getSigma();
        return CTLink.WIDTH * Math.min(demand, supply);
    }


    public List<CTCellFace> getFaces() {
        return faces;
    }

    public void jumpAndUpdateNeighbors(double now) {

        CTCell nb = next.getNextCellAndJump();
        next = null;
        this.nextCellJumpTime = Double.NaN;


        Set<CTCell> affectedCells = new HashSet<>();
        for (CTCellFace face : this.faces) {
            affectedCells.add(face.nb);
        }
        for (CTCellFace face : nb.faces) {
            affectedCells.add(face.nb);
        }
        for (CTCell cell : affectedCells) {
            cell.updateIntendedCellJumpTimeAndChooseNextJumper(now);
        }
    }

    public void jumpOffPed(CTPed ctPed) {
        this.peds.remove(ctPed);
        this.rho = this.peds.size() / alpha;
    }

    public void jumpOnPed(CTPed ctPed) {
        if (this.parent instanceof CTNode) {
            return;
        }
        this.peds.add(ctPed);
        this.rho = this.peds.size() / alpha;
        this.pedCnt++;
    }

    public HashSet<CTPed> getPeds() {
        return peds;
    }
}