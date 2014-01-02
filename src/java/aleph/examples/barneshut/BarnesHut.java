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

package aleph.examples.barneshut;
import aleph.Aleph;
import aleph.Constants;
import aleph.GlobalObject;
import aleph.RemoteFunction;
import aleph.RemoteThread;
import aleph.AlephException;
import aleph.Join;
import aleph.PE;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.*;



/**
 * implementg barnus_huts_nbody algorithm in Aleph.
 *
 *@author An Yan
 *@date   Dec 97
 **/

public class BarnesHut implements Serializable {

  /**********************************************************************
   * some constant settings
   **/
  static final boolean CONSOLE_OUTPUT = false; // show result in console 
                                               //in addition to outfile.
  static final short nDim = 3 ;             // space dimensions
  static final int   nSub = 1 << nDim ;         // subcells per cell
  static final int   iMax = 1 << (8 * 4 - 2) ;      //  highest bit
  static final int   maxCells = 8*1024 ; 

  static String inFile = "examples/barneshut/infile";
  static String outFile = "examples/barneshut/outfile";
   
  /// class Node: general node, may be extended to Body node or cell node
  static class Node implements Serializable{
    double  mass;
    double[]  pos;

    public Node() {
      pos = new double[nDim];
    }
  } ;
   
  
  /// class Body node
  static class Body extends Node {
    double[]  vel;

    public Body(){
      vel = new double[nDim];
    }
  } 
   
  /// class Cell node
  static class Cell extends Node {
    int  type; // size of this field assumes at least 3 dimensions
               // (so, oct-tree, i.e., 8 children, i.e., 8 bits)
    GlobalObject[] next;
  
    public Cell(){
      next = new GlobalObject[nSub];
    }
  
    ///check if child i is a cell (not a body) node
    boolean isCell( int i ) {
      return  (( type >> i ) & 1) != 0 ;
    };
  
    ///mark child i as a cell node
    void markCell( int i ) {
      type = (type | (1 << i));
    };
    
  } // end of Cell

  /**
   * this class stores data distribution information.
   **/
  static class Data implements Serializable{
    int pe;          // on which pe the GlobalObjects are stored
    int ncells;      // number of "local" cells
    int nbody ;      // number of "local" bodies 
    int bodies_there;  

    Data( int pe ) {
      this.pe = pe;
    }
  }
  
  static GlobalObject[] cellTab; // references to global cell nodes
  static GlobalObject[] bodyTab; // references to global body nodes

  static double rsize;        // space size
  static double[] rmin = new double[nDim];

