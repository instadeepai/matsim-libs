/* *********************************************************************** *
 * project: org.matsim.*
 * JointReplanningConfigGroup.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.thibautd.jointtripsoptimizer.run.config;

import java.lang.reflect.Field;
import java.lang.String;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.groups.PlanomatConfigGroup;
import org.matsim.core.config.groups.PlanomatConfigGroup.TripStructureAnalysisLayerOption;
import org.matsim.core.config.groups.PlanomatConfigGroup.SimLegInterpretation;
import org.matsim.core.config.Module;

/**
 * Gives access to all parameters needed for the joint plan optimisation.
 * @author thibautd
 */
public class JointReplanningConfigGroup extends Module {

	private static final Logger log = Logger.getLogger(JointReplanningConfigGroup.class);

	private static final long serialVersionUID = 1L;
	public static final String GROUP_NAME = "JointReplanning";

	//parameter names
	/**
	 * probability with wich the mutation operator is aplied on a gene.
	 */
	public static final String MUTATION_PROB = "mutationProbability";
	/**
	 * rate of the whole arithmetical CO: rate * populationSize couples will be
	 * mated.
	 */
	public static final String WHOLE_CO_PROB = "WholeArithmeticalCrossOverRate";
	/**
	 * rate of the simple arithmetical CO: rate * populationSize couples will be
	 * mated.
	 */
	public static final String SIMPLE_CO_PROB = "SimpleArithmeticalCrossOverRate";
	/**
	 * rate of the single arithmetical CO: rate * populationSize couples will be
	 * mated.
	 */
	public static final String SINGLE_CO_PROB = "SingleArithmeticalCrossOverRate";
	/**
	 * rate of the CO letting continuous dimensions untouched: rate * populationSize couples will be
	 * mated.
	 */
	public static final String DISCRETE_CO_PROB = "discreteOnlyCrossOverRate";
	/**
	 * non-unifority parameter: the higher it is, the quicker the mutation of
	 * double values becomes small.
	 */
	public static final String NON_UNIFORMITY_PARAM = "mutationNonUniformity";
	/**
	 * determines if the joint trip participation is to optimize.
	 */
	public static final String OPTIMIZE_TOGGLE = "toggleToOptimize";
	/**
	 * For analysis purpose: output plot for each optimised plan. To use
	 * on little scenarios only!
	 */
	public static final String PLOT_FITNESS = "plotFitnessEvolution";
	/**
	 * Determines if the mode is optimized. This should always be set to "true"
	 * if the participation is optimized.
	 */
	public static final String OPTIMIZE_MODE = "modeToOptimize";
	/**
	 * The mode choice set.
	 */
	public static final String AVAIL_MODES = "availableModes";
	/**
	 * true if the stop condition is based on fitness evolution, false
	 * if the maximum number of iterations should always be performed.
	 */
	public static final String DO_MONITOR = "fitnessToMonitor";
	/**
	 * number of iterations before the fitness monitoring begins.
	 */
	public static final String ITER_MIN_NUM = "minNumberOfGAIterations";
	/**
	 * maximum number of generations.
	 */
	public static final String ITER_NUM = "maxNumberOfGAIterations";
	/**
	 * number of generations to wait between each best fitness measurement.
	 */
	public static final String MONITORING_PERIOD = "fitnessMonitoringPeriod";
	/**
	 * if the difference between two consecutive best fitness measurements are 
	 * below this value, the iterations are stopped.
	 */
	public static final String MIN_IMPROVEMENT = "minimumFitnessImprovementCHF";
	/**
	 * "layer" at which the subtour should be determined: facility or link.
	 * should always be set to link: pick up and drop off have their own facilities
	 */
	public static final String STRUCTURE_LAYER = "tripStructureAnalysisLayer";
	/**
	 * "Scale" of the discrete distance in the restricted tournament selection
	 */
	public static final String DISCRETE_DIST_SCALE = "discreteDistanceScale";
	/**
	 * "Cetin" or "CharyparEtAl". determines whether the first or the last link
	 * of a route is taken into acount when estimating travel time.
	 */
	public static final String SIM_LEG_INT = "simLegInterpretation";
	/**
	 * true: the offsprings resulting from cross overs are mutated before selection
	 * false: at each iteration, new chromosomes are generated by mutating the old ones,
	 * and compete with them in selection.
	 */
	public static final String IN_PLACE = "mutationInPlace";
	/**
	 * Knowing that a double gene will be mutated, probability for the mutation
	 * to be non uniform.
	 */
	public static final String P_NON_UNIFORM = "probNonUniform";
	/**
	 * true if only the hamming distance have to be considered in RTS
	 */
	public static final String HAMMING_DISTANCE = "useHammingDistanceOnlyInRTS";
	/**
	 * Population size is a linear function of chromosome size
	 */
	public static final String POPULATION_COEF = "populationSizeSlope";
	/**
	 * Population size is a linear function of chromosome size
	 */
	public static final String POPULATION_INTERCEPT = "populationSizeIntercept";
	/**
	 * the maximum number of chromosomes in the population.
	 * Values inferior to 2 correspond to no limitation.
	 */
	public static final String MAX_POP_SIZE = "maxPopulationSize";
	/**
	 * RTS window size is a linear function of population size
	 */
	public static final String WINDOW_SIZE_COEF = "windowSizeSlope";
	/**
	 * RTS window size is a linear function of population size
	 */
	public static final String WINDOW_SIZE_INTERCEPT = "windowSizeIntercept";
	/**
	 * Defines the max CPU time per clique member before stopping the GA,
	 * in nanosecs. negative or null value means no limitation. The value
	 * Should be set quite high, as the aim is to avoid loosing time on
	 * anyway too hard instances: an annoying side effect of time based criteria
	 * is that the optimisation of the very same instance with the very same random
	 * seed may lead to sligthly different results.
	 */
	public static final String MAX_CPU_TIME = "maxCpuTimePerMemberNanoSecs";
	/**
	 * true to use local optimisers on newly generated solutions
	 */
	public static final String MEMETIC ="isMemetic";
	/**
	 * Weight of the non-optimizing fitness
	 */
	public static final String NON_MEM_FITNESS_WEIGHT = "directFitnessWeight";
	/**
	 * Weight of the duration-optimizing fitness
	 */
	public static final String DUR_OPT_FITNESS_WEIGHT = "durationOptimizingFitnessWeight";
	/**
	 * Weight of the toggle-optimizing fitness
	 */
	public static final String TOGGLE_OPT_FITNESS_WEIGHT = "toggleOptimizingFitnessWeight";
	/**
	 * Stopping criterion for the duration optimizer, in case no score stagnation occurs
	 */
	public static final String N_MAX_SIMPLEX_ITERS = "nMaxMultidirectionnalSearchIterations";
	/**
	 * if true, the population size will be multiplied by (1 + n_jointTrips).
	 */
	public static final String MULTIPLICATIVE_POP_SIZE = "isMultiplicativePopSize";
	/**
	 * if true, the widow size will be proportional to the population size.
	 * Otherwise, it will be set to 2 ** nToggleGenes, that is, the number of
	 * possible combinations of joint trips.
	 */
	public static final String PROPORTIONNAL_WINDOW_SIZE = "isProportionnalWindowSize";

