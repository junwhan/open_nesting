package edu.vt.rt.datastructures;

import edu.vt.rt.datastructures.lang.*;
import edu.vt.rt.datastructures.util.*;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.util.network.Network;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		HyFlow.start(Integer.parseInt(args[0]));
	
		DistributedSet<Integer> set = new AtomicHashSet<Integer>("temp");
		DSynchronizer start = new DSynchronizer("start"), finish = new DSynchronizer("finish");
		
		//Master code
		if (Network.getInstance().getID() == 0) {
			start.create();
			set.create();
			finish.create();
			for (int i = 0; i < 10; i++) {
				set.add(i);
			}
			start.setReady();
			while(!finish.isReady()) {}
			System.exit(0);
		}
		else {
			while (!start.isReady()) {}
			int t = 9;
			System.out.println(set.remove(t));
			System.out.println(set.contains(t));
			finish.setReady();
			Thread.sleep(1000);
			System.exit(0);
		}

	}

}
