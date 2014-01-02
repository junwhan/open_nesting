/*
 * Aleph Toolkit
 *
 * Copyright 1999, Brown University, Providence, RI.
 * 
 *                         All Rights Reserved
 * 
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose other than its incorporation into a commercial
 * product is hereby granted without fee, provided that the above copyright
 * notice appear in all copies and that both that copyright notice and this
 * permission notice appear in supporting documentation, and that the name of
 * Brown University not be used in advertising or publicity pertaining to
 * distribution of the software without specific, written prior permission.
 * 
 * BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
 * INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR ANY
 * PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY BE LIABLE FOR ANY
 * SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
 * CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE
 */

package aleph.examples;

import aleph.Aleph;
import aleph.AlephException;
import aleph.GlobalObject;
import aleph.Join;
import aleph.PE;
import aleph.RemoteThread;
import java.io.Serializable;
import java.util.Iterator;

public class GlobalObjectLatency implements Serializable {

  public long value;

  public static void main(String[] args) {
    int count = 1;
    int round = 1;
    try {
	switch (args.length) {
	  default:
	  case 2: round = Integer.parseInt(args[1]); 
	  case 1: count = Integer.parseInt(args[0]);
	  case 0: break; 
	}
    } catch (NumberFormatException e) {
      Aleph.error("usage: GlobalObjectLatency [<#increments/round>] [<#rounds>]" );
      Aleph.exit(1);
    }

    System.out.println("Test is : " + round + " round(s) of " + count + " incrementation(s)");
    System.out.flush();
    
    GlobalObject global = new GlobalObject( new GlobalObjectLatency() );
    UserThread   fork   = new UserThread(count, round, PE.numPEs(), global);
    Join         join   = new Join();
    long start = System.currentTimeMillis();
    for (Iterator e = PE.allPEs(); e.hasNext(); ) {
      fork.start((PE) e.next(), join);
    }
    join.waitFor();
    GlobalObjectLatency counter = (GlobalObjectLatency) global.open("r");
    System.out.println(PE.numPEs() + " PEs, Final value is " + counter.value);
    System.out.println("Elapsed time: " +
		       ((double) (System.currentTimeMillis() - start)) / 1000.0
		       + " seconds");
  }

  static class UserThread extends RemoteThread {

    GlobalObject global;
    int count;
    int round;
    int nbPEs;

      UserThread(int count, int round, int nbPEs, GlobalObject global)
	  {
	      this.count = count;
	      this.round = round;
	      this.nbPEs = nbPEs;
	      this.global = global;
	  }
      
      public void runRound (int count) throws AlephException
	  {
	      for (int i = 0; i < count; i++) {
		  GlobalObjectLatency counter = (GlobalObjectLatency) global.open("w");
		  counter.value++;
		  global.release();
	      }
	  }
      
      public void run() {
	  try{
	      for (int i = 0; i < round; i++) {
		  runRound(count);
		  long temp;
		  do {
			// Use of Events should be interesting here
			// provided some that some local predicate can
			// be remotely evaluated.
		  GlobalObjectLatency counter = (GlobalObjectLatency) global.open("r");
		  temp = counter.value;
		  global.release();
		  Thread.yield(); // allows the server to got the lock
		  } while (temp < (i+1)*count*nbPEs);
	      }
	  } catch (AlephException e) {}
    }
  }
}

/* Benchs result with two PEs:

   
aleph.examples.GlobalObjectLatency  1 1

Test is : 1 round(s) of 1 incrementation(s)
2 PEs, Final value is 2
Elapsed time: 299.788 seconds

aleph.examples.GlobalObjectLatency  1 2

Test is : 2 round(s) of 1 incrementation(s)
2 PEs, Final value is 4
Elapsed time: 182.826 seconds
aleph.examples.GlobalObjectLatency 1 3


Test is : 3 round(s) of 1 incrementation(s)
PE.0: Starting...
PE.0: starting round 0
PE.0: round 0 done
PE.1: Starting...
PE.1: starting round 0
PE.1: round 0 done
PE.1: starting round 1
PE.1: round 1 done
PE.0: starting round 1
PE.0: round 1 done
PE.0: starting round 2
PE.1: starting round 2
PE.0: round 2 done
PE.1: round 2 done
PE.1... Done
PE.0... Done
2 PEs, Final value is 6
Elapsed time: 17.323 seconds
 */
