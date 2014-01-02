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

/**
 * This example stresses Aleph's ability to manage enormous global objects.
 * @author Maurice Herlihy
 * @date   August 1997
 **/

public class BigCounter implements Serializable{

  public long[] value;

  public BigCounter() {
    value = new long[aleph.comm.udp.Constants.MAX_DATA_SIZE]; // requires multiple datagram packets
    value[0] = 0;
  }

  public static void main(String[] args) {
    int count  = 1;
    try {
      if ( args.length > 0)
	count = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      Aleph.error("usage: BigCounter <#increments>");
      Aleph.exit();
    }
    GlobalObject global = new GlobalObject( new BigCounter() );
    UserThread   fork   = new UserThread(count, global);
    Join         join   = new Join();
    long start = System.currentTimeMillis();
    for (Iterator e = PE.allPEs(); e.hasNext(); )
	fork.start((PE) e.next(), join);
    join.waitFor();
    BigCounter counter = (BigCounter) global.open("r");
    System.out.println("Final value is " + counter.value[0]);
    System.out.println("Elapsed time: " +
		       ((double) (System.currentTimeMillis() - start)) / 1000.0
		       + " seconds");
  }

  static class UserThread extends RemoteThread {

    GlobalObject global;
    int count;

    UserThread(int count, GlobalObject global) {
      this.count = count;
      this.global = global;
    }

    public void run() {
      try{
	for (int i = 0; i < count; i++) {
	  BigCounter counter = (BigCounter) global.open("w");
	  counter.value[0]++;
	  global.release();
	}
      } catch (AlephException e) {}
    }
  }
}
