
#include "circle.h"
#include <GL/glut.h>
#include <GL/gl.h>
#include <GL/glu.h>
#include <math.h>

/*
 * constructor
 */
Circle::Circle(float rad,float r, float g, float b)
{
	radius = rad;
	r_color = r;
	g_color = g;
	b_color = b;
}

/*
 * draws the circle based on the color and radius given in the constructor
 */
void Circle::drawCircle()
{
	 float angle;

	  glColor3f(r_color,g_color,b_color);
	  glBegin(GL_LINE_LOOP);
	      for(int i = 0; i < 100; i++) {
	          angle = i*2*M_PI/100;
	          glVertex3f((cos(angle) * radius),(sin(angle) * radius),0.0);
	      }
	  glEnd();

}

/*
 * returns the circle radius
 */
float Circle::getCircleRadius()
{
	return radius;

}

Circle::~Circle(void){}

/*
 * given x,y normalized coordinates (in the range [-1 1]) returns true
 * if the coordinates are inside the circle.
 * x - the x normalized coordinate
 * y - the y normalized coordinate
 */
bool Circle::insideBall(float x,float y)
{
	return x*x + y*y <= radius*radius;
}
