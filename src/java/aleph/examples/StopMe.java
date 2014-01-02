 /*
 * Aleph Toolkit
 *
 * Copyright 1997, Brown University, Providence, RI.
 * 
 *                         All Rights Reserved
 * 
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose other than its incorporation into a
 * commercial product is hereby granted without fee, provided that the
 * above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation, and that the name of Brown University not be used in
 * advertising or publicity pertaining to distribution of the software
 * without specific, written prior permission.
 * 
 * BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
 * INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR ANY
 * PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY BE LIABLE FOR
 * ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package aleph.examples;

/**
 * Run an infinite loop at each PE.  Useful for testing console stop button.
 * Discretion advised.
 **/

import aleph.PE;
import aleph.Join;
import aleph.RemoteThread;
import java.util.Iterator;

public class StopMe {

  static class HelloThread extends RemoteThread {
    public void run() {
      for (int i = 1000000; i > 0; i--) {
	System.out.println( i + " bottles of beer on the wall");
	System.out.println( i + " bottles of beer");
	System.out.println( "take one down and pass it around");
	System.out.println( (i-1) + " bottles of beer on the wall");
	System.out.println();
	System.out.flush();
      }
    }
  }

  public static void main(String[] args) {
    HelloThread thread = new HelloThread();
    Join join = new Join();
    long start = System.currentTimeMillis();
    for (Iterator e = PE.allPEs(); e.hasNext(); )
      thread.start((PE) e.next(), join);
    join.waitFor();
  }
}
