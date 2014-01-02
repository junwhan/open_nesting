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
package aleph.examples.ray.output;
import aleph.examples.ray.data.Color;
import java.io.*;
import aleph.*;

public class PPMplotter implements Plotter {
  String filename;
  int width, height;
  double pixel[][][] = null;
  double maxr=1, maxg=1, maxb=1;

  public PPMplotter(String fname)
  { 
    filename = fname; 
  }

public boolean setSize(int w, int h)
{
  if (pixel!=null) {
    return false;
  }
  width = w;
  height = h;
  long before = System.currentTimeMillis();
  pixel = new double[width][height][3];
  long duration = System.currentTimeMillis() - before;
  if (Aleph.verbosity(Constants.LOQUACIOUS))
    System.out.println("Allocation took " +
		       duration / 1000 + "."+ duration % 1000 + " seconds" );
  return true;
}

public synchronized void writePixel(int x, int y, Color c)
{
c.get(pixel[x][y]);
if (pixel[x][y][0]>maxr) maxr = pixel[x][y][0];
if (pixel[x][y][1]>maxg) maxg = pixel[x][y][1];
if (pixel[x][y][2]>maxb) maxb = pixel[x][y][2];
/*
System.out.println("Pixel["+x+"]["+y+"] = ("+
	pixel[x][y][0]+", "+pixel[x][y][1]+", "+pixel[x][y][2]+")");
*/
}

public void endPlot()
{
try {
	FileOutputStream f = new FileOutputStream(filename);
	f.write(img2ppmraw());
	f.close();
} catch (IOException e) {
	System.err.println("Couldn't write file!");
	}
}

private byte[] img2ppmraw()
{
byte tmpbuf[] = ("P6\n" + width + ' ' + height + "\n255\n").getBytes();
byte buf[] = new byte[tmpbuf.length + width * height * 3];
int x,y;

try {
	System.arraycopy(tmpbuf, 0, buf, 0, tmpbuf.length);
} catch (Exception e) {}
int i=tmpbuf.length;
for (y=0;y<height;y++)
	for (x=0;x<width;x++) {
		buf[i] = (byte)Math.floor(255*pixel[x][y][0]/maxr);
		buf[i+1] = (byte)Math.floor(255*pixel[x][y][1]/maxg);
		buf[i+2] = (byte)Math.floor(255*pixel[x][y][2]/maxb);
		i+=3;
		}
return buf;
}

}
