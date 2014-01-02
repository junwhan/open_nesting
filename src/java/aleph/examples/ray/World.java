package aleph.examples.ray;
import aleph.Aleph;
import aleph.Constants;
import aleph.examples.ray.data.*;
import aleph.examples.ray.object.*;
import aleph.examples.ray.light.*;
import aleph.examples.ray.output.*;
import java.awt.Image;
import java.awt.image.*;
import java.util.Vector;
import java.util.Enumeration;

public class World implements java.io.Serializable {
public static final double INFINITY = Double.POSITIVE_INFINITY;
int MAX_RECURSE = 5;
Vector lights = new Vector();
Vector objects = new Vector();
boolean rendering = false;
int renders = 0;

  public World(boolean toy)
  {
    //This is a toy world
    MAX_RECURSE=3;
    if (Aleph.verbosity(Constants.LOQUACIOUS))
      System.out.println("Creating world.");
    addObj(new Sphere(
		      new Color(.1,0,0), Color.blue, Color.green,
		      Color.black, Color.black, Color.black, null, 20,
		      new Vec3d(-1,.5,0), Vec3d.Y, .75
		      ));
    addObj(new Sphere(
		      new Color(.1,0,0), Color.blue, Color.green,
		      Color.gray, Color.black, Color.black, null, 20,
		      new Vec3d(1,-.5,0), Vec3d.Y, .75
		      ));
    addLight(new DirectionalLight(Color.white, Vec3d.Y.negate()));
    //addLight(new PointLight(Color.white, Vec3d.Y.negate()));
    setRendering(true);
    if (Aleph.verbosity(Constants.LOQUACIOUS))
      System.out.println("Created world.");
  }
  
public static void main(String args[])
{
int width = 500;
int height = 500;
World w = new World(true);
PPMplotter ppm[] = { new PPMplotter("test.ppm") };
Camera camera = new Camera(Vec3d.Z.negate(), Vec3d.Y,
		new Vec3d(0,0,3), Math.PI/2.0, width, height, ppm);
ppm[0].setSize(width, height);
render(w, camera);
}

public World(int max)
{ MAX_RECURSE=max; }

public synchronized void addObj(Obj obj)
{
if (rendering) return;
objects.addElement(obj);
}

public synchronized void addLight(Light light)
{
if (rendering) return;
lights.addElement(light);
}

public synchronized boolean setRendering(boolean r)
{
if (renders==0) rendering = r;
return rendering;
}

protected synchronized boolean testsetRender()
{
if (rendering) renders++;
return rendering;
}

protected synchronized void doneRendering()
{ if (rendering&&renders>0) renders--; }

public static void render(World world, Camera camera)
{
Enumeration e;
int pixel[] = new int[2];
int i,j;
Vec3d pos = camera.getPos();
i = 0;
while (camera.hasMoreElements()) {
	Color c = world.traceRay(0, pos, camera.nextRay(pixel));
	camera.writePixel(pixel[0], pixel[1], c);
	}
System.err.println("Writing file");
camera.endPlot();
}

public static Obj intersect(Enumeration e, Vec3d origin,
				Vec3d ray, double tval[])
{
Obj obj = null;

while (e.hasMoreElements()) {
	Obj curobj = (Obj)e.nextElement();
	double nval = curobj.intersect(origin, ray);
	if (nval<tval[0]) {
		tval[0] = nval;
		obj = curobj;
		}
	}
return obj;
}

public void calcLight(Enumeration e, Vec3d incident, Vec3d point,
		Vec3d normal, Color diff[], Color spec[], double shine)
{
int i=0;
double dir[] = new double[3];
double dist[] = new double[1];

while (e.hasMoreElements()) {
	Light light = (Light)e.nextElement();
	dist[0] = light.toLight(point, dir);
	Vec3d toLight = new Vec3d(dir);
	double falloff = light.falloff(dist[0]);
//System.out.println("Light is "+dist[0]+
//" far away along "+toLight+" from "+point);
	Obj shadow = World.intersect(objects.elements(),
			point.addeps(toLight), toLight, dist);
	if (shadow==null) {
		diff[i] = light.calcDiffuse(falloff, toLight, normal);
		spec[i] = light.calcSpecular(falloff, incident, toLight,
						normal, shine);
	} else {
//System.out.println("Found shadow object "+dist[0]+" far away");
		diff[i] = Color.black;
		spec[i] = Color.black;
		}
	i++;
	}
}

public Color traceRay(int level, Vec3d origin, Vec3d ray)
{
if (level>MAX_RECURSE) return Color.black;
Thread.yield();
Color c, spec[], diff[];
int i = 0;
double dist[] = { INFINITY };
Enumeration e = objects.elements();
Vec3d normal, point;
Obj obj = World.intersect(e, origin, ray, dist);

if (obj==null) return Color.black;
point = ray.mult(dist[0]).add(origin);
normal = obj.normalAt(point);
e = lights.elements();
spec = new Color[lights.size()];
diff = new Color[spec.length];
calcLight(e, ray, point, normal, diff, spec, obj.getShine());
return obj.colorAt(level, this, point, normal, ray,
		Color.addup(spec), Color.addup(diff));
}

}

