/*
 * Aleph Toolkit
 *
 * Copyright 1999 Brown University, Providence, RI.
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
import aleph.Constants;
import aleph.GlobalObject;
import aleph.Join;
import aleph.PE;
import aleph.RemoteThread;
import java.io.Serializable;
import java.util.Iterator;

/**
 * This example is Travelling Saleman Problem
 * @author Zhihao Zhang
 * @date   November 1997
 **/

public class TSP {
  private static final double M_E   = 2.7182818284590452354;
  private static final double M_E2  = 7.3890560989306502274;
  private static final double M_E3  = 20.08553692318766774179;
  private static final double M_E6  = 403.42879349273512264299;
  private static final double M_E12 = 162754.79141900392083592475;

  public GlobalObject _root;

  TSP(int nodeNum) {
    _root = buildTree(nodeNum, 0, 0, PE.numPEs(), 0.0, 1.0, 0.0, 1.0);
  }

  // Return an estimate of median of n values distributed in [min,max)
  private static double median(double min, double max, int n) {
    double t;
    double retval;
      
    t = Math.random(); // in [0.0,1.0) 
    if (t > 0.5) {
      retval = Math.log(1.0-(2.0*(M_E12-1)*(t-0.5)/M_E12))/12.0;
    }
    else {
      retval = -Math.log(1.0-(2.0*(M_E12-1)*t/M_E12))/12.0;
    }
    // We now have something distributed on (-1.0,1.0) 
    retval = (retval+1.0) * (max-min)/2.0;
    retval = retval + min;
    return retval;
  }
  
  // Get double uniformly distributed over [min,max)
  private static double uniform(double min, double max) {
    double retval;
    
    retval = Math.random(); // in [0.0,1.0)
    retval = retval * (max-min);
    return retval + min;
  }
  
  // Builds a 2D tree of n nodes in specified range with dir as primary
  // axis (0 for x, 1 for y)
  public static GlobalObject buildTree(int n, int dir, int peNo, int numPE, double minX, 
				       double maxX, double minY, double maxY) 
  {
    double med;
    Tree t = new Tree();
    Join jv = new Join();
    PE me = PE.thisPE();
    GlobalObject gb;
    int leftN, rightN;
    
    if (Aleph.verbosity(Constants.LOQUACIOUS))
      System.out.println("n:"+n+" dir:"+dir+" peNo:"+peNo+" numPE:"+numPE);
    if (n == 0) return null;
    gb = new GlobalObject(t);
    
    t = (Tree) gb.open("w");
    leftN = (n-1)/2;
    rightN = n-1-leftN;
    if (dir == 1) {
      dir = 0;
      med = median(minX, maxX, n);

      if (leftN == 0) 
	t._left = null;
      else {
	if (me.equals(PE.getPE(peNo+numPE/2))) {  // Local PE
	  if (Aleph.verbosity(Constants.LOQUACIOUS))
	    System.out.print("Left ");
	  t._left = buildTree(leftN, dir, peNo+numPE/2, numPE/2, minX, med, minY, maxY);
	} else {  // Remote PE
	  try {
	    gb.release();
	  } catch (AlephException ale) {
	    Aleph.warning("Release failed: " + ale.getMessage());
	  }
	  Builder builder = new Builder(gb, leftN, dir, peNo+numPE/2, numPE/2, 
					minX, med, minY, maxY);
	  builder.start(PE.getPE(peNo+numPE/2), jv);
	  jv.waitFor();
	  t = (Tree) gb.open("w");
	}
      }
      
      if (rightN == 0)
	t._right = null;
      else {
	if (Aleph.verbosity(Constants.LOQUACIOUS))
	  System.out.print("Right ");
	// Right subtree always in local PE
	t._right = buildTree(rightN, dir, peNo, numPE/2, med, maxX, minY, maxY);
      }

      t._x = med;
      t._y = uniform(minY, maxY);

    } else {
      dir = 1;
      med = median(minY, maxY, n);

      if (leftN == 0) 
	t._left = null;
      else {
	if (me.equals(PE.getPE(peNo+numPE/2))) {  // Local PE
	  if (Aleph.verbosity(Constants.LOQUACIOUS))
	    System.out.print("Left ");
	  t._left = buildTree(leftN, dir, peNo+numPE/2, numPE/2, minX, maxX, minY, med);
	} else {  // Remote PE
	  try {
	    gb.release();
	  } catch (AlephException ale) {
	    Aleph.warning("Release failed: " + ale.getMessage());
	  }
	  Builder builder = new Builder(gb, leftN, dir, peNo+numPE/2, numPE/2, 
					minX, maxX, minY, med);
	  builder.start(PE.getPE(peNo+numPE/2), jv);
	  jv.waitFor();
	  t = (Tree) gb.open("w");
	}
      }

      if (rightN == 0)
	t._right = null;
      else {
	if (Aleph.verbosity(Constants.LOQUACIOUS))
	  System.out.print("Right ");
	// Right subtree always in local PE
	t._right = buildTree(rightN, dir, peNo, numPE/2, minX, maxX, med, maxY);
      }

      t._y = med;
      t._x = uniform(minX, maxX);
    }

    t._size = n;
    t._next = null;
    t._prev = null;
    jv.waitFor();
    
    try {
      gb.release();
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }
    return gb;
    //new GlobalObject(t);      
  }

