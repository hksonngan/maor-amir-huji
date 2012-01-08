#include <GL/glew.h>
#include <GL/glut.h>

void main(int argc, char **argv) {

	glutInit(&argc, argv);
	...
	glewInit();

	if (glewIsSupported("GL_VERSION_2_0"))
		printf("Ready for OpenGL 2.0\n");
	else {
		printf("OpenGL 2.0 not supported\n");
		exit(1);
	}
	setShaders();

        glutMainLoop();
}