	//parameter values, initialized to defaults.
	private double populationCoef = 1;
	private double populationIntercept = 0;
	private int maxPopulationSize = -1;
	private double windowSizeCoef = 0.15;
	private double windowSizeIntercept = 0;
	private double mutationProb = 0.1;
	private double wholeCrossOverProb = 0.3;
	private double simpleCrossOverProb = 0.3;
	private double singleCrossOverProb = 0.1;
	private double discreteCrossOverProb = 0.0;
	private int numberOfIterations = 50;
	private double betaNonUniformity = 25;
	private boolean optimizeToggle = true;
	private boolean plotFitness = false;
	private boolean optimizeMode = true;
	private List<String> availableModes = null;
	private static final String defaultModes = "car,pt,walk,bike";
	private int minNumberOfIterations = 3;
	private int monitoringPeriod = 1;
	private boolean doMonitor = true;
	private double minImprovement = 0.000001d;
	private TripStructureAnalysisLayerOption
		tripStructureAnalysisLayer = TripStructureAnalysisLayerOption.link;
	private double discreteDistScale = 3600;
	private SimLegInterpretation simLegInt = SimLegInterpretation.CharyparEtAlCompatible;
	private boolean inPlaceMutation = true;
	private double pNonUniform = 0.0;
	private boolean useOnlyHammingDistance = false;
	private boolean isProportionnalWindowSize = false;
	// default: with or without?
	//private long maxCpuTimePerMember = (long) (10 * 1E9);
	private long maxCpuTimePerMember = -1;
	private boolean isMemetic = true;
	private double directFitnessWeight = 6;
	private double durationMemeticFitnessWeight = 0.25;
	private double toggleMemeticFitnessWeight = 3.75;
	private int nMaxSimplexIters = 200;
	private boolean isMultiplicative = false;

