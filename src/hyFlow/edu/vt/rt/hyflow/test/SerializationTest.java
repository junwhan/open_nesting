package edu.vt.rt.hyflow.test;

import java.io.IOException;

import aleph.Message;
import edu.vt.rt.hyflow.HyFlow;
import edu.vt.rt.hyflow.benchmark.tm.loan.LoanAccount;
import edu.vt.rt.hyflow.util.network.Network;

public class SerializationTest {
	private static int SIZE = 10;
	public static int TRIALS = 100;
	public static void main(String[] args) {
		
		Object[] o = new Object[SIZE];
		for(int i=0; i<o.length; i++)
			o[i] = new LoanAccount();
		System.out.println(getObjectSize(LoanAccount.class) * SIZE);

		int id = Integer.parseInt(args[0]);
		HyFlow.start(id);
		if(id==0)
			try {
				long sum = System.currentTimeMillis();
				for(int i=0; i<TRIALS; i++)
					new SendObject(o).send(Network.getAddress("1"));
				long now = System.currentTimeMillis();
				
				System.out.println(now + " " + sum + " " + (now-sum));
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}
	
	

	  /**
	  * Return the approximate size in bytes, and return zero if the class
	  * has no default constructor.
	  *
	  * @param aClass refers to a class which has a no-argument constructor.
	  */
	  public static long getObjectSize( Class aClass ){
	    long result = 0;

	    //if the class does not have a no-argument constructor, then
	    //inform the user and return 0.
	    try {
	      aClass.getConstructor( new Class[]{} );
	    }
	    catch ( NoSuchMethodException ex ) {
	      System.err.println(aClass + " does not have a no-argument constructor.");
	      return result;
	    }

	    //this array will simply hold a bunch of references, such that
	    //the objects cannot be garbage-collected
	    Object[] objects = new Object[fSAMPLE_SIZE];

	    //build a bunch of identical objects
	    try {
	      Object throwAway = aClass.newInstance();

	      long startMemoryUse = getMemoryUse();
	      for (int idx=0; idx < objects.length ; ++idx) {
	        objects[idx] = aClass.newInstance();
	      }
	      long endMemoryUse = getMemoryUse();

	      float approximateSize = ( endMemoryUse - startMemoryUse ) /100f;
	      result = Math.round( approximateSize );
	    }
	    catch (Exception ex) {
	      System.err.println("Cannot create object using " + aClass);
	    }
	    return result;
	  }

	  // PRIVATE //
	  private static int fSAMPLE_SIZE = 100;
	  private static long fSLEEP_INTERVAL = 100;

	  private static long getMemoryUse(){
	    putOutTheGarbage();
	    long totalMemory = Runtime.getRuntime().totalMemory();

	    putOutTheGarbage();
	    long freeMemory = Runtime.getRuntime().freeMemory();

	    return (totalMemory - freeMemory);
	  }

	  private static void putOutTheGarbage() {
	    collectGarbage();
	    collectGarbage();
	  }

	  private static void collectGarbage() {
	    try {
	      System.gc();
	      Thread.currentThread().sleep(fSLEEP_INTERVAL);
	      System.runFinalization();
	      Thread.currentThread().sleep(fSLEEP_INTERVAL);
	    }
	    catch (InterruptedException ex){
	      ex.printStackTrace();
	    }
	  }	
}

class SendObject extends Message{
	static int count;
	Object o;
	SendObject(Object o){
		this.o = o;
	}
	
	@Override
	public void run() {
		count++;
		if(count==SerializationTest.TRIALS)
			System.exit(0);
	}
}