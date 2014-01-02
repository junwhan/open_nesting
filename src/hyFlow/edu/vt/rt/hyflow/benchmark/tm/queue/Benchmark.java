package edu.vt.rt.hyflow.benchmark.tm.queue;

import edu.vt.rt.hyflow.util.io.Logger;
import edu.vt.rt.hyflow.util.network.Network;

public class Benchmark extends edu.vt.rt.hyflow.benchmark.tm.Benchmark{
	@Override
	protected Class[] getSharedClasses() {
		return new Class[] { Node.class };
	}
	
	QueueHandler queueHandler = new QueueHandler();

	@Override
	protected void createLocalObjects() {
		if(Network.getInstance().getID()==0)
			queueHandler.createQueue();
	}

	@Override
	protected String getLabel() {
		return "Queue-TM";
	}

	@Override
	protected int getOperandsCount() {
		return 1;
	}

	@Override
	protected Object randomId() {
		return new Integer((int)(Math.random()*localObjectsCount));
	}

	@Override
	protected void readOperation(Object... ids) {
		try {
			queueHandler.find((Integer)ids[0]);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void writeOperation(Object... ids) {
		QueueHandler queue = queueHandler;
		if(Math.random()>0.5){
			Logger.debug("[ADD]");
			try {
				queue.enqueue((Integer)ids[0]);
				elementsSum += (Integer)ids[0];
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}else{
			Logger.debug("[DEL]");
			try {
				if(queue.dequeue()!=null)
					elementsSum -= (Integer)ids[0];
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	int elementsSum = 0;
	@Override
	protected void checkSanity() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			Logger.debug("Sanity Check:" + ((Network.getInstance().getID()==0) ? queueHandler.sum() : "?") + "/" + elementsSum);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static String getServerId(String id) {
		return id.split("-")[0];
	}
}
