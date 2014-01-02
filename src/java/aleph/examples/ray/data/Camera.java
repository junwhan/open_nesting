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
package aleph.examples.ray.data;
import aleph.examples.ray.output.Plotter;

public class Camera implements java.io.Serializable {

  protected int w,h,x,y;
  protected Vec3d pos, upleft, right, down;
  protected double invw, invh;
  protected Plotter plotters[];
  
  public Camera(Vec3d look, Vec3d up, Vec3d position,
		double fov, int xpix, int ypix, Plotter p[])
  {
    w = xpix;
    h = ypix;
    pos = position;
    double two = 2.0;
    double width = Math.tan(fov*.5);
    double height = (width*h)/w;
    
    right = look.crossnorm(up).mult(width);
    down = look.crossnorm(right).mult(height);
    upleft = look.normalize().sub(down.add(right));
    invw = 2.0/w;
    invh = 2.0/h;
    plotters = p;
  }
  
  public int numRays() { return w*h; }
  public synchronized boolean hasMoreElements() { return ((y<h)&&(x<w)); }
  
  public synchronized Vec3d nextRay(int pixel[])
  {
    Vec3d c = upleft.addnorm(right.mult(x*invw).add(down.mult(y*invh)));
    pixel[0]=x;
    pixel[1]=y;
    if (++x>=w) {
      x = 0;
      y++;
//System.out.println("Firing ray: "+c);
    }
    return c;
  }
  
  public void writePixel(int px, int py, Color c)
  {
    int i;
    for (i=0;i<plotters.length;i++)
	plotters[i].writePixel(px, py, c);
  }
  
  public void endPlot()
  {
    int i;
    for (i=0;i<plotters.length;i++)
      plotters[i].endPlot();
  }
  
  public Vec3d getPos()
  { return pos; }
  
}