  /** ================================================================
   * barnus_huts_data.nbody main
   **/
  public static void main (String[] args) {
    int c, i;
    GlobalObject root;
    double dt;
    int pe;
    Join join = new Join();
  
    Data data[] = new Data[ PE.numPEs() ]; // store data distribution info

    /* default values for parameters */
    int n = 16;
    int steps  = 4;
    double dtime  = 0.0125;
    int genout = 0;

    try{
      if(args.length > 0)
        inFile = args[0];
      if(args.length > 1)
        outFile = args[1];
      if(args.length > 2)
        n = Integer.parseInt( args[2] );
      if(args.length > 3)
        steps = Integer.parseInt( args[3] );
      if(args.length > 4)
        genout = Integer.parseInt( args[4] );
    }catch( NumberFormatException e ) {
      System.err.println(
        "Usage: BarnesHut [infile] [outfile] [#nodeNum=16] [#stepsToGo=4] [#outputFlag=1]");
      Aleph.exit();
    }

    if (Aleph.verbosity(Constants.LOQUACIOUS))
      System.out.println("init data ...");

    long t0 = System.currentTimeMillis();

    InitDataThread forkInitData = new InitDataThread( n ); 
    for (Iterator e = PE.allPEs(); e.hasNext(); ) 
      forkInitData.start( (PE) e.next() , join );
    join.waitFor();
    // get returned value
    while(join.hasNext()) {
      try{
        Data d = (Data)join.next();
        data[ d.pe ] = d;
      } catch (Exception e) {
	Aleph.panic(e);
      }
    }
  
    long t1 = System.currentTimeMillis();
    dt = 0.5 * dtime;
    while (steps>0) {
      if (Aleph.verbosity(Constants.LOQUACIOUS)) {
	System.out.println("-----------Steps to go - " + 
			   steps + " ----------------");
	System.out.println("bookkeeping ...");
      }

      for ( pe = 0; pe < PE.numPEs(); pe++ ) {
        BookKeepingThread forkBookKeeping = new BookKeepingThread( data[pe] );
        forkBookKeeping.start( PE.getPE(pe), join );
      }  
      join.waitFor();
      // get returned value
      while(join.hasNext()) {
        try{
          Data d = (Data)join.next();
          data[ d.pe ] = d;
        } catch (Exception e) {
	  Aleph.panic(e);
        }
      }
  
      if (Aleph.verbosity(Constants.LOQUACIOUS))
	System.out.println("maketree ...");
      root = newCell(null,0,data[ PE.thisPE().getIndex() ]);
      for ( pe = 0; pe < PE.numPEs(); pe++ ) {
        MakeTreeThread forkMakeTree = new MakeTreeThread( root, data[pe] );
        forkMakeTree.start( PE.getPE(pe), join );
      }
      join.waitFor();
      // get returned value
      while(join.hasNext()) {
        try{
          Data d = (Data)join.next();
          data[ d.pe ] = d;
        } catch (Exception e) {
	  Aleph.panic(e);
        }
      }

      if (Aleph.verbosity(Constants.LOQUACIOUS))
	System.out.println("compute cofm ...");
      for ( pe = 0; pe < PE.numPEs(); pe++ ) {
        HackCofmThread forkHackCofm = new HackCofmThread( data[pe] );
        forkHackCofm.start( PE.getPE(pe), join );
      }
      join.waitFor();
      // get returned value
      while(join.hasNext()) {
        try{
          Data d = (Data)join.next();
          data[ d.pe ] = d;
        } catch (Exception e) {
	  Aleph.panic(e);
        }
      }
  
      if (Aleph.verbosity(Constants.LOQUACIOUS))
	System.out.println("stepvel ...");

      for ( pe = 0; pe < PE.numPEs(); pe++ ) {
        StepVelThread forkStepVel = new StepVelThread( dt, root, data[pe] );
        forkStepVel.start( PE.getPE(pe), join );
      }
      join.waitFor();
      // get returned value
      while(join.hasNext()) {
        try{
          Data d = (Data)join.next();
          data[ d.pe ] = d;
        } catch (Exception e) {
	  Aleph.panic(e);
        }
      }
  
      if (Aleph.verbosity(Constants.LOQUACIOUS))
	System.out.println("steppos ...");

      for ( pe = 0; pe < PE.numPEs(); pe++ ) {
        StepPosThread forkStepPos = new StepPosThread( dtime, data[pe] );
        forkStepPos.start( PE.getPE(pe), join );
      }
      join.waitFor();
      // get returned value
      while(join.hasNext()) {
        try{
          Data d = (Data)join.next();
          data[ d.pe ] = d;
        } catch (Exception e) {
	  Aleph.panic(e);
        }
      }
  
      --steps;
    }
    long t2 = System.currentTimeMillis();
    if (Aleph.verbosity(Constants.LOQUACIOUS))
      System.out.println( "Done." );
  
    if (genout != 0 ) {
      System.out.println("generate output ...");
      PrintOut( data );
    }
    long t3 = System.currentTimeMillis();
  
    if (Aleph.verbosity(Constants.LOQUACIOUS)) {
      System.out.println("Initialization time = " + (t1-t0) + " msecs");
      System.out.println("Computation time  = " + (t2-t1) + " msecs");
      System.out.println("Output time     = " + (t3-t2) + " msecs");
    }
    System.out.println("Elapsed time: " +
		       ((double) (t3 - t0)) / 1000.0
		       + " seconds");

  } // end of main()

  
  /** ================================================================
   * class BookKeepingThread
   * Things to do before each iteration, including clearing
   * the cache
   */
  static class BookKeepingThread extends RemoteFunction{  
  