	public JointReplanningConfigGroup() {
		super(GROUP_NAME);
		log.debug("joint replanning config group initialized");
	}

	/*
	 * =========================================================================
	 * base class methods
	 * =========================================================================
	 */
	@Override
	public void addParam(final String param_name, final String value) {
		if (param_name.equals(MUTATION_PROB)) {
			this.setMutationProbability(value);
		}
		else if (param_name.equals(WHOLE_CO_PROB)) {
			this.setWholeCrossOverProbability(value);
		}
		else if (param_name.equals(SIMPLE_CO_PROB)) {
			this.setSimpleCrossOverProbability(value);
		}
		else if (param_name.equals(SINGLE_CO_PROB)) {
			this.setSingleCrossOverProbability(value);
		}
		else if (param_name.equals(DISCRETE_CO_PROB)) {
			this.setDiscreteCrossOverProbability(value);
		}
		else if (param_name.equals(ITER_NUM)) {
			this.setMaxIterations(value);
		}
		else if (param_name.equals(NON_UNIFORMITY_PARAM)) {
			this.setMutationNonUniformity(value);
		}
		else if (param_name.equals(OPTIMIZE_TOGGLE)) {
			this.setOptimizeToggle(value);
		}
		else if (param_name.equals(PLOT_FITNESS)) {
			this.setPlotFitness(value);
		}
		else if (param_name.equals(OPTIMIZE_MODE)) {
			this.setModeToOptimize(value);
		}
		else if (param_name.equals(AVAIL_MODES)) {
			this.setAvailableModes(value);
		}
		else if (param_name.equals(ITER_MIN_NUM)) {
			this.setMinIterations(value);
		}
		else if (param_name.equals(MONITORING_PERIOD)) {
			this.setMonitoringPeriod(value);
		}
		else if (param_name.equals(DO_MONITOR)) {
			this.setFitnessToMonitor(value);
		}
		else if (param_name.equals(MIN_IMPROVEMENT)) {
			this.setMinImprovement(value);
		}
		else if (param_name.equals(STRUCTURE_LAYER)) {
			this.setTripStructureAnalysisLayer(value);
		}
		else if (param_name.equals(DISCRETE_DIST_SCALE)) {
			this.setDiscreteDistanceScale(value);
		}
		else if (param_name.equals(SIM_LEG_INT)) {
			this.setSimLegInterpretation(value);
		}
		else if (param_name.equals(IN_PLACE)) {
			this.setInPlaceMutation(value);
		}
		else if (param_name.equals(P_NON_UNIFORM)) {
			this.setNonUniformMutationProbability(value);
		}
		else if (param_name.equals(HAMMING_DISTANCE)) {
			this.setUseOnlyHammingDistanceInRTS(value);
		}
		else if (param_name.equals(POPULATION_COEF)) {
			this.setPopulationCoef(value);
		}
		else if (param_name.equals(WINDOW_SIZE_COEF)) {
			this.setWindowSizeCoef(value);
		}
		else if (param_name.equals(POPULATION_INTERCEPT)) {
			this.setPopulationIntercept(value);
		}
		else if (param_name.equals(WINDOW_SIZE_INTERCEPT)) {
			this.setWindowSizeIntercept(value);
		}
		else if (param_name.equals(MAX_CPU_TIME)) {
			this.setMaxCpuTimePerMemberNanoSecs(value);
		}
		else if (param_name.equals(MEMETIC)) {
			this.setIsMemetic(value);
		}
		else if (param_name.equals(NON_MEM_FITNESS_WEIGHT)) {
			this.setDirectFitnessWeight(value);
		}
		else if (param_name.equals(DUR_OPT_FITNESS_WEIGHT)) {
			this.setDurationMemeticFitnessWeight(value);
		}
		else if (param_name.equals(TOGGLE_OPT_FITNESS_WEIGHT)) {
			this.setToggleMemeticFitnessWeight(value);
		}
		else if (param_name.equals(N_MAX_SIMPLEX_ITERS)) {
			this.setMaxSimplexIterations(value);
		}
		else if (param_name.equals(MAX_POP_SIZE)) {
			this.setMaxPopulationSize(value);
		}
		else if (param_name.equals(MULTIPLICATIVE_POP_SIZE)) {
			this.setIsMultiplicativePopulationSize(value);
		}
		else if (param_name.equals(PROPORTIONNAL_WINDOW_SIZE)) {
			this.setIsProportionnalWindowSize(value);
		}
		else {
			log.warn("Unrecognized JointReplanning parameter: "+
					param_name+", of value: "+value+".");
		}
	}


