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
import aleph.examples.ray.World;
import aleph.examples.ray.texture.Texture;

public abstract class Obj implements java.io.Serializable {
public static final double EPSILON = 1e-28;
public static final double MINTVAL = 1e-3;
public static final double INFINITY = Double.POSITIVE_INFINITY;
protected static final Color defcolor = Color.black;
protected Color ambient;
protected Color diffuse;
protected Color specular;
protected Color reflect;
protected Color transparent;
protected Color blend;
protected Texture texture;
protected double shine;
protected Vec3d center;
protected Vec3d pole;

protected final void setProps(Color amb, Color diff, Color spec,
				Color refl, Color trans, Color blen,
				Texture tex, double sh,
				Vec3d cen, Vec3d pol)
{
ambient = (amb==null) ? defcolor : amb;
blend = (blen==null) ? defcolor : blen;
diffuse = (diff==null) ? defcolor : diff.mult(blend.invert());
specular = (spec==null) ? defcolor : spec;
reflect = (refl==null) ? defcolor : refl;
transparent = (trans==null) ? defcolor : trans;
texture = tex;
shine = sh;
center = cen;
pole = pol.normalize();
/*
System.out.println("Ambient = "+ambient);
System.out.println("Diffuse = "+diffuse);
System.out.println("Specular = "+specular);
System.out.println("Center = "+center);
System.out.println("Pole = "+pole);
*/
}

public final static boolean eps(double a, double b)
{
double c = Math.abs(a-b);
return (c<EPSILON);
}

public final double getShine()
{ return shine; }

public final Color doTex(Vec3d point)
{
if (texture==null)
	return Color.black;
else
	return texture.colorAt(point, this);
}

public abstract Vec3d normalAt(Vec3d point);
public abstract UV uvAt(Vec3d point);
public final Color colorAt(int level, World w, Vec3d point, Vec3d normal,
			Vec3d incident, Color spec, Color diff)
{
Color c[] = {
	ambient, diffuse.mult(diff), specular.mult(spec),
	reflect.mult(w.traceRay(level+1, point, incident.reflect(normal))),
	blend.mult(doTex(point))
	};
/*
System.out.println("Ambient: "+c[0]);
System.out.println("Diffuse: "+c[1]);
System.out.println("Specular: "+c[2]);
*/
return Color.addup(c);
}

public abstract double intersect(Vec3d origin, Vec3d ray);

}