    Data data ;

    BookKeepingThread( Data data ){
      this.data = data;
    }

    public Object run ()
    {
      data.bodies_there = 0;
      data.ncells = 0;
    
      rsize = 4.0;
      SETVS(rmin, -2.0);

      return (Object) data;
    }
  }
  
  /** ================================================================
  * class InitDataThread
  * Read in all the bodies and their data (mass, pos, vel)
  **/
  
  static class InitDataThread extends RemoteFunction {
  
    private int totalBodyNum;
    Data data ;
  
    InitDataThread ( int n ){
      totalBodyNum = n;
    }
  
    public Object run ( )
    {
      int i;
      data = new Data( PE.thisPE().getIndex() );
    
      i = 0;
      data.nbody = totalBodyNum / PE.numPEs();
      if (PE.thisPE().getIndex() < (totalBodyNum % PE.numPEs()))
        ++data.nbody;
      else
        i = totalBodyNum % PE.numPEs();
      i += data.nbody * PE.thisPE().getIndex() ;
      bodyTab  =  new GlobalObject[ data.nbody ];
    
      // open file and read
      File src = new File( inFile );
      DataInputStream srcs = null;
      try{
	if (!src.exists()) {
          System.err.println( "input file " + src + " does not exist ");
	  Aleph.exit();
	}
        if(!src.isFile() ){
          System.err.println( "Iinput file " + inFile + " is not a file ");
          Aleph.exit();
        };
        srcs = new DataInputStream( new FileInputStream(src) );
        srcs.skipBytes( i * 8 *(1 + 2*nDim) );  
  
        // read into bodyTab
        Body body; 
        for(i = 0 ; i < data.nbody ; i++ ){
          int j;
          body = new Body() ;
          body.mass = srcs.readDouble(); 
          for( j = 0; j < nDim; j++ )
            body.pos[j] = srcs.readDouble(); 
          for( j = 0; j < nDim; j++ )
            body.vel[j] = srcs.readDouble(); 
          bodyTab[i] = new GlobalObject ( body );
  
        }
      } 
      catch( IOException e ){ }
      finally{
        if ( srcs != null )
          try{
            srcs.close(); // close file
          } catch(IOException e) { };
      };
    
      /* Create cells and cell table and initialize */
      cellTab = new GlobalObject[ maxCells ]; 
      for (i=0; i < maxCells; i++){
        cellTab[i] = null;
      }
  
      return (Object) data;

    }
  } // end class InitDataThread

  /** ================================================================
   * MAKETREE
   * Creates the oct-tree of bodies
   */
  
  static class MakeTreeThread extends RemoteFunction {
    
    GlobalObject root;
    Data data ;
  
    MakeTreeThread ( GlobalObject root, Data data ) {
      this.root = root;
      this.data = data;
    }
  
    public Object run ( )
    {
      boolean in_write_mode;
      int level,index;
      int[] xp = new int[nDim];
      int[] xr = new int[nDim];
      GlobalObject gBody;
      GlobalObject gCell;
      Cell cell;
      Body body;
  
      while ( data.bodies_there < data.nbody) {

        gBody = bodyTab[data.bodies_there++];
        body = (Body) gBody.open( "r" );
        intcoord(xp, body.pos);
        try{
          gBody.release();
        }catch (AlephException e) {}
  
        gCell = root;
        level = iMax >> 1;
        while (true) {
          cell = (Cell) gCell.open( "r" );
          in_write_mode = false;
          index = subindex(xp, level);
          level >>= 1;
  
          if (cell.next[index] == null) {
            try{
              gCell.release();
            }catch (AlephException e) {}
            cell = (Cell) gCell.open( "w" );
            in_write_mode = true;
            if (cell.next[index] == null) {
              cell.next[index] = gBody;
              try{
                gCell.release();
              }catch (AlephException e) {}
              break;
            }
          }
          if (!cell.isCell(index)) {
            if (!in_write_mode) {
              try{
                gCell.release();
              }catch (AlephException e) {}
              cell = (Cell) gCell.open( "w" );
            }
            if (!cell.isCell(index)) {
              cell.next[index] = newCell(cell.next[index], level, data);
              cell.markCell(index);
            }
          }
          GlobalObject g = cell.next[index];
          try{
               gCell.release();
          }catch (AlephException e) {}
          gCell = g;
        }
      }

      return (Object) data;

    } /* run() */
    
  } // end class MakeTreeThread