	@Override
	public String getValue(final String param_name) {
		if (param_name.equals(MUTATION_PROB)) {
			return String.valueOf(this.getMutationProbability());
		}
		else if (param_name.equals(WHOLE_CO_PROB)) {
			return String.valueOf(this.getWholeCrossOverProbability());
		}
		else if (param_name.equals(SIMPLE_CO_PROB)) {
			return String.valueOf(this.getSimpleCrossOverProbability());
		}
		else if (param_name.equals(SINGLE_CO_PROB)) {
			return String.valueOf(this.getSingleCrossOverProbability());
		}
		else if (param_name.equals(DISCRETE_CO_PROB)) {
			return String.valueOf(this.getDiscreteCrossOverProbability());
		}
		else if (param_name.equals(ITER_NUM)) {
			return String.valueOf(this.getMaxIterations());
		}
		else if (param_name.equals(NON_UNIFORMITY_PARAM)) {
			return String.valueOf(this.getMutationNonUniformity());
		}
		else if (param_name.equals(OPTIMIZE_TOGGLE)) {
			return String.valueOf(this.getOptimizeToggle());
		}
		else if (param_name.equals(PLOT_FITNESS)) {
			return String.valueOf(this.getPlotFitness());
		}
		else if (param_name.equals(OPTIMIZE_MODE)) {
			return String.valueOf(this.getModeToOptimize());
		}
		else if (param_name.equals(AVAIL_MODES)) {
			return getAvailableModesString();
		}
		else if (param_name.equals(ITER_MIN_NUM)) {
			return String.valueOf(this.getMinIterations());
		}
		else if (param_name.equals(MONITORING_PERIOD)) {
			return String.valueOf(this.getMonitoringPeriod());
		}
		else if (param_name.equals(DO_MONITOR)) {
			return String.valueOf(this.getFitnessToMonitor());
		}
		else if (param_name.equals(MIN_IMPROVEMENT)) {
			return String.valueOf(this.getMinImprovement());
		}
		else if (param_name.equals(STRUCTURE_LAYER)) {
			return this.getTripStructureAnalysisLayer().toString();
		}
		else if (param_name.equals(DISCRETE_DIST_SCALE)) {
			return String.valueOf(this.getDiscreteDistanceScale());
		}
		else if (param_name.equals(SIM_LEG_INT)) {
			return String.valueOf(this.getSimLegInterpretation());
		}
		else if (param_name.equals(IN_PLACE)) {
			return String.valueOf(this.getInPlaceMutation());
		}
		else if (param_name.equals(P_NON_UNIFORM)) {
			return String.valueOf(this.getNonUniformMutationProbability());
		}
		else if (param_name.equals(HAMMING_DISTANCE)) {
			return String.valueOf(this.getUseOnlyHammingDistanceInRTS());
		}
		else if (param_name.equals(POPULATION_COEF)) {
			return String.valueOf(this.getPopulationCoef());
		}
		else if (param_name.equals(WINDOW_SIZE_COEF)) {
			return String.valueOf(this.getWindowSizeCoef());
		}
		else if (param_name.equals(POPULATION_INTERCEPT)) {
			return String.valueOf(this.getPopulationIntercept());
		}
		else if (param_name.equals(WINDOW_SIZE_INTERCEPT)) {
			return String.valueOf(this.getWindowSizeIntercept());
		}
		else if (param_name.equals(MAX_CPU_TIME)) {
			return String.valueOf(this.getMaxCpuTimePerMemberNanoSecs());
		}
		else if (param_name.equals(MEMETIC)) {
			return String.valueOf(this.getIsMemetic());
		}
		else if(param_name.equals(MAX_POP_SIZE)) {
			return String.valueOf(this.getMaxPopulationSize());
		}
		else if (param_name.equals(NON_MEM_FITNESS_WEIGHT)) {
			return String.valueOf(this.getDirectFitnessWeight());
		}
		else if (param_name.equals(DUR_OPT_FITNESS_WEIGHT)) {
			return String.valueOf(this.getDurationMemeticFitnessWeight());
		}
		else if (param_name.equals(TOGGLE_OPT_FITNESS_WEIGHT)) {
			return String.valueOf(this.getToggleMemeticFitnessWeight());
		}
		else if (param_name.equals(N_MAX_SIMPLEX_ITERS)) {
			return ""+this.getMaxSimplexIterations();
		}
		else if (param_name.equals(MULTIPLICATIVE_POP_SIZE)) {
			return ""+this.getIsMultiplicativePopulationSize();
		}
		else if (param_name.equals(PROPORTIONNAL_WINDOW_SIZE)) {
			return ""+this.isProportionnalWindowSize();
		}
		return null;
	}

