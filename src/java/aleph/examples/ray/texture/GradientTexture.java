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
package aleph.examples.ray.texture;
import aleph.examples.ray.data.*;
import aleph.examples.ray.object.Obj;

public class GradientTexture implements java.io.Serializable {
private Vec3d dir;
private double invDmagsq;
private Color c[] = new Color[2];

public GradientTexture(Vec3d direction, Color color1, Color color2)
{
dir = direction;
invDmagsq = 1.0/dir.lengthSquared();
c[0] = color1;
c[1] = color2;
}

public Color colorAt(Vec3d point, Obj obj)
{
double dist[] = new double[2];
int flip = 0;

dist[0] = Math.abs(dir.dot(point)) * invDmagsq;
if (dist[0] > 1.0) {
	flip = (int)Math.floor(dist[0]);
	dist[0] -= flip;
	}
if ((flip%2)==1) {
	dist[0] = .5 + .5 * Math.cos(Math.PI * dist[0]);
	dist[1] = 1.0 - dist[0];
} else {
	dist[1] = .5 + .5 * Math.cos(Math.PI * dist[0]);
	dist[0] = 1.0 - dist[1];
	}
return Color.blend(dist, c);
}

}
