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
package aleph.examples.ray.object;
import aleph.examples.ray.data.*;
import aleph.examples.ray.texture.Texture;

public class Sphere extends Obj implements java.io.Serializable {
/*
protected Color ambient;
protected Color diffuse;
protected Color specular;
protected Color reflect;
protected Color transparent;
protected Texture texture;
protected double shine;
protected Vec3d center;
protected Vec3d pole;
*/
private double radsq;
private double radius;

public Sphere(Color amb, Color diff, Color spec, Color refl,
		Color trans, Color bl, Texture tex, double sh,
		Vec3d cen, Vec3d pol, double rad)
{
radius = rad;
//System.out.println("Radius = "+rad);
radsq = rad * rad;
setProps(amb, diff, spec, refl, trans, bl, tex, sh, cen, pol);
}

public Vec3d normalAt(Vec3d point)
{ return point.sub(center).normalize(); }

public UV uvAt(Vec3d point)
{ return new UV(0,0); }

public double intersect(Vec3d origin, Vec3d ray)
{
//System.out.println("Called intersect on Sphere! "+ray+" "+origin);
Vec3d P = origin.sub(center);
double tval;
double a = ray.dot(ray),
	b = 2 * (P.dot(ray)),
	c = P.dot(P) - radsq,
	discriminant = b * b - 4 * a * c;
if (discriminant >= 0) {
	if (a == 0) {
		if (b == 0)
			tval = INFINITY;
		else
			tval = -(c/b);
	} else if (discriminant > 0) {
		double t1, t2;

		discriminant = Math.sqrt(discriminant);
		a = .5/a;
		t1 = (discriminant - b) * a;
		t2 = - ((discriminant + b) * a);
		if (t1<MINTVAL)
			tval = t2;
		else if (t2<MINTVAL)
			tval = t1;
		else
			tval = (t1<t2) ? t1 : t2;
	} else {
		tval = -(b*(.5/a));
		}
	if (tval < MINTVAL)
		tval = INFINITY;
} else
	tval = INFINITY;
//if (tval!=INFINITY) System.out.println("Hit sphere!");
return tval;
}


}
