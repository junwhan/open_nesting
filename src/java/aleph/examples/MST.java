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
package aleph.examples;

import aleph.Aleph;
import aleph.Join;
import aleph.PE;
import aleph.RemoteFunction;
import aleph.RemoteThread;
import aleph.comm.*;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Hashtable;
import java.util.Random;

/**
 * Minimum Spanning Tree
 *
 * J. Bentley. "A Parallel Algorithm for Constructing Minimum Spanning Trees."
 * J. of Algorithms 1:51-59 (1980).
 *
 * Adapted from the Cid benchmark written by Martin C. Carlisle
 *
 * @author Weisheng Xu
 * @date   Nov 1997
 */
public class MST implements Serializable {

  private static Random random = new Random(1234); // for random graphs
  private static final int RANGE=2048; // largest weight

   // the static data member which will hold the different parts of the graph
   // on different PEs
  static Vertex myVertexList = null;

  static int numvert;
  static int numproc;
  static int perproc;

 public static void main(String args[]) {
   numproc = PE.numPEs();
   numvert = 64;		// default number of vertexes

   try {
     if (args.length > 0) {
       int num = Integer.parseInt(args[0]);
       if (num > 0)
	 numvert = num;
     }
   } catch (NumberFormatException e) {
     Aleph.error("usage: MST <#num_of_vertics>");
     Aleph.exit();
   }

   // convert to a power of 2
   numproc = toPowerOf2(numproc);
   numvert = toPowerOf2(numvert);

   // in case we have fewer vertices
   if (numvert < numproc)
     numproc = numvert;

   // show the numproc & numvert
   // numvert may be different from the number given
   System.out.println("numproc="+numproc+" numvert="+numvert+"\n");
 
   perproc = numvert / numproc;

   MST mst = new MST();

   System.out.println("Make graph");
   long start = System.currentTimeMillis();

   try {
     mst.makeGraph();
   } catch (Exception e) {
     e.printStackTrace();
   }

   System.out.println("Elapsed time: " +
		      ((double) (System.currentTimeMillis() - start)) / 1000.0
		      + " seconds\n");
   System.out.flush();

   System.out.println("Compute mst");
   start = System.currentTimeMillis();

   long dist = 0;
   try {
     dist = mst.computeMst();
   } catch (Exception e) {
     e.printStackTrace();
     Aleph.exit();
   }
   System.out.println("Elapsed time: " +
		      ((double) (System.currentTimeMillis() - start)) / 1000.0
		      + " seconds\n");
   System.out.println("MST has cost " + dist);
   System.out.flush();
   Aleph.exit();
 }

   // return the closest number which is a power of 2
   static int toPowerOf2(int value) {
     int guess = 1;
     while (guess <= value)
       guess = (guess << 1);
     return (guess >> 1);
   }

   // randomly generate a graph of numvert nodes
   void makeGraph() {

     // we don't really return a global Graph object; the graph is distributed
     // uniformly on numproc PEs, each part denoted by -myVertexList- 
     // respectively

     System.out.println("phase 1");
     InitThread fork = new InitThread(numvert, perproc);
     Join       join = new Join();
     Iterator e = PE.roundRobin();
     for (int i = 0; i < numproc; i++)
       fork.start((PE) e.next(), join);
     join.waitFor();

     System.out.println("phase 2");
     AddEdgeThread fork1 = new AddEdgeThread(numvert, perproc);
     e = PE.allPEs();
     for (int i = 0; i < numproc; i++)
       fork1.start((PE) e.next(), join);
     join.waitFor();

   }

   // create numvert/numproc nodes on each PE
   // the nodes on each PE are put in a linked list
   class InitThread extends RemoteThread {
     int numvert;
     int perproc;

     public InitThread(int numvert, int perproc) {
       this.numvert = numvert;
       this.perproc = perproc;
     }

     public void run() {
       // get my index
       int j = PE.thisPE().getIndex();

       Vertex v0 = null;

       if (perproc > 1) {
         v0 = new Vertex(Integer.MAX_VALUE, null, new Hashtable(), j*perproc+perproc-1);

         Vertex v;
         for (int i=perproc-2; i>0; i--) {
           v = new Vertex(Integer.MAX_VALUE, v0, new Hashtable(), j*perproc+i);
           v0 = v;
         }
       }

       myVertexList = new Vertex(Integer.MAX_VALUE, v0, new Hashtable(), j*perproc);
     }

   }

   // insert edge info to each node's hashtable
   class AddEdgeThread extends RemoteThread {
     int numvert;
     int perproc;
     public AddEdgeThread(int numvert, int perproc) {
       this.numvert = numvert;
       this.perproc = perproc;
     }
     
     public void run() {
       // get my index
       Vertex vj = myVertexList;
       for (; vj != null; vj = vj.next) {
         for (int i = 0; i < numvert; i++) {
            if (i != vj.myNo.intValue()) {
               int dist = computeDist(i, vj.myNo.intValue());
               // use unique vertex number as key
               (vj.edgeHash).put(new Integer(i), new Integer(dist));
            }
         }
       }
     }
   }


   int computeDist(int i, int j) {
     return(Math.abs(random.nextInt() % RANGE) + 1);
   }

