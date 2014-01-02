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

import aleph.Aleph;
import aleph.Join;
import aleph.PE;
import aleph.RemoteFunction;
import java.util.Iterator;

/** 
 * Trivial RemoteFunction demo.
 *
 * @author  Maurice Herlihy
 * @date    November 97
 **/

public class HelloFunction {

  static class HelloThread extends RemoteFunction{
    public Object run() {
      return "Hello world from " + PE.thisPE() + " of " + PE.numPEs();
    }
  }

  public static void main(String[] args) {
    HelloThread thread = new HelloThread();
    Join join = new Join();
    long start = System.currentTimeMillis();
    for (Iterator e = PE.allPEs(); e.hasNext(); )
      thread.start((PE) e.next(), join);
    while(join.hasNext())
      System.out.println((String) join.next());
    System.out.println("Elapsed time: " +
		       ((double) (System.currentTimeMillis() - start)) / 1000.0);
  }
}