  /* ================================================================
   * intcoord() -- funtion called by newCell and MakeTree
   * Converts floating point coords RP into integer-scale coords XP
   */
  static void intcoord(int[] xp, double[] rp)
  {
    int k;
    double xsc;
  
    for (k = 0; k < nDim; k++) {
      xsc = (rp[k] - rmin[k]) / rsize;
      if (0.0 <= xsc && xsc < 1.0)
        xp[k] = (int) Math.round(iMax * xsc);
      else {
        System.err.println( "Particle pos. out of range along "+ k +" dim.");
        System.err.flush();
        Aleph.exit(0);
      }
    }
  } /* intcoord() */
  
  /** ================================================================
   * SUBINDEX -- funtion of MakeTreeThread class
   * Given integer-range coords x, determines oct-tree cell index
   */
  static int subindex(int[] x, int l)
  {
    int i, k;
    boolean yes;
  
    i   = 0;
    yes = false;
    for (k = 0; k < nDim; k++) {

      if (((x[k] & l) != 0 && !yes) || ((x[k] & l)==0 && yes)) {
        i += nSub >> (k + 1);
        yes = true;
      }
      else yes = false;
    }
    return i;
  }

  /* ================================================================
   * NEWCELL -- -- called by both main() and MakeTreeThread.run() 
   * Allocates and initializes a new cell, which child p at index l
   */
  static GlobalObject newCell( GlobalObject child, int l, Data data)
  {
    Cell cell;
    Body body;
    int[] xp = new int[nDim];
    int n, i;
  
    n = data.ncells++;
    if (data.ncells > maxCells) {
      System.err.println("makecell: MAXCELLS limit reached");
      System.err.flush();
      Aleph.exit(1);
    }
    if (cellTab[n] == null)
      cellTab[n] = new GlobalObject( new Cell() ); 
    cell = (Cell) cellTab[n].open( "w" );
    for (i = 0; i < nSub; ++i)
      cell.next[i] = null;
    cell.type = 0;
    cell.mass = -1.0;
    if (child != null) {
      body = (Body) child.open( "r" ); 
      intcoord(xp, body.pos);
      cell.next[subindex(xp,l)] = child;
      try{ 
        child.release(); 
      }catch (AlephException e) {}
    }
    try{
      cellTab[n].release();
    }catch (AlephException e) {}

    return (cellTab[n]);

  } /* newCell() */
  
  /** ================================================================
   *  class HackCofmThread
   * Compute center-of-mass of each sub-tree
   */
  static class HackCofmThread extends RemoteFunction {
    
    Data data ;

    HackCofmThread( Data data ){
      this.data = data;
    }

    public Object  run ()
    {
      Cell cell;
      Node node;
      double[] tmp = new double[nDim];
      int n, i;
    
      while (data.ncells > 0) {
        n = --data.ncells;
        cell = (Cell) cellTab[n].open( "w" );
        cell.mass = 0.0;
        SETVS(cell.pos, 0.0);
        for (i = 0; i < nSub; ++i) {
          if (cell.next[i] != null) {
            if ( cell.isCell(i)) {
              while (true) {
                node = (Node) cell.next[i].open( "r" );
                if (node.mass < 0.0) {
                  try{ 
                    cell.next[i].release();
                  } catch (AlephException e) {}
                }
                else
                  break;
              }
              cell.mass += node.mass;
              MULVS(tmp, node.pos, node.mass);
              ADDV(cell.pos, cell.pos, tmp);
              try{ 
                cell.next[i].release();
              } catch (AlephException e) {}
            }
            else {
              node = (Node) cell.next[i].open( "r" );
              cell.mass += node.mass;
              MULVS(tmp, node.pos, node.mass);
              ADDV(cell.pos, cell.pos, tmp);
              try{ 
                cell.next[i].release(); 
              }catch (AlephException e) {}
            }
          }
        } // end for()
        DIVVS(cell.pos, cell.pos, cell.mass);
        try{
          cellTab[n].release();
        }catch (AlephException e) {}
  
      }
      return (Object) data;

    }  /* run() */
  
  } // end class HackCofmThread

