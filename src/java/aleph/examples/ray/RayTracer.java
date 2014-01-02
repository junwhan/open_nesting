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

package aleph.examples.ray;
import aleph.Constants;
import aleph.examples.ray.data.*;
import aleph.examples.ray.object.*;
import aleph.examples.ray.light.*;
import aleph.examples.ray.output.*;
import java.awt.Image;
import java.awt.image.*;
import java.util.Iterator;
import aleph.*;

class Pixel implements java.io.Serializable {
  public int x,y;
  public Object ray;

  public Pixel(int pixel[], Vec3d theray)
  {
    x = pixel[0];
    y = pixel[1];
    ray = theray;
  }
  
}

public class RayTracer extends RemoteThread {
  Vec3d pos;
  GlobalObject globworld, pixlist;
  //GlobalObject globcam;
  
  public RayTracer(Vec3d origin, GlobalObject pixel, GlobalObject world)
  {
    pixlist = pixel;
    pos = origin;
    globworld = world;
    //globcam = camera;
  }

public static void main(String args[])
  {
    if (Aleph.verbosity(Constants.LOQUACIOUS))
      System.out.println("Making world");

    long before = System.currentTimeMillis();

    int width = 100;
    int height = 100;
    if (args.length==2) {
      width = Integer.parseInt(args[0]);
      height = Integer.parseInt(args[1]);
      if ((width*height)<=0) {
	width = 500;
	height = 500;
      }
    }
    World w = new World(true);
    PPMplotter ppm[] = { new PPMplotter("test.ppm") };
    Camera camera = new Camera(Vec3d.Z.negate(), Vec3d.Y,
			       new Vec3d(0,0,3), Math.PI/3.0, width, height, ppm);
    if(ppm[0].setSize(width, height) == false)
      Aleph.panic("File: test.ppm cannot be found.");

    if (Aleph.verbosity(Constants.LOQUACIOUS))
      System.out.println("Rendering");
    render(w, camera);

    long duration = System.currentTimeMillis() - before;
    System.out.println("elapsed time " +
		       duration / 1000 + "."+ duration % 1000 + " seconds" );
  }
  
  public static void render(World world, Camera camera)
  {
    Iterator e;
    int pixel[] = new int[2];
    int i,j;
    int nrays = camera.numRays();
    int PEs = PE.numPEs();
    Pixel pix[][] = new Pixel[PEs][];
    GlobalObject plist[] = new GlobalObject[PEs];
    int extra = nrays % PEs;
    int size = nrays / PEs;

    for (j=0;j<PEs;j++) {
      if (j<extra) {
	plist[j] = new GlobalObject(new Pixel[size+1]);
      } else {
	Pixel [] dummy = new Pixel[size];
	plist[j] = new GlobalObject(dummy);
      }
      pix[j] = (Pixel[])plist[j].open("w");
    }
    if (Aleph.verbosity(Constants.LOQUACIOUS))
      System.out.println("Created "+PEs+" arrays of "+size+
			 " elements, and an extra on the first "+extra);
    Vec3d pos = camera.getPos();
    Vec3d theray;
    i = 0;
    while (camera.hasMoreElements()) {
      e = PE.allPEs();
      for (j=0;camera.hasMoreElements()&&(j<PEs);j++) {
	theray = camera.nextRay(pixel);
	pix[j][i] = new Pixel(pixel, theray);
      }
      i++;
    }
    e = PE.allPEs();
    GlobalObject globworld = new GlobalObject(world);
    RemoteThread ray[] = new RemoteThread[PEs];
    Join join = new Join();
    for (j=0;j<PEs;j++) {
      try {
	plist[j].release();
      } catch (AlephException ex) {
	System.out.println("Could not release local GlobalObject?!? -- "+ex);
	return;
      }
      ray[j] = new RayTracer(pos, plist[j], globworld);
      ray[j].start((PE)e.next(), join);
    }
    if (Aleph.verbosity(Constants.LOQUACIOUS))
      System.out.println("Threads started: waiting");
    join.waitFor();

    if (Aleph.verbosity(Constants.LOQUACIOUS))
      System.out.println("Writing file");
    try {
      for (j=0;j<PEs;j++) {
	Pixel p[] = (Pixel[])plist[j].open("r");
	for (i=0;i<p.length;i++)
	  camera.writePixel(p[i].x, p[i].y, (Color)p[i].ray);
	plist[j].release();
      }
    } catch (ClassCastException cce) {
      System.out.println("Color wasn't set or GlobalObject was unexpected type! -- "+ 
			 cce);
      return;
    } catch (AlephException ae) {
      System.out.println("Couldn't open pixel list! -- "+ae);
      return;
    }
    camera.endPlot();
  }
  
  public void run()
  {
    try {
      World world = (World)globworld.open("r");
      Pixel pix[] = (Pixel[])pixlist.open("w");
      for (int i=0;i<pix.length;i++) {
	pix[i].ray = world.traceRay(0, pos, (Vec3d)pix[i].ray);
	Thread.yield();
      }
      pixlist.release();
      globworld.release();
    } catch (Exception e) {
      Aleph.panic(e);
    }
    if (Aleph.verbosity(Constants.LOQUACIOUS))
      System.out.println("Finished pixel on PE "+PE.thisPE());
  }
  
  /*
    public void trace(World world, Pixel p) throws AlephException
    {
    Color c = world.traceRay(0, pos, p.ray);
    Camera camera = (Camera)globcam.open("w");
    camera.writePixel(p.x, p.y, c);
    globcam.release();
    }
    */

}

