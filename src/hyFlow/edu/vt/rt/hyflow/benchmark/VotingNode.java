package edu.vt.rt.hyflow.benchmark;

import java.io.IOException;

import edu.vt.rt.hyflow.util.network.Network;
import aleph.Message;

public class VotingNode extends HyFlowBasicNode {

	static class Hi extends Message{
		@Override
		public void run() {
			System.out.println("Hi from " + from);
			try {
				new HiBack().send(from);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static class HiBack extends Message{
		@Override
		public void run() {
			System.out.println("Hi Back from " + from);
			synchronized (returned) {
				returned.notifyAll();
			}
		}
	}

	private static Boolean returned = new Boolean(true);
	
	@Override
	void process() {
		System.out.println("Saying Hi");
		if(Network.getInstance().getID()!=0)
			try {
				new Hi().send(Network.getAddress("0"));
				System.out.println("Wait for the reply");
				try {
					returned.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Notified with the reply");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	

	public static void main(String[] args) throws NumberFormatException, Throwable {
		Integer nodeId = Integer.parseInt(args[0]);
		new VotingNode().startUp(nodeId);
	}

}
