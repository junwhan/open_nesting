.SUFFIXES: .java .class
FLAGS =

.java.class:
	javac $(FLAGS) $*.java

all:	all_ray all_data all_light all_object all_textures all_output
	sync

all_ray: \
	RayTracer.class \
	World.class

all_data:	\
	data/Camera.class \
	data/Color.class \
	data/UV.class \
	data/Vec3d.class

all_light:	\
	light/DirectionalLight.class \
	light/Light.class \
	light/PointLight.class

all_object:	\
	object/Obj.class \
	object/Sphere.class

all_output:	\
	output/PPMplotter.class \
	output/Plotter.class

all_textures:	\
	texture/GradientTexture.class \
	texture/Texture.class

clean:
	rm -f *.class
	rm -f data/*.class
	rm -f light/*.class
	rm -f object/*.class
	rm -f output/*.class
	rm -f texture/*.class