	@Override
	public TreeMap<String,String> getParams() {
		TreeMap<String,String> map = new TreeMap<String,String>();
		this.addParameterToMap(map, MUTATION_PROB);
		this.addParameterToMap(map, WHOLE_CO_PROB);
		this.addParameterToMap(map, SIMPLE_CO_PROB);
		this.addParameterToMap(map, SINGLE_CO_PROB);
		this.addParameterToMap(map, DISCRETE_CO_PROB);
		this.addParameterToMap(map, ITER_NUM);
		this.addParameterToMap(map, NON_UNIFORMITY_PARAM);
		this.addParameterToMap(map, OPTIMIZE_TOGGLE);
		this.addParameterToMap(map, PLOT_FITNESS);
		this.addParameterToMap(map, OPTIMIZE_MODE);
		this.addParameterToMap(map, AVAIL_MODES);
		this.addParameterToMap(map, ITER_MIN_NUM);
		this.addParameterToMap(map, MONITORING_PERIOD);
		this.addParameterToMap(map, DO_MONITOR);
		this.addParameterToMap(map, MIN_IMPROVEMENT);
		this.addParameterToMap(map, STRUCTURE_LAYER);
		this.addParameterToMap(map, DISCRETE_DIST_SCALE);
		this.addParameterToMap(map, SIM_LEG_INT);
		this.addParameterToMap(map, IN_PLACE);
		this.addParameterToMap(map, HAMMING_DISTANCE);
		this.addParameterToMap(map, P_NON_UNIFORM);
		this.addParameterToMap(map, POPULATION_COEF);
		this.addParameterToMap(map, WINDOW_SIZE_COEF);
		this.addParameterToMap(map, POPULATION_INTERCEPT);
		this.addParameterToMap(map, WINDOW_SIZE_INTERCEPT);
		this.addParameterToMap(map, MAX_CPU_TIME);
		this.addParameterToMap(map, MEMETIC);
		this.addParameterToMap(map, MAX_POP_SIZE);
		this.addParameterToMap(map, NON_MEM_FITNESS_WEIGHT);
		this.addParameterToMap(map, DUR_OPT_FITNESS_WEIGHT);
		this.addParameterToMap(map, TOGGLE_OPT_FITNESS_WEIGHT);
		this.addParameterToMap(map, N_MAX_SIMPLEX_ITERS);
		this.addParameterToMap(map, PROPORTIONNAL_WINDOW_SIZE);
		return map;
	}

	/*
	 * =========================================================================
	 * getters/setters
	 * =========================================================================
	 */

	public double getMutationProbability() {
		return this.mutationProb;
	}

	public void setMutationProbability(String mutationProb) {
		this.mutationProb = Double.valueOf(mutationProb);

		if ((this.mutationProb < 0)||(this.mutationProb > 1)) {
			throw new IllegalArgumentException("probability values must in [0,1]");
		}
	}

	public double getWholeCrossOverProbability() {
		return this.wholeCrossOverProb;
	}

	public void setWholeCrossOverProbability(String coProb) {
		this.wholeCrossOverProb = Double.valueOf(coProb);

		if ((this.wholeCrossOverProb < 0)||(this.wholeCrossOverProb > 1)) {
			throw new IllegalArgumentException("probability values must in [0,1]");
		}
	}