  // sling tree nodes into a list -- requires root to be tail of list
  // only fills in next field, not prev
  private static GlobalObject makeList(GlobalObject root) 
  {
    Tree t;
    GlobalObject gleft;
    GlobalObject gright;
    GlobalObject gttmp;
    GlobalObject retval = root;
    Tree ttmp;
    Join jv = new Join();
 
    if (root == null) return null;
    t = (Tree) root.open("w");
    gleft = makeList(t._left); /* head of left list */
    gright = makeList(t._right); /* head of right list */
    
    if (gright != null) {
      retval = gright;
      gttmp = t._right;
      ttmp = (Tree) gttmp.open("w");
      ttmp._next = root;
      try {
	gttmp.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
    }
    if (gleft != null) {
      retval = gleft;
      gttmp = t._left;
      ttmp = (Tree) gttmp.open("w");
      ttmp._next = (gright==null)? root : gright;
      try {
	gttmp.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
    }
    
    t._next = null;
    try {
      root.release();
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }
    return retval;
  }
 
  public void printTree()
  {
    Tree t = (Tree) _root.open("r");
    if (Aleph.verbosity(Constants.LOQUACIOUS)) {
      System.out.println("Building tree of size " + t._size);
      t.print();
    }
    try {
      _root.release();
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }
  }

  public void printList()
  {
    System.out.println("List: ");
    printList(_root);
  }

  public void printList(GlobalObject root)
  {
    Tree tmp, t;
    GlobalObject gtmp, oldgtmp;
   
    if (root == null) return;
    t = (Tree) root.open("r");
    System.out.println("x = " + t._x + ", y = " + t._y);
    gtmp = t._next;
    try {
      root.release();
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }

    while(gtmp != root) {
      tmp = (Tree) gtmp.open("r");
      if (Aleph.verbosity(Constants.LOQUACIOUS))
	System.out.println("x = " + tmp._x + ", y = " + tmp._y);
      oldgtmp = gtmp;
      gtmp = tmp._next;
      try {
	oldgtmp.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
    }
  }
  
  // reverse orientation of list
  private static void reverseList(GlobalObject root)
  {
    GlobalObject gprev, gback, gnext, gtmp;
    Tree prev, back, next, tmp, t;
     
    if (root == null) return;
    t = (Tree) root.open("w");

    gprev = t._prev;
    prev = (Tree) gprev.open("w");
    prev._next = null;
    t._prev = null;
    try {
      gprev.release();
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }

    gback = root;
    gtmp = root;
    root = t._next;
    back = t;

    while(root != null) {
      t = (Tree) root.open("w");
      gnext = t._next;
      t._next = gback;
      back._prev = root;
      try {
	gback.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
      gback = root;
      back = t;
      root = gnext;
    }

    try {
      gback.release();
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }

    tmp = (Tree) gtmp.open("w");
    tmp._next = gprev;
    try {
      gtmp.release();
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }

    prev = (Tree) gprev.open("w");
    prev._prev = gtmp;
    try {
      gprev.release();
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }

  }

  private static double distance(Tree a, Tree b) {
    double ax,ay,bx,by;
    
    ax = a._x; ay = a._y;
    bx = b._x; by = b._y;

    return (Math.sqrt((ax-bx)*(ax-bx)+(ay-by)*(ay-by)));
  }
 
  // Use closest-point heuristic from Cormen Leiserson and Rivest
  private static GlobalObject conquer(GlobalObject gt) 
  {  
    Tree cycle, tmp, min, prev, next;
    GlobalObject gdonext;
    GlobalObject gcycle, gmin, gtmp, gnext, gprev;
    Tree t;
    double mindist,test;
    double mintonext, mintoprev, ttonext, ttoprev;
 
    if (gt == null) return null;
    gt = makeList(gt);
    
    // Create initial cycle 
    t = (Tree) gt.open("w");
    gcycle = gt;
    gt = t._next;
    t._next = gcycle;
    t._prev = gcycle;
    try {
      gcycle.release();
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }

    for (; gt != null; gt=gdonext) { // loop over remaining points 
      t = (Tree) gt.open("w");
      gdonext = t._next; // value won't be around later
      gmin = gcycle;
      cycle = (Tree) gcycle.open("r");
      mindist = distance(t, cycle);
      gtmp = cycle._next;
      try {
	gcycle.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }

      while (!gtmp.equals(gcycle)) {
	GlobalObject oldgtmp;
	tmp = (Tree) gtmp.open("r");
	test = distance(tmp, t);
	if (test < mindist) {
	  mindist = test;
	  gmin = gtmp;
        } /* if */
	oldgtmp = gtmp;
	gtmp = tmp._next;
	try {
	  oldgtmp.release();
	} catch (AlephException ale) {
	  Aleph.warning("Release failed: " + ale.getMessage());
	}
      } /* while gtmp... */

      min = (Tree) gmin.open("w");
      gnext = min._next;
      gprev = min._prev;
      /* if next is same as prev they are all min */
      if (!gprev.equals(gmin)) {
	prev = (Tree) gprev.open("w");
	if (!gnext.equals(gprev)) {
	  next = (Tree) gnext.open("w");
	} else {
	  next = prev;
	}
      } else {
	next = min;
	prev = min;
      }
      mintonext = distance(min, next);
      mintoprev = distance(min, prev);
      ttonext = distance(t, next);
      ttoprev = distance(t, prev);
      if ((ttoprev - mintoprev) < (ttonext - mintonext)) {
	/* insert between min and prev */
	prev._next = gt;
	t._next = gmin;
	t._prev = gprev;
	min._prev = gt;
      } else {
	next._prev = gt;
	t._next = gnext;
	min._next = gt;
	t._prev = gmin;
      }
      try {
	gmin.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
      if (!gprev.equals(gmin)) {
	try {
	  gprev.release();
	} catch (AlephException ale) {
	  Aleph.warning("Release failed: " + ale.getMessage());
	}
	if (!gprev.equals(gnext)) {
	  try {
	    gnext.release();
	  } catch (AlephException ale) {
	    Aleph.warning("Release failed: " + ale.getMessage());
	  }
	}
      }
      try {
	gt.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }

    } /* for t... */

    return gcycle;
  }
  
  // Merge two cycles as per Karp 
  private static GlobalObject merge(GlobalObject ga, GlobalObject gb, 
				    GlobalObject gt, int nproc)
  {  
    Tree min, next, prev, tmp;
    GlobalObject gmin, gnext, gprev, gtmp;
    Tree a, b, t;
    double mindist, test, mintonext, mintoprev, ttonext, ttoprev;
    Tree n1, p1, n2, p2;
    GlobalObject gn1, gp1, gn2, gp2;
    double tton1, ttop1, tton2, ttop2;
    double n1ton2, n1top2, p1ton2, p1top2;
    int choice;
    int i;
    
    /* Compute location for first cycle */
    gmin = ga;
    a = (Tree) ga.open("r");
    t = (Tree) gt.open("w");
    mindist = distance(t, a);
    gtmp = ga;
    ga = a._next;
    try {
      gtmp.release(); /* release ga from above */
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }

    for (; !ga.equals(gtmp); ) {
      GlobalObject oldga;
      a = (Tree) ga.open("r");
      test = distance(a, t);
      if (test < mindist) {
	mindist = test;
	gmin = ga;
      } /* if */
      oldga = ga;
      ga = a._next;
      try {
	oldga.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
    } /* for ga... */

    min = (Tree) gmin.open("w");
    gnext = min._next;
    gprev = min._prev;
    next = (Tree) gnext.open("w");
    prev = (Tree) gprev.open("w");
    mintonext = distance(min, next);
    mintoprev = distance(min, prev);
    ttonext = distance(t, next);
    ttoprev = distance(t, prev);
    if ((ttoprev - mintoprev) < (ttonext - mintonext)) {
      /* would insert between min and prev */
      gp1 = gprev;
      p1 = prev;
      gn1 = gmin;
      n1 = min;
      tton1 = mindist;
      ttop1 = ttoprev;
      try {
	gnext.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
    }
    else { /* would insert between min and next */
      gp1 = gmin;
      p1 = min;
      gn1 = gnext;
      n1 = next;
      ttop1 = mindist;
      tton1 = ttonext;
      try {
	gprev.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
    }
    
    /* At this point, I am holding gt,gn1,gp1 in write mode */
    /* Compute location for second cycle */
    gmin = gb;
    b = (Tree) gb.open("r");
    mindist = distance(t, b);
    gtmp = gb;
    gb = b._next;
    try {
      gtmp.release(); /* gb from above */
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }

    for (; !gb.equals(gtmp); ) {
      GlobalObject oldgb;
      
      b = (Tree) gb.open("r");
      test = distance(b, t);
      if (test < mindist) {
	mindist = test;
	gmin = gb;
      } /* if */
      oldgb = gb;
      gb = b._next;
      try {
	oldgb.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
    } /* for tmp... */

    min = (Tree) gmin.open("w");
    gnext = min._next;
    gprev = min._prev;
    next = (Tree) gnext.open("w");
    prev = (Tree) gprev.open("w");
    mintonext = distance(min, next);
    mintoprev = distance(min, prev);
    ttonext = distance(t, next);
    ttoprev = distance(t, prev);
    if ((ttoprev - mintoprev) < (ttonext - mintonext)) {
      /* would insert between min and prev */
      gp2 = gprev;
      p2 = prev;
      gn2 = gmin;
      n2 = min;
      tton2 = mindist;
      ttop2 = ttoprev;
      try {
	gnext.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
    }
    else { /* would insert between min and next */
      gp2 = gmin;
      p2 = min;
      gn2 = gnext;
      n2 = next;
      ttop2 = mindist;
      tton2 = ttonext;
      try {
	gprev.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
    }
    /* At this point, I am holding gt,gn1,gp1,gn2,gp2 in write mode */
    
    /* Now we have 4 choices to complete:
       1:t,p1 t,p2 n1,n2
       2:t,p1 t,n2 n1,p2
       3:t,n1 t,p2 p1,n2
       4:t,n1 t,n2 p1,p2 */
    n1ton2 = distance(n1, n2);
    n1top2 = distance(n1, p2);
    p1ton2 = distance(p1, n2);
    p1top2 = distance(p1, p2);
    
    mindist = ttop1+ttop2+n1ton2;
    choice = 1;
    
    test = ttop1+tton2+n1top2;
    if (test < mindist) {
      choice = 2;
      mindist = test;
    }
    
    test = tton1+ttop2+p1ton2;
    if (test < mindist) {
      choice = 3;
      mindist = test;
    }
    
    test = tton1+tton2+p1top2;
    if (test < mindist) choice = 4;

    switch (choice) {
    case 1:
      /* 1:p1,t t,p2 n2,n1 -- reverse 2!*/
      try {
	gn2.release();
	gp2.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
      reverseList(gn2);

      n2 = (Tree) gn2.open("w");
      p2 = (Tree) gp2.open("w");
      p1._next = gt;
      t._prev = gp1;
      t._next = gp2;
      p2._prev = gt;
      n2._next = gn1;
      n1._prev = gn2;
      break;

    case 2:
      /* 2:p1,t t,n2 p2,n1 -- OK*/
      p1._next = gt;
      t._prev = gp1;
      t._next = gn2;
      n2._prev = gt;
      p2._next = gn1;
      n1._prev = gp2;
      break;

    case 3:
      /* 3:p2,t t,n1 p1,n2 -- OK*/
      p2._next = gt;
      t._prev = gp2;
      t._next = gn1;
      n1._prev = gt;
      p1._next = gn2;
      n2._prev = gp1;
      break;

    case 4:
      /* 4:n1,t t,n2 p2,p1 -- reverse 1!*/
      try {
	gn1.release();
	gp1.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
      reverseList(gn1);
      
      n1 = (Tree) gn1.open("w");
      p1 = (Tree) gp1.open("w");
      n1._next = gt;
      t._prev = gn1;
      t._next = gn2;
      n2._prev = gt;
      p2._next = gp1;
      p1._prev = gp2;
      break;
    }

    try {
      gt.release();
      gn1.release();
      gp1.release();
      gn2.release();
      gp2.release();
    } catch (AlephException ale) {
      Aleph.warning("Release failed: " + ale.getMessage());
    }

    return gt;
  }
  
  // Compute TSP for the tree t -- use conquer for problems <= sz
  public void go() {
    go(_root, 150, PE.numPEs());
  }

  private static GlobalObject go(GlobalObject gt,int sz,int nproc) {
    GlobalObject gleftval;
    GlobalObject grightval;
    GlobalObject gtleft,gtright;
    Tree t;
    int nproc_2 = nproc/2;
    try {
      t = (Tree) gt.open("r");
      gtleft = t._left;
      gtright = t._right;
      try {
	gt.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
      if (t._size <= sz) return conquer(gt);
    
      Join jv = new Join();
      PE pid = gtleft.getHome();
      if (pid.equals(PE.thisPE())) {  // Local PE
	gleftval = go(gtleft, sz, nproc_2);
      } else {  // Remote PE
	gleftval = new GlobalObject(new Tree());
	GlobalObject gb = new GlobalObject(gleftval);
	Traveler traveler = new Traveler(gb, gtleft, sz, nproc_2);
	traveler.start(pid, jv);
      }
    
      grightval = go(gtright, sz, nproc_2);
      jv.waitFor();
      return merge(gleftval, grightval, gt, nproc);
    } catch (Exception e) {
      Aleph.panic(e);
      return null;		// not reached
    }
  }
  
  public static void main(String[] args) {
    int nodeNum = 15;
    int flag    = 0;
    try {
      if ( args.length > 1)
	flag = Integer.parseInt(args[1]);
      if ( args.length > 0)
	nodeNum = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      Aleph.warning("usage: TSP [#nodeNum] [#flag]");
      Aleph.exit(1);
    }

    TSP tsp = new TSP(nodeNum);
    if (flag>=1) tsp.printTree();
    
    long start = System.currentTimeMillis();
    tsp.go();
    long end = System.currentTimeMillis();
    if (flag>=1) tsp.printList();
    System.out.println("Elapsed time: " +
		       ((double) (end - start)) / 1000.0
		       + " seconds");
  }

  static class Tree implements Serializable{

    public int _size;
    public double _x;
    public double _y;
    public GlobalObject _left;
    public GlobalObject _right;
    public GlobalObject _next;
    public GlobalObject _prev;

    Tree() {
    }
     
    public void print() {
      print(0);
    }

    public void print(int level) {
      Tree t;
      
      for (int i=0; i<level; i++)
	System.out.print("    ");
	System.out.println("x = " + _x + ", y = " + _y);

      if (_left != null) {
	for (int i=0; i<level; i++)
	  System.out.print("    ");
	System.out.println("Left");
	t = (Tree) _left.open("r");
	t.print(level+1);
	try {
	  _left.release();
	} catch (AlephException ale) {
	  Aleph.warning("Release failed: " + ale.getMessage());
	}
      }
      if (_right != null) {
	for (int i=0; i<level; i++)
	  System.out.print("    ");
	System.out.println("Right");
	t = (Tree) _right.open("r");
	t.print(level+1);
	try {
	  _right.release();
	} catch (AlephException ale) {
	  Aleph.warning("Release failed: " + ale.getMessage());
	}
      }
    }

  }  // Tree
  
  static class Builder extends RemoteThread {
    GlobalObject root;
    int n, dir, peNo, numPE;
    double minX, maxX, minY, maxY;
    
    Builder(GlobalObject root, int n, int dir, int peNo, int numPE, double minX, 
	    double maxX, double minY, double maxY) {
      this.root = root;
      this.n = n;
      this.dir = dir;
      this.peNo = peNo;
      this.numPE = numPE;
      this.minX = minX;
      this.maxX = maxX;
      this.minY = minY;
      this.maxY = maxY;
    }

    public void run() {
      Tree t = (Tree) root.open("w");
      if (Aleph.verbosity(Constants.LOQUACIOUS))
	System.out.print("Left ");
      t._left = buildTree(n, dir, peNo, numPE, minX, maxX, minY, maxY);
      try {
      root.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
    }
  }  // Builder

  static class Traveler extends RemoteThread {
    GlobalObject gg, gt;
    int sz, nproc;
    
    Traveler(GlobalObject gg, GlobalObject gt, int sz, int nproc) {
      this.gg = gg;
      this.gt = gt;
      this.sz = sz;
      this.nproc = nproc;
    }

    public void run() {
      GlobalObject gleftval = (GlobalObject) gg.open("w");
      GlobalObject gtleft = (GlobalObject) gt.open("r");
      gleftval = go(gtleft, sz, nproc);
      try {
	gg.release();
	gt.release();
      } catch (AlephException ale) {
	Aleph.warning("Release failed: " + ale.getMessage());
      }
    }
  }  // Traveler

}
