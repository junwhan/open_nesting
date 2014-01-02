package edu.vt.rt.hyflow.benchmark;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.util.network.Network;


public class HyFlowBasicNode {

	void process(){
		System.out.println("Doing something");
		// Make something useful here
		System.out.println("Something Done");	
	}
	
	public void startUp(Integer nodeId) throws InterruptedException, FileNotFoundException, IOException{
		System.out.println("Starting " + nodeId);
		
		HyFlow.readConfigurations();
		Network.getInstance().setID(nodeId);
		HyFlow.start(nodeId);
		
		System.out.println("Started");
		
		new Thread(){
			public void run() {
				try {	Thread.sleep(5000);	} catch (InterruptedException e) {}
				process();
			};
		}.start();
		
		while(true){
			Thread.sleep(2000);
		}		
	}
}