   /**
    * BlueReturn class
    */
   class BlueReturn implements Serializable {
     public Vertex vert;
     public int dist;

     public BlueReturn(int dist, Vertex vert) {
        this.dist = dist;
        this.vert = vert;
     }

     public BlueReturn() {
        this.dist = Integer.MAX_VALUE;
        this.vert = null;
     }
   }

   // compute the MST; parts of the graph are located on the PEs where the 
   // computing will be done
   long computeMst() {
     long cost = 0;

     // insert first node on PE[0]
     Vertex inserted = myVertexList;
     myVertexList = inserted.next;
     numvert--;

     // announce insertion and find next one
     // since inserted won't be changed, we use a simple Java object instead
     // of a GlobalObject
     while (numvert > 0) {
        BlueRuleThread fork0 = new BlueRuleThread(
				   new Vertex(inserted), numproc, 0);
        BlueReturn br = (BlueReturn) fork0.run();
        inserted = br.vert;
        cost += br.dist;

        if (inserted == null) 
          System.out.println("inserted=null! numvert="+numvert+" cost="+cost);

        numvert--;
     }

     return cost;
   }

   class BlueRuleThread extends RemoteFunction {
     Vertex inserted;
     int nproc;
     int pn;

     public BlueRuleThread(Vertex inserted, int nproc, int pn) {
        this.inserted = inserted;
        this.nproc = nproc;
        this.pn = pn;
      }

      public Object run() {

        BlueRuleThread fork, fork1;
        Join join, join1; 
        BlueReturn retleft = new BlueReturn();
        BlueReturn retright = new BlueReturn(); 

        if (nproc > 1) {
          fork = new BlueRuleThread(inserted, nproc/2, pn+nproc/2);
	  join = new Join();
          fork.start(PE.getPE(pn+nproc/2), join);
	  try {
	    retleft = (BlueReturn) join.next();
          } catch (NoSuchElementException e) {
	    System.out.println("left NoSuchElementException: "+e);
          }

          fork1 = new BlueRuleThread(inserted, nproc/2, pn);
	  join1 = new Join();
          fork1.start(PE.thisPE(), join1);
          try {
            retright = (BlueReturn) join1.next();
          } catch (NoSuchElementException e) {
            System.out.println("right NoSuchElementException: "+e);
          }
          if (retleft.dist < retright.dist) {
              retright.dist = retleft.dist;
              retright.vert = retleft.vert;
          }

          // return retright
	  return (Object)retright;

        } else {
	  if (myVertexList != null) {
            if (inserted.myNo.equals(myVertexList.myNo)) {
               // remove it
               myVertexList = myVertexList.next;
            }
	  }

          return (Object) blueRule(inserted, myVertexList);
        }
      }


      BlueReturn blueRule(Vertex inserted, Vertex vlist) {

        if (vlist == null) {
           return new BlueReturn();
        }

        Vertex prev = vlist;
	// get copy of the first vertex
        BlueReturn retval = new BlueReturn(vlist.mindist, new Vertex(vlist));
        Hashtable hash = vlist.edgeHash;

        Integer ind = (Integer) hash.get(inserted.myNo);
        if (ind != null) {
           int dist = ind.intValue();
           if (dist < retval.dist) {
              vlist.mindist = dist;
              retval.dist = dist;
           }
        } else {
           System.out.println("Not found: "+vlist.myNo+"<->"+inserted.myNo);
           Aleph.exit(0);
        }

        int count = 0;
        // we are guaranteed that inserted is not the first one on the list
        for (Vertex tmp=vlist.next; tmp!=null; prev=tmp, tmp=tmp.next) {
           count++;
           if (inserted.myNo.equals(tmp.myNo)) {      // remove it
              prev.next = tmp.next;
           } else {
              hash = tmp.edgeHash;
              int dist0 = tmp.mindist;
              ind = (Integer) hash.get(inserted.myNo);
              if (ind != null) {
                 int dist = ind.intValue();
                 if (dist < dist0) {
                    tmp.mindist = dist;
                    dist0 = dist;
                 }
              } else
                 System.out.println("Not found: "+tmp.myNo+"<->"+inserted.myNo);
              if (dist0 < retval.dist) {
                retval.vert = new Vertex(tmp);    // copy the vertex
                retval.dist = dist0;
              }
           }
        }

        return retval;
      }
   }
class Vertex implements Serializable {
  public int mindist;
  public Integer myNo;		// unique vertex number
  public transient Vertex next;	// don't copy
  public transient Hashtable edgeHash; // don't copy

  public Vertex(int mindist, Vertex next, Hashtable edgeHash, int myNo) {
    this.mindist = mindist;
    this.next = next;
    this.edgeHash = edgeHash;
    this.myNo = new Integer(myNo);
  }

  // with this constructor, only 'mindist' & 'myNo' are copied
  // fields 'next' & 'edgeHash' are assigned null 
  // so that we can avoid unnecessary data communication during compustMST
  public Vertex(Vertex v) {
    this.mindist = v.mindist;
    this.next = null;
    this.edgeHash = null;
    this.myNo = v.myNo;
  }
}
}


