package edu.vt.rt.datastructures.benchmark;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import edu.vt.rt.datastructures.lang.DistributedSet;
import edu.vt.rt.datastructures.util.DCounter;
import edu.vt.rt.datastructures.util.DSynchronizer;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.util.network.Network;

public class Set {

	private static int EXPERIMENT;
	private static DistributedSet<Integer> SET;
	private static String SET_CLASS;
	private static int NODE_COUNT;
	private static int NODE_ID;
	private static int NUM_ELEMS;
	private static int NUM_SPAN;	//Span of different numbers in set from 0
	private static long RUN_TIME;

	public static void main(String[] args) throws InterruptedException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		//Parse Arguments
		try {
			NODE_ID = Integer.parseInt(args[0]);
			SET_CLASS = args[1];
			EXPERIMENT = Integer.parseInt(args[2]);
			NUM_ELEMS = Integer.parseInt(args[3]);
			NUM_SPAN = Integer.parseInt(args[4]);
			RUN_TIME = Integer.parseInt(args[5]);
		} catch (NumberFormatException e) {
			System.out.println("Number expected");
			System.exit(0);
		}
		//Test variables	
		NODE_COUNT = Network.getInstance().nodesCount();
		Random generator = new Random(NODE_ID);
		boolean toggle = false;
		int containsCalls = 0, addCalls = 0, removeCalls = 0;
		SET = (DistributedSet<Integer>) Class.forName("edu.vt.rt.datastructures.lang." + SET_CLASS).getDeclaredConstructor(String.class).newInstance("LinkedList");
		DCounter callCount = new DCounter("callCount");
		DSynchronizer start = new DSynchronizer("startSync"), finished = new DSynchronizer("finishedSync");
		DSynchronizer[]	nodes = new DSynchronizer[NODE_COUNT-1];
		for (int i = 0; i < NODE_COUNT-1; i++) {
			nodes[i] = new DSynchronizer("node " + Integer.toString(i+1) + " finished sync");
		}

		//Start HyFlow 
		HyFlow.start(NODE_ID);

		//Populate system
		if (NODE_ID == 0) {
			start.create();
			SET.create();
			callCount.create();
			finished.create();
			//load up the list with some initial values
			Random gen = new Random(1988);
			for (int i = 0; i < NUM_ELEMS; i++) {
				SET.add(gen.nextInt(NUM_SPAN));
			}
			start.setReady();
		}
		else {
			//Make other nodes wait for master to populate data structure
			nodes[NODE_ID-1].create();
			while (!start.isReady()) {}
		}

		//Run Experiment
		long startTime = System.currentTimeMillis(), endTime = startTime;
		while (endTime - startTime < RUN_TIME) {
			int functionType = generator.nextInt(100);
			int value = generator.nextInt(NUM_SPAN);
			if (functionType >= EXPERIMENT) {	//Read function				
				SET.contains(value);
				containsCalls++;
			}
			else {	//Write function
				toggle = !toggle;	//Keep adds and removes even
				if (toggle) {
					SET.add(value);
					addCalls++;
				}
				else {
					SET.remove(value);
					removeCalls++;
				}
			}
			endTime = System.currentTimeMillis();
		}

		long totalTime = TimeUnit.SECONDS.convert(endTime-startTime, TimeUnit.MILLISECONDS);
		long callsPerSecond = (containsCalls + addCalls + removeCalls) / totalTime;

		if (NODE_ID == 0) {
			//Wait for all other nodes to report results
			int i = 0;
			while (i != NODE_COUNT-1) {
				if (nodes[i].isReady()) {
					i++;
				}
			}
			//Print out total results
			callsPerSecond += callCount.getCount();
			finished.setReady();
			System.out.println(EXPERIMENT + "\t" + NODE_COUNT + "\t" + NUM_ELEMS + "\t" + callsPerSecond);
			//Give other nodes time to determine tests are finished
			Thread.sleep(2000);
		}
		else {
			callCount.add(callsPerSecond);
			nodes[NODE_ID-1].setReady();
			while (!finished.isReady()) {}
		}
		System.exit(0);
	}

}
