/*
 * Aleph Toolkit
 *
 * Copyright 1999, Brown University, Providence, RI.
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

package aleph.examples.cholesky;

import aleph.Aleph;
import aleph.AlephException;
import aleph.GlobalObject;
import aleph.Join;
import aleph.PE;
import aleph.RemoteThread;
import java.io.File;
import java.io.Serializable;
import java.util.Iterator;

/**
 * Cholesky factorization of a sparse symmetric positive definite matrix.
 * @author Liye Ma
 * @date   February 1999
 **/
public class Cholesky implements Constants {

  public static final boolean DEBUG = false;

  //the matrix upon which we'll perform the operation
  public SPDMatrix _L;

  private static File DEFAULT  = new File("examples/cholesky/data", "m50-0.1");
  private static File fileName = DEFAULT;

  /**
   * divide volumn k by the square root of its diagonal
   **/
  public void cdiv (Column ck) {
    int n = ck._val.length;
    ck._val[ck._id] = Math.sqrt(ck._val[ck._id]);
    for(int i=ck._id+1; i<n; i++) {
      ck._val[i] /= ck._val[ck._id];
    }
  }

  /**
   * factor matrix using multifrontal method
   **/
  public void multiFrontal () {
    if(DEBUG)
      Aleph.debug("Multifrontal Method selected");
    
    Iterator e = PE.roundRobin();
    int n = _L.getDim();        // matix dimension

    Column[] c = new Column[n]; // make columns int global objects
    GlobalObject[] gb = new GlobalObject[n];
    for(int i=0; i<n; i++) {
      c[i] = new Column();
      c[i]._id = i;
      c[i]._val = _L.getColumn(i);
      gb[i] = new GlobalObject(c[i]);
    }
    
    //construct a level array
    int maxlevel = 0;
    int[] level = new int[n];
    for(int i=0; i<n; i++) level[i] = 0;
    for(int i=1; i<n; i++)
      for(int k=0; k<i; k++)
	if(Math.abs(_L.getElem(i, k)) > epsilon)
	  if(level[i] < (level[k]+1)) {
	    level[i] = level[k]+1;
	    if(level[i] > maxlevel) 
	      maxlevel = level[i];
	  }
    if (DEBUG) {
      Aleph.debug("Level of Columns:");
      for(int i=0; i<n; i++)
	Aleph.debug(level[i] + "  ");
    }

    //factorizaton
    long start = System.currentTimeMillis();
    for(int i=0; i<=maxlevel; i++) {
      //cdiv
      for(int k=0; k<n; k++)
	if(level[k] == i) { //level i?
	  if (DEBUG)
	    Aleph.debug("CDivide "+k+" ...");
	  Column ctmp = (Column)gb[k].open("w");
	  cdiv(ctmp);
	  try {
	    gb[k].release();
	  } catch (AlephException x) {
	    Aleph.panic("Release failed: " + x);
	  }
	}
      //cmod
      Join join = new Join();
      for(int k=0; k<n; k++) 
	if(level[k] == i) { //level i?
	  if (DEBUG)
            Aleph.debug("CMode "+k+" ...");
	  for(int j=k+1; j<n; j++)
	    if(level[j] > level[k]) {
	      Moder m = new Moder(gb[j], gb[k]);
	      m.start((PE)e.next(), join);
	    }
	}
      join.waitFor();
    } 
    
    //write the columns back
    for(int i=0; i<n; i++) {
      Column ctmp = (Column)gb[i].open("r");
      _L.setColumn(ctmp._id, ctmp._val);
    }
    System.out.println("Elapsed Time: "+((double)System.currentTimeMillis()
                                         - start) / 1000.0);
    //clear the upper-triangular part
    for(int i=0; i<n; i++)
      for(int j=i+1; j<n; j++)
    	_L.setElem(i, j, 0.0);
  }

  /**
   * factor the matrix using Right-Looking method
   **/
  public void rightLooking() {
    if (DEBUG)
      Aleph.debug("rightLooking Method selected");

    Iterator e = PE.roundRobin();

    int n = _L.getDim();

    // retrieve columns and make them globalobjects
    Column[] c = new Column[n];
    GlobalObject[] gb = new GlobalObject[n];
    for(int i=0; i<n; i++) {
      c[i] = new Column();
      c[i]._id = i;
      c[i]._val = _L.getColumn(i);
      gb[i] = new GlobalObject(c[i]);
    }
    
    //factorizaton
    long start = System.currentTimeMillis();
    for(int k=0; k<n; k++) {
      //cdiv
      if(DEBUG)
        Aleph.debug("CDivide "+k+" ...");
      Column ctmp = (Column)gb[k].open("w");
      cdiv(ctmp);
      try {
	gb[k].release();
      } catch(AlephException x) {
	Aleph.panic("Release failed: " + x);
      }

      //cmod
      if (DEBUG)
        Aleph.debug("CMode " + k + " ...");
      Join join = new Join();
      for(int j=k+1; j<n; j++) {
	Moder m = new Moder(gb[j], gb[k]);
	m.start((PE)e.next(), join);
      }
      join.waitFor();
    }

    //write the columns back
    for(int i=0; i<n; i++) {
      Column ctmp = (Column)gb[i].open("r");
      _L.setColumn(ctmp._id, ctmp._val);
    }
    System.out.println("Elapsed Time: "+((double)System.currentTimeMillis()
                                         - start) / 1000.0);
    //clear the upper-triangular part
    for(int i=0; i<n; i++)
      for(int j=i+1; j<n; j++)
    	_L.setElem(i, j, 0.0);
  }

  /**
   * Cholesky factorization.  Default is multifrontal algorithm. If property
   * <code>aleph.cholesky.rightlooking</code> is defined, use right-looking
   * algorithm instead.
   **/
  public void cholesky() {
    if (Aleph.getProperty("aleph.cholesky.rightlooking") != null)
      rightLooking();
    else
      multiFrontal();
  }
    
  /**
   * main method
   **/
  public static void main (String[] args) {
    if (! DEFAULT.exists())
      Aleph.panic(DEFAULT.getAbsolutePath() + " does not exist");
    Cholesky d = new Cholesky();
    //allocate space
    d._L = new SPDMatrix();
    //load the data
    if (args.length > 0)
      fileName = new File(args[0]);
    d._L.loadFile(fileName.toString());
    d.cholesky();
    //output the result to a file
    d._L.saveFile(fileName + ".res");
    System.out.println("done");
  }

  /**
   * Inner class that does "cmod" operation
   **/
  static class Moder extends RemoteThread {
    public GlobalObject _source, _dest; // two columns
    Moder (GlobalObject d, GlobalObject s) { // contructor
      _source = s;
      _dest = d;
    }
    //do the cmod operation
    public void run () {
      Column source, dest;
      source = (Column)_source.open("r");
      dest = (Column)_dest.open("w");
      int n = source._val.length;
      double tmp1, tmp2, fac;
      fac = source._val[dest._id];
      for(int i=0; i<n; i++) {
	tmp1 = dest._val[i]; 
	tmp2 = source._val[i]; 
	dest._val[i] = tmp1-tmp2*fac;
      }
      try {
	_source.release();
	_dest.release();
      } catch(AlephException e) {
	Aleph.panic("Release failed: " + e);
      }
    }
  }

  /**
   * Inner class that represents a matrix column
   **/
  static class Column implements Serializable {
    public int _id;
    public double[] _val;
  }
}
