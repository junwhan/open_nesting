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
import aleph.RemoteThread;
import java.io.Serializable;
import java.util.Iterator;

/**
 * N-Queens benchmark.
 **/
public class Queens implements Serializable {

  private static final int N = 8;
  public int[]     row;
  public boolean[] col;
  public boolean[] left;
  public boolean[] right;

  /**
   * Main program.
   * @param arg[0] is recursion depth.
   **/ 
  public static void main(String[] args) {
    int depth = 0;		// default = sequential
    try {
      if (args.length > 0)
	depth = Integer.parseInt(args[0]);
    } catch (Exception e) {
      Aleph.error("Could not parse recursion depth " + args[0]);
      return;
    }
  
    Queens solution = new Queens();
    long start = System.currentTimeMillis();
    for (int i = 0; i < N; i++)
      solution.addQueen(0, i, depth);
    System.out.println("Elapsed time is "
		       + ((double) (System.currentTimeMillis() - start)/1000.0)
		       + " seconds");
  }

  public Queens() {
    row = new int[N];
    col = new boolean[N];
    for (int i = 0; i < col.length; i++)
      col[i] = true;
    left  = new boolean[2*N];
    right = new boolean[2*N];
    for (int i = 0; i < 2*N; i++)
      left[i] = right[i] = true;
  }

  public Queens(Queens solution, int r, int c) {
    row = new int[N];
    col = new boolean[N];
    left  = new boolean[2*N];
    right = new boolean[2*N];
    System.arraycopy(solution.row,   0, this.row,   0, N);
    System.arraycopy(solution.col,   0, this.col,   0, N);
    System.arraycopy(solution.left,  0, this.left,  0, 2*N);
    System.arraycopy(solution.right, 0, this.right, 0, 2*N);
    this.row[r] = c;
    this.col[c] = false;
    this.setLeftDiag(r, c, false);
    this.setRightDiag(r, c, false);
  }

  public String toString() {
    String result = "[" + row[0];
    for (int i = 1; i < row.length; i++)
      result = result + " " + row[i];
    return result + "]";
  }

  void addQueen(int r, int c, int depth) {
    if (depth == 0) {
      /* copy partial solution */
      Queens solution = new Queens(this, r, c);
	
      int nextRow = r+1;

      for (int i = 0; i < N; i++) {
	if (solution.col[i] && solution.getLeftDiag(nextRow, i)
	    && solution.getRightDiag(nextRow, i)) {
	  solution.row[nextRow] = i;
	  if (nextRow == N-1) {
	    System.out.println(solution);
	    return; /* no other solutions possible */
	  } else {
	    solution.addQueen(nextRow, i, 0);
	    col[c] = true;
	    setLeftDiag(r, c, true);
	    setRightDiag(r, c, true);
	  }
	}
      }
      return;
    }

    Queens solution = new Queens(this, r, c);

    int nextRow = r+1;

    Join join = new Join();
    Iterator e = PE.roundRobin();
    for (int i = 0; i < N; i++) {
      if (solution.col[i] && solution.getLeftDiag(nextRow, i)
	  && solution.getRightDiag(nextRow, i)) {
	solution.row[nextRow] = i;
	if (nextRow == N-1) {
	  System.out.println(solution);
	  return;		// no other solutions possible
	} else {		// fork and return
	  Worker worker = new Worker(solution, nextRow, i, depth-1);
	  worker.start((PE) e.next(), join);
	}
      }
    }
    join.waitFor();
  }

  boolean getLeftDiag(int i, int j) {
    return left[i+j];
  }

  boolean getRightDiag(int i, int j) {
    return right[N+i-j-1];
  }

  void setLeftDiag(int i, int j, boolean value) {
    left[i+j] = value;
  }

  void setRightDiag(int i, int j, boolean value) {
    right[N+i-j-1] = value;
  }

  static class Worker extends RemoteThread {
    Queens solution;
    int r, c, depth;
    Worker(Queens solution, int r, int c, int depth) {
      this.solution = solution;
      this.r = r;
      this.c = c;
      this.depth = depth;
    }
    public void run() {
      try {
	solution.addQueen(r, c, depth);
      }	catch (Exception e) {
	Aleph.error(e);
	System.exit(-1);
      }
    }
  }

}