	public double getSingleCrossOverProbability() {
		return this.singleCrossOverProb;
	}

	public void setSingleCrossOverProbability(String coProb) {
		this.singleCrossOverProb = Double.valueOf(coProb);

		if ((this.singleCrossOverProb < 0)||(this.singleCrossOverProb > 1)) {
			throw new IllegalArgumentException("probability values must in [0,1]");
		}
	}

	public double getSimpleCrossOverProbability() {
		return this.simpleCrossOverProb;
	}

	public void setSimpleCrossOverProbability(String coProb) {
		this.simpleCrossOverProb = Double.valueOf(coProb);

		if ((this.simpleCrossOverProb < 0)||(this.simpleCrossOverProb > 1)) {
			throw new IllegalArgumentException("probability values must in [0,1]");
		}
	}

	public int getMaxIterations() {
		return this.numberOfIterations;
	}

	public void setMaxIterations(String iterations) {
		this.numberOfIterations = Integer.parseInt(iterations);

		if (this.numberOfIterations < 0) {
			throw new IllegalArgumentException("number of iterations must be positive");
		}
	}

	public double getMutationNonUniformity() {
		return this.betaNonUniformity;
	}

	public void setMutationNonUniformity(String beta) {
		this.betaNonUniformity = Double.valueOf(beta);

		if (this.betaNonUniformity <= 0d) {
			throw new IllegalArgumentException("non uniformity mutation parameter"+
					" must be positive");
		}
	}

	public boolean getOptimizeToggle() {
		return this.optimizeToggle;
	}

	public void setOptimizeToggle(String value) {
		if (value.toLowerCase().equals("true")) {
			this.optimizeToggle = true;
		}
		else if (value.toLowerCase().equals("false")) {
			this.optimizeToggle = false;
		}
		else {
			throw new IllegalArgumentException("value for "+
					OPTIMIZE_TOGGLE+" must be \"true\" or \"false\"");
		}
	}

	public boolean getPlotFitness() {
		return this.plotFitness;
	}

	public void setPlotFitness(String value) {
		if (value.toLowerCase().equals("true")) {
			this.plotFitness = true;
		}
		else if (value.toLowerCase().equals("false")) {
			this.plotFitness = false;
		}
		else {
			throw new IllegalArgumentException("value for "+
					OPTIMIZE_TOGGLE+" must be \"true\" or \"false\"");
		}
	}

	public List<String> getAvailableModes() {
		if (this.availableModes == null) {
			//log.warn("modes available for the optimisation initialized to the "+
			//		"set of all available values");
			//this.availableModes = getAllModes();
			log.info("using default mode choice set: "+defaultModes);
			this.setAvailableModes(defaultModes);
		}
		return this.availableModes;
	}

	private String getAvailableModesString() {
		List<String> modes = this.getAvailableModes();

		StringBuffer buff = new StringBuffer();

		for (String mode : modes) {
			buff.append( "," );
			buff.append( mode );
		}

		return buff.substring( 1 );
	}

	public void setAvailableModes(String value) {
		String[] modes = value.split(",");

		//List<String> allModes = getAllModes();
		this.availableModes = new ArrayList<String>();

		for (String mode : modes) {
		//	if (allModes.contains(mode)) {
			try {
				this.availableModes.add((String)
						TransportMode.class.getField(mode).get(null));
			}
			//else {
			catch (NoSuchFieldException e) {
				throw new IllegalArgumentException("unrecognized mode: "+mode);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("error while iterating over "+
						"TransportMode fields");
			}
		}
	}

	private static List<String> getAllModes() {
		List<String> out = new ArrayList<String>();

		//iterate over all public fields of transport mode
		//TODO: more precise catches.
		for (Field field : TransportMode.class.getFields()) {
			try {
				out.add((String) field.get(null));
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("error while iterating over "+
						"TransportMode fields");
			}
		}

		return out;
	}

	public void setModeToOptimize(String value) {
		this.optimizeMode = Boolean.valueOf(value);
	}

	public boolean getModeToOptimize() {
		return this.optimizeMode;
	}

	public void setFitnessToMonitor(String value) {
		this.doMonitor = Boolean.parseBoolean(value);
	}

	public boolean getFitnessToMonitor() {
		return this.doMonitor;
	}

