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

public final class Color implements java.io.Serializable {
public final static Color black = new Color(0,0,0);
public final static Color red = new Color(1,0,0);
public final static Color green = new Color(0,1,0);
public final static Color blue = new Color(0,0,1);
public final static Color white = new Color(1,1,1);
public final static Color cyan = new Color(0,1,1);
public final static Color magenta = new Color(1,0,1);
public final static Color yellow = new Color(1,1,0);
public final static Color gray = new Color(.5,.5,.5);
private double r;
private double g;
private double b;

public Color(double c[])
{
r = c[0];
g = c[1];
b = c[2];
}

public Color(double red, double green, double blue)
{
r = red;
g = green;
b = blue;
}

public boolean equals(Color c)
{ return (r==c.r)&&(g==c.g)&&(b==c.b); }

public void get(double[] c)
{
c[0] = r;
c[1] = g;
c[2] = b;
}

public double[] getColor()
{
double[] c = {r, g, b};
return c;
}

public final static Color addup(Color c[])
{
int i;
double r=0,g=0,b=0;

for (i=0;i<c.length;i++) {
	r += c[i].r;
	g += c[i].g;
	b += c[i].b;
	}
return (((r+g+b)>0) ? new Color(r,g,b) : black);
}

public final Color invert()
{ return new Color(1.0-r, 1.0-g, 1.0-b); }

public final Color blend(Color coeff, Color c)
{
double r=0, g=0, b=0;

r = c.r*coeff.r + r*(1.0-coeff.r);
g = c.g*coeff.g + g*(1.0-coeff.g);
b = c.b*coeff.b + b*(1.0-coeff.b);
return (((r+g+b)>0) ? new Color(r,g,b) : black);
}

public final static Color blend(Color coeff[], Color c[])
{
int i, l = (coeff.length>c.length) ? c.length : coeff.length;
double r=0, g=0, b=0;

for (i=0;i<l;i++) {
	r += c[i].r*coeff[i].r;
	g += c[i].g*coeff[i].g;
	b += c[i].b*coeff[i].b;
	}
return (((r+g+b)>0) ? new Color(r,g,b) : black);
}

public final static Color blend(double coeff[], Color c[])
{
int i, l = (coeff.length>c.length) ? c.length : coeff.length;
double r=0, g=0, b=0;

for (i=0;i<l;i++) {
	r += c[i].r*coeff[i];
	g += c[i].g*coeff[i];
	b += c[i].b*coeff[i];
	}
return (((r+g+b)>0) ? new Color(r,g,b) : black);
}

public final double getRed()
{ return r; }

public final double getGreen()
{ return g; }

public final double getBlue()
{ return b; }

public final Color add(Color c)
{ return new Color(r+c.r, g+c.g, b+c.b); }

public final Color mult(double c[])
{ return new Color(r*c[0], g*c[1], b*c[2]); }

public final Color mult(Color c)
{ return new Color(r*c.r, g*c.g, b*c.b); }

public final Color mult(double s)
{ return new Color(r*s, g*s, b*s); }

public String toString()
{ return "Color("+r+","+g+","+b+")"; }

}
