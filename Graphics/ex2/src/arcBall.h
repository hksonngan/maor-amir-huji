
#ifndef ARCBALL_H_
#define ARCBALL_H_
#include <GL/glut.h>
#include <GL/gl.h>
#include <GL/glu.h>
#include <math.h>
// looks like school solution
#define TRANSLATE_FACTOR 1.52
/*
 * An arc ball class. Given normalized coordinates of two vectors (starting - when the button was clicked, and current)
 * it calculates the rotate/translate/scale factors.
 */
class ArcBall{

public:

	ArcBall();
	virtual ~ArcBall();
	void applyScale(float* start_y,float current_y,float* currentAngle);
	void applyTranslate(float start_x,float start_y,float current_x,float current_y,float currentAngle, float initFov1,float ratio,float near,GLfloat* matrix);
	void applyRotate(float start_x,float start_y,float start_z,float current_x , float current_y , float current_z,GLfloat* matrix);

private:
	void normalizeVector(float* x , float* y , float* z);


};


#endif /* ARCBALL_H_ */
