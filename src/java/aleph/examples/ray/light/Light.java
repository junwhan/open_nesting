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
package aleph.examples.ray.light;
import aleph.examples.ray.data.*;

public abstract class Light implements java.io.Serializable {
public static final double INFINITY = Double.POSITIVE_INFINITY;
protected Color color;
protected double fallconst[] = {0,0,1};

protected final void setProps(Color c, double k1, double k2, double k3)
{
color = c;
fallconst[0] = k1;
fallconst[1] = k2;
fallconst[2] = k3;
}

public final Color getColor()
{ return color; }

public abstract double toLight(Vec3d point, double[] direction);

public final double falloff(double dist)
{
if (Double.isInfinite(dist))
	return 1.0;
else if (dist<=1)
	return (fallconst[0]+fallconst[1]+fallconst[2]);
else {
	double d=1/dist;
	return (fallconst[0]*d*d + fallconst[1]*d + fallconst[2]);
	}
}

public final Color calcDiffuse(double fall, Vec3d tolight, Vec3d normal)
{
double nl = normal.dot(tolight);
if (nl<=0)
	return Color.black;
else
	return color.mult(fall*nl);
}

public final Color calcSpecular(double fall, Vec3d incident,
			Vec3d tolight, Vec3d normal, double shine)
{
Vec3d V = incident.negate();
Vec3d R = tolight.negate().reflect(normal);
double vr = V.dot(R);
if (vr<=0)
	return Color.black;
else
	return color.mult(fall*Math.pow(vr, shine));
}

}