	public void setMinIterations(String value) {
		this.minNumberOfIterations = Integer.valueOf(value);
	}

	public int getMinIterations() {
		return this.minNumberOfIterations;
	}

	public void setMonitoringPeriod(String value) {
		this.monitoringPeriod = Integer.valueOf(value);
	}

	public int getMonitoringPeriod() {
		return this.monitoringPeriod;
	}

	public void setMinImprovement(String value) {
		this.minImprovement = Double.parseDouble(value);
	}

	public double getMinImprovement() {
		return this.minImprovement;
	}

	public void setTripStructureAnalysisLayer(String value) {
		if (value.equals("facility")) {
			this.tripStructureAnalysisLayer =
				PlanomatConfigGroup.TripStructureAnalysisLayerOption.facility;
		}
		else if (value.equals("link")) {
			this.tripStructureAnalysisLayer =
				PlanomatConfigGroup.TripStructureAnalysisLayerOption.link;
		}
		else {
			throw new IllegalArgumentException("the tripStructureAnalysisLayer option"+
					" must be facility or link.");
		}
	}

	public TripStructureAnalysisLayerOption getTripStructureAnalysisLayer() {
			return this.tripStructureAnalysisLayer;
	}

	public void setDiscreteDistanceScale(String value) {
		this.discreteDistScale = Double.valueOf(value);
	}

	public double getDiscreteDistanceScale() {
		return this.discreteDistScale;
	}

	public void setSimLegInterpretation(String value) {
		this.simLegInt = SimLegInterpretation.valueOf(value);
	}

	public SimLegInterpretation getSimLegInterpretation() {
		return this.simLegInt;
	}

	public void setInPlaceMutation(final String value) {
		this.inPlaceMutation = Boolean.valueOf(value);
	}

	public boolean getInPlaceMutation() {
		return this.inPlaceMutation;
	}

	public void setNonUniformMutationProbability(final String value) {
		this.pNonUniform = Double.parseDouble(value);
	}

	public double getNonUniformMutationProbability() {
		return this.pNonUniform;
	}

	public void setUseOnlyHammingDistanceInRTS(final String value) {
		this.useOnlyHammingDistance = Boolean.parseBoolean(value);
	}

	public boolean getUseOnlyHammingDistanceInRTS() {
		return this.useOnlyHammingDistance;
	}

	public void setPopulationCoef(final String value) {
		populationCoef = Double.parseDouble(value);
	}

	public double getPopulationCoef() {
		return populationCoef;
	}

	public void  setPopulationIntercept(final String value) {
		populationIntercept = Double.parseDouble(value);
	}

	public double getPopulationIntercept() {
		return populationIntercept;
	}


	public void setWindowSizeCoef(final String value) {
		windowSizeCoef = Double.parseDouble(value);
	}

	public double getWindowSizeCoef() {
		return windowSizeCoef;
	}

	public void setWindowSizeIntercept(final String value) {
		windowSizeIntercept = Double.parseDouble(value);
	}

	public double getWindowSizeIntercept() {
		return windowSizeIntercept;
	}

	public void setMaxCpuTimePerMemberNanoSecs(final String value) {
		this.maxCpuTimePerMember = Long.valueOf(value);
	}

	public long getMaxCpuTimePerMemberNanoSecs() {
		return this.maxCpuTimePerMember;
	}

	public void setIsMemetic(final String value) {
		this.isMemetic = Boolean.valueOf(value);
	}

	public boolean getIsMemetic() {
		return isMemetic;
	}

	public void setMaxPopulationSize(final String value) {
		this.maxPopulationSize = Integer.valueOf(value);
	}

	public int getMaxPopulationSize() {
		return (this.maxPopulationSize < 2 ? Integer.MAX_VALUE : this.maxPopulationSize);
	}

	public void setDirectFitnessWeight(final String value) {
		this.directFitnessWeight = Double.parseDouble(value);
	}

	public double getDirectFitnessWeight() {
		return this.directFitnessWeight;
	}

	public void setDurationMemeticFitnessWeight(final String value) {
		this.durationMemeticFitnessWeight = Double.parseDouble(value);
	}

	public double getDurationMemeticFitnessWeight() {
		return this.durationMemeticFitnessWeight;
	}

