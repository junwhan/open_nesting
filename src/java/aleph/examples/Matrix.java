/*
 * Aleph Toolkit
 *
 * Copyright 1997, Brown University, Providence, RI.
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

public class Matrix implements Serializable{

 public  int[][]  A ; // one of the two matrices
 public  int[][]  B ; // the other one
 public  int[][]  Result ; // matrix multiplication result

  // Initilize the input matrices. All the entries are 1
 public Matrix(int dim){
   A = new int[dim][dim];
   B = new int[dim][dim];
   Result = new int[dim][dim]; 
   for (int i = 0; i < dim; i++)
     for (int j = 0; j < dim; j++) {
       A[i][j]=1;
       B[i][j]=1; 
       Result[i][j]=0; 
     }
 }
  // Main Process
  public static void main(String[] args) {
   int n  = 2;			// n is the dimension of the matrix
   int m  = 1;			// m is the number of tests to run
   try {
     if ( args.length > 0)
       n = Integer.parseInt(args[0]);
     if ( args.length > 1)
       m = Integer.parseInt(args[1]);
   } catch (NumberFormatException e) {
     Aleph.error("usage: Matrix <#dimension> <#tests>");
     Aleph.exit(1);
   }

   for (int r = 0; r < m; r++) {

     if (r > 0) {
       System.out.println("Iteration " + r);
     }

     GlobalObject global = new GlobalObject( new Matrix(n) );
     UserThread   fork;
     Join         join   = new Join();

     long start = System.currentTimeMillis();

     // Start n*n threads to compute each entry of the result matrix

     Iterator e = PE.roundRobin();
     for (int i = 0; i < n; i++)
       for (int j = 0; j < n; j++) {
	 fork = new UserThread(global, i, j, n);
	 fork.start((PE)e.next(), join);
       }
     join.waitFor();
     // Print out the result
     Matrix matrix = (Matrix) global.open("r");
     System.out.println("The result matrix is: ");
     System.out.println(matrix);
     try { global.release(); } catch (AlephException x) {}
     System.out.println("Elapsed time: " +
			((double) (System.currentTimeMillis() - start)) / 1000.0
			+ " seconds");
     System.out.flush();
   }
 }
  // Thread to count the multiplication
  static public class UserThread extends RemoteThread implements Serializable{

    GlobalObject global;
    int row; // which row of the result
    int column; // which column
    int dimension; // the dimension of the matrix

    UserThread(GlobalObject global, int i, int j, int n) {
      this.global = global;
      row = i;
      column = j;
      dimension = n;
    }

   public void run() {
     int[] a = new int[dimension];
     int[] b = new int[dimension]; 

     Aleph.debug("RemoteThread(" + row + "" + column + ") started");
     // First read in the necessary values from the input matrices
     Matrix matrix = (Matrix) global.open("r");
     Aleph.debug("RemoteThread(" + row + "" + column + ") reading");
     for (int i =0 ; i < dimension ; i++) {
       a[i] = matrix.A[row][i];
       b[i] = matrix.B[i][column];
     }
     try { global.release(); } catch (AlephException e) {}
     // Compute the multiplication
     int sum = 0;
     for (int i = 0; i< dimension; i++){
       sum = sum + a[i]*b[i];
     }
     Aleph.debug("RemoteThread(" + row + "" + column + ") about to write");
     matrix = (Matrix) global.open("w");
     Aleph.debug("RemoteThread(" + row + "" + column + ") writing");
     matrix.Result[row][column] = sum;
     try { global.release(); } catch (AlephException e) {}
     Aleph.debug("RemoteThread(" + row + "" + column + ") done");
   }
  }
  public String toString() {
    StringBuffer s = new StringBuffer();
    int n = Result.length;
    for (int i = 0; i < n; i++) {
      s.append("[");
      for (int j = 0; j < n; j++)
        s.append(Integer.toString(Result[i][j]));
      s.append("]\n");
    }
    return s.toString();
  }
}

