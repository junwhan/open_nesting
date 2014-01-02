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

/** A Vector class with 3 doubles - (x,y,z) */

public final class Vec3d implements java.io.Serializable {

    public static final double EPSILON = 1e-6;
    public static final Vec3d ZERO = new Vec3d(0,0,0);
    public static final Vec3d X = new Vec3d(1,0,0);
    public static final Vec3d Y = new Vec3d(0,1,0);
    public static final Vec3d Z = new Vec3d(0,0,1);
    private double x,y,z;

    /** Creates and initializes a vector as (x,y,z) */
    public Vec3d(double x,double y,double z){
        set(x,y,z);
    }

    /** Creates and initializes a vector as (v[0],v[1],v[2]) */
    public Vec3d(double v[]){
        set(v);
    }

    /** Sets the value of this vector to (x,y,z) */
    private final void set(double x, double y, double z){
        this.x=x; this.y=y; this.z=z;
    }

    /** Sets the value of this vector to (v[0],v[1],v[2]) */
    private final void set(double[] v){
        x=v[0]; y=v[1]; z=v[2];
    }

    /** Copies this vector's values into array v */
    public final void get(double[] v){
        v[0]=x; v[1]=y; v[2]=z;
    }

    /** Returns this vector's values as an array */
    public final double[] get(){
        double[] ret={x,y,z};
        return ret;
    }

    /** Returns the reflection of this vector around the normal n */
    public final Vec3d reflect(Vec3d n){
	double l = 2*(n.x*x+n.y*y+n.z*z)/n.lengthSquared();
	return new Vec3d(x-l*n.x, y-l*n.y, z-l*n.z);
//	return sub(n.mult(2*dot(n)/n.lengthSquared()));
    }

    /** Returns the vector sum of this and v */
    public final Vec3d add(Vec3d v){
        return new Vec3d(x+v.x, y+v.y, z+v.z);
    }

    /** Returns this shifted EPSILON along v */
    public final Vec3d addeps(Vec3d v){
        return new Vec3d(x+(v.x*EPSILON), y+(v.y*EPSILON), z+(v.z*EPSILON));
    }

    /** Returns the normalized vector sum of this and v */
    public final Vec3d addnorm(Vec3d v) {
	double x1, y1, z1, l;
	x1 = x+v.x;
	y1 = y+v.y;
	z1 = z+v.z;
	l = Math.sqrt(x1*x1+y1*y1+z1*z1);
	if (l!=0)
		return new Vec3d(x1/l, y1/l, z1/l);
	else
		return new Vec3d(0,0,0);
    }

    /** Returns this-v */
    public final Vec3d sub(Vec3d v){
        return new Vec3d(x-v.x, y-v.y, z-v.z);
    }

    /** Returns the vector cross product of this vector and v */
    public final Vec3d cross(Vec3d v) {
	return new Vec3d(
	    (y * v.z) - (z * v.y),
	    (z * v.x) - (x * v.z),
	    (x * v.y) - (y * v.x)
	    );
    }

    /** Returns the normalized vector cross product of this vector and v */
    public final Vec3d crossnorm(Vec3d v) {
	double x1, y1, z1, l;
	x1 = (y * v.z) - (z * v.y);
	y1 = (z * v.x) - (x * v.z);
	z1 = (x * v.y) - (y * v.x);
	l = Math.sqrt(x1*x1+y1*y1+z1*z1);
	if (l!=0)
		return new Vec3d(x1/l, y1/l, z1/l);
	else
		return new Vec3d(0,0,0);
    }

    /** Returns the vector opposite to this one */
    public final Vec3d negate(){
        return new Vec3d(-x, -y, -z);
    }

    /** Returns this vector normalized */
    public final Vec3d normalize(){
        double l=length();
        if(l!=0)
            return new Vec3d(x/l, y/l, z/l);
        else
	    return this;
    }

    /** Returns this vector scaled by s */
    public final Vec3d mult(double s){
        return new Vec3d(x*s, y*s, z*s);
    }

    /** Returns the squared length of this vector */
    public final double lengthSquared(){
        return x*x + y*y + z*z;
    }

    /** Returns the length of this vector */
    public final double length(){
        return Math.sqrt(lengthSquared());
    }

    /** Returns the squared distance between this vector and v */
    public final double distanceSquared(Vec3d v){
        return (x-v.x)*(x-v.x) + (y-v.y)*(y-v.y) + (z-v.z)*(z-v.z);
    }

    /** Returns the distance between this vector and vector v */
    public final double distance(Vec3d v){
        return Math.sqrt(distanceSquared(v));
    }

    /** Returns the dot product between this vector and v */
    public final double dot(Vec3d v){
        return x*v.x + y*v.y + z*v.z;
    }

    /** Returns true if this vector is equal to v */
    public boolean equals(Vec3d v){
        return x==v.x && y==v.y && z==v.z;
    }

    /** Returns a hash number based on the data values in this object.
      This currently calls super.hashCode()  */
    public int hashCode(){
        return super.hashCode();
    }

    /** Returns a string that contains the values of thie vector */
    public String toString(){
        return new String("Vec3d "+x+":"+y+":"+z);
    }

    // ADDITIONAL FUNCTIONALITY

    /** Gets the ith component of this vector */
    public final double get(int i){
        switch(i){
        case 0: return x; 
        case 1: return y;
        case 2: return z;
        default: System.err.println("Vec3d.get(int) must be 0,1, or 2"); return -1;
        }
    }

    /** Gets the x component of the vector */
    public final double getX() { return x; }

    /** Gets the y component of the vector */
    public final double getY() { return y; }

    /** Gets the z component of the vector */
    public final double getZ() { return z; }

}