  /** ================================================================
  * class StepVelThread
  * For each body,
  *   compute acceleration (hackgrav);
  *   compute new velocity using acceleration, old velocity, and time step
  **/
  
  static class StepVelThread extends RemoteFunction {
    final static double EpsSq = 0.0025; 
    final static double TolSq = 0.25;  
    double dtime;
    GlobalObject gRoot;
    Data data ;
    
    StepVelThread(double dtime, GlobalObject root, Data data){
      this.dtime = dtime;
      gRoot = root;
      this.data = data;
    }
  
    public Object run()
    {
      GlobalObject gBody;
      Body body;
      double[] dvel = new double[nDim];
      double[] pos0 = new double[nDim];
      double[] acc  = new double[nDim];
    
      while (data.bodies_there > 0) {
        gBody = bodyTab[--data.bodies_there];
        body = (Body) gBody.open( "r" ); 
        SETVS(acc, 0.0);
        SETV(pos0, body.pos);
  
        try{ // release as soon as possible
          gBody.release();
        } catch (AlephException e) { }
  
        hackgrav(gBody, gRoot, pos0, acc, rsize*rsize, true);
  
        // open for write
        body = (Body) gBody.open( "w" );
  
        MULVS(dvel, acc, dtime);
        ADDV(body.vel, body.vel, dvel);
        try{
          gBody.release();
        } catch (AlephException e) { }
      }
  
      return (Object) data;

    }
    
    /** ================================================================
      * HACKGRAV()
      * Compute gravitational force
      */
    static void hackgrav(GlobalObject  gBody,
            GlobalObject  gNode,
            double[]   pos0,
            double[]   acc,
            double  dsq,
            boolean     is_cell)
    {
      double dr2;
      double phii,mor3;
      double[] dr = new double[nDim];
      double[] ai = new double[nDim];
      Node node;
      Cell cell;
      int i;
  
      if ( gBody.equals(gNode) ) return;
    
      node = (Node) gNode.open( "r" ); 
      if (is_cell) {
        cell = (Cell) node;
        SUBV(dr, cell.pos, pos0);
        dr2 = dot(dr, dr);
        if (TolSq*dr2 < dsq) {
          for (i = 0; i < nSub; ++i){
            if (cell.next[i] != null) {
              try{ //  Is release required for COPY mode?
                gNode.release();
              } catch (AlephException e) { }
  
              hackgrav(gBody, cell.next[i], pos0, acc, dsq/4.0, cell.isCell(i));
              node = (Node) gNode.open( "r" );   // COPY mode
  
            }
          }
        }
        else {
          dr2 += EpsSq;
          phii = cell.mass/Math.sqrt(dr2);
          mor3 = phii/dr2;
          MULVS(ai, dr, mor3);
          ADDV(acc, acc, ai);
        }
      }
      else {
        SUBV(dr, node.pos, pos0);
        dr2  = dot(dr, dr);
        dr2 += EpsSq;
        phii = node.mass/Math.sqrt(dr2);
        mor3 = phii/dr2;
        MULVS(ai, dr, mor3);
        ADDV(acc, acc, ai);
      }
  
      try{ 
        gNode.release();
      } catch (AlephException e) { }
  
      long t2 = System.currentTimeMillis();
    }
  
    
    /* ================================================================
     * DOT() -- function of class HackGravThread
     * dot-product of two vectors
     */
    static private double dot (double[] xp, double[] yp)
    {
      int k;
      double sum;
    
      sum = 0.0;
      for (k = 0; k < nDim; k++)
        sum += (xp[k]) * (yp[k]);
      return  sum;
    }
  
  } // end of class StepVelThread