	public void setToggleMemeticFitnessWeight(final String value) {
		this.toggleMemeticFitnessWeight = Double.parseDouble(value);
	}

	public double getToggleMemeticFitnessWeight() {
		return this.toggleMemeticFitnessWeight;
	}

	public void setMaxSimplexIterations( final String value ) {
		this.nMaxSimplexIters = Integer.valueOf(value);
	}

	public int getMaxSimplexIterations() {
		return this.nMaxSimplexIters;
	}

	public void setIsMultiplicativePopulationSize(final String value) {
		this.isMultiplicative = Boolean.parseBoolean( value );
	}

	public boolean getIsMultiplicativePopulationSize() {
		return isMultiplicative;
	}


	public void setDiscreteCrossOverProbability(final String value) {
		discreteCrossOverProb = Double.parseDouble( value );
	}

	public double getDiscreteCrossOverProbability() {
		return discreteCrossOverProb;
	}

	public void setIsProportionnalWindowSize(final String value) {
		isProportionnalWindowSize = Boolean.parseBoolean( value );
	}

	public boolean isProportionnalWindowSize() {
		return isProportionnalWindowSize;
	}

	// allow setting of GA params "directly"
	public void setMutationProbability(final double mutationProb) {
		this.mutationProb = (mutationProb);

		if ((this.mutationProb < 0)||(this.mutationProb > 1)) {
			throw new IllegalArgumentException("probability values must in [0,1]");
		}
	}

	public void setWholeCrossOverProbability(final double coProb) {
		this.wholeCrossOverProb = (coProb);

		if ((this.wholeCrossOverProb < 0)||(this.wholeCrossOverProb > 1)) {
			throw new IllegalArgumentException("probability values must in [0,1]");
		}
	}

	public void setSingleCrossOverProbability(final double coProb) {
		this.singleCrossOverProb = (coProb);

		if ((this.singleCrossOverProb < 0)||(this.singleCrossOverProb > 1)) {
			throw new IllegalArgumentException("probability values must in [0,1]");
		}
	}

	public void setSimpleCrossOverProbability(final double coProb) {
		this.simpleCrossOverProb = (coProb);

		if ((this.simpleCrossOverProb < 0)||(this.simpleCrossOverProb > 1)) {
			throw new IllegalArgumentException("probability values must in [0,1]");
		}
	}

	public void setMaxIterations(final int iterations) {
		this.numberOfIterations = (iterations);

		if (this.numberOfIterations < 0) {
			throw new IllegalArgumentException("number of iterations must be positive");
		}
	}

	public void setMutationNonUniformity(final double beta) {
		this.betaNonUniformity = (beta);

		if (this.betaNonUniformity <= 0d) {
			throw new IllegalArgumentException("non uniformity mutation parameter"+
					" must be positive");
		}
	}

	public void setMinIterations(final int value) {
		this.minNumberOfIterations = (value);
	}

	public void setMonitoringPeriod(final int  value) {
		this.monitoringPeriod = value;
	}

	public void setMinImprovement(final double value) {
		this.minImprovement = value;
	}

	public void setDiscreteDistanceScale(final double value) {
		this.discreteDistScale = value;
	}

	public void setInPlaceMutation(final boolean value) {
		this.inPlaceMutation = (value);
	}

	public void setNonUniformMutationProbability(final double value) {
		this.pNonUniform = (value);
	}

	public void setUseOnlyHammingDistanceInRTS(final boolean value) {
		this.useOnlyHammingDistance = value;
	}

	public void setPopulationCoef(final double value) {
		populationCoef = value;
	}

	public void  setPopulationIntercept(final double value) {
		populationIntercept = value;
	}

	public void setWindowSizeCoef(final double value) {
		windowSizeCoef = value;
	}

	public void setWindowSizeIntercept(final double value) {
		windowSizeIntercept = value;
	}

	public void setMaxPopulationSize(final int value) {
		this.maxPopulationSize = value;
	}

	public void setDirectFitnessWeight(final double value) {
		this.directFitnessWeight = value;
	}

	public void setDurationMemeticFitnessWeight(final double value) {
		this.durationMemeticFitnessWeight = value;
	}

	public void setToggleMemeticFitnessWeight(final double value) {
		this.toggleMemeticFitnessWeight = value;
	}

	public void setMaxSimplexIterations( final int value ) {
		this.nMaxSimplexIters = value;
	}
}