  /** ================================================================
   * class StepPosThread
   * Compute new position, based on velocity, old position and timestep
   * This is a purely local operation
   */
  static class StepPosThread extends RemoteFunction{  
    private double dtime;
    Data data ;

    StepPosThread(double dtime, Data data){
      this.dtime = dtime;
      this.data = data;
    }

    public Object run ()
    {
      GlobalObject gBody;
      Body body;
      double[] dpos = new double[nDim];
    
      while (data.bodies_there < data.nbody) {
        gBody = bodyTab[data.bodies_there++];
        body = (Body) gBody.open( "w" );
        MULVS(dpos, body.vel, dtime);
        ADDV(body.pos, body.pos, dpos);
        try{
          gBody.release();
        } catch( AlephException e ){}
      }

      return (Object) data;

    }
  } // end of class StepPos

  /** ================================================================
   * class OutputThread
   * For each body,
   *  print position and velocity
   */
  static class OutputThread extends RemoteThread {
    Data data;

    OutputThread( Data data ) {
      this.data = data;
    }

    public void run()
    {
      Body body;
      int i,j;
      RandomAccessFile out ;
    
      try{ 
        out = new RandomAccessFile( outFile , "rw" );
        out.seek( out.length() );
        for (i = 0; i < data.nbody; ++i) {
          body = (Body) bodyTab[i].open( "r" );
          out.writeDouble( body.mass );
          for (j = 0; j < nDim; ++j)
            out.writeDouble( body.pos[j] );
          for (j = 0; j < nDim; ++j)
            out.writeDouble( body.vel[j] );
          out.writeChar( '\n' );
          if(CONSOLE_OUTPUT){
            System.out.println( bodyTab[i] + " " + body.mass + " " + 
              body.pos[0] + " " +  body.pos[1] + " " + body.pos[2] + " " +
              body.vel[0] + " " + body.vel[1] + " " + body.vel[2]);
            System.out.flush();
          }
          try{
            bodyTab[i].release();
          } catch (AlephException e) {}
        }
        out.close();
      }catch( IOException e ){ }
  
    } /* gen_output() */
  
  } // end of class OutputThread.


  /* ================================================================
   * PRINTOUT
   * Produce output for all bodies
   */
  
  static private void PrintOut( Data data[] )
  {
    int i;
    Join join = new Join();
  
    // create an empty outfile
    try{ 
      FileOutputStream out = new FileOutputStream( new File( outFile ) );
      out.close();
    } catch( IOException e ){ }

    for ( int pe = 0; pe < PE.numPEs(); pe++ ) {
      OutputThread forkOutput = new OutputThread( data[pe] );
      forkOutput.start( PE.getPE(pe), join );
    }
    join.waitFor();
  }

  /** =========================================
   * math functions
   */
  static void SETVS(double[] v,double s) 
  { 
    for (int k = 0; k < nDim; k++) 
      v[k] = s; 
  }
  
  static void SETV( double[] v, double[] u) 
  { 
    for (int k = 0; k < nDim; k++) 
      v[k] = u[k]; 
  }
  
  static void DIVVS( double[] v, double[] u, double s) 
  { 
    for ( int k = 0; k < nDim; k++) 
      v[k] = u[k] / s; 
  }
  
  static void ADDV( double[] v, double[] u, double[] w) 
  { 
    for (int k = 0; k < nDim; k++) 
      v[k] = u[k] + w[k]; 
  }
  
  static void SUBV( double[] v, double[] u, double[] w) 
  { 
    for (int k = 0; k < nDim; k++) 
      v[k] = u[k] - w[k]; 
  }
  
  static void MULVS( double[] v, double[] u, double s) 
  { 
    for ( int k = 0; k < nDim; k++) 
      v[k] = u[k] * s; 
  }
} // end of class BH
