
#include "arcBall.h"


// empty constructor/destructor
ArcBall::ArcBall(){}
ArcBall::~ArcBall(void){}


/*
 * given y normalized coordinates of the point where the mouse was clicked and the current point,
 * calculate and change the new fov angle, based on the old value of the angle.
 * start_y - a pointer to the starting point's y coordinate
 * current_y - the current point's y coordinate
 * currentAngle - a pointer to the float object who saves the value of the fov angle.
 */
void ArcBall::applyScale(float* start_y,float current_y,float* currentAngle){

	      float scale_factor = 1.0+current_y-*start_y;
		  if (*currentAngle*scale_factor <= 170.0 && *currentAngle*scale_factor >= 1.0)
		  {
			  *currentAngle *= scale_factor;
			  *start_y = current_y;
		  }
}
/*
 * given normalized coordinates of the point where the mouse was clicked and the current point,
 * calculate the parameters to use in order to translate the object and update the given matrix
 * accordingly.
 * start_x - x coordinate of the point where the mouse was clicked.
 * start_y - y coordinate of the point where the mouse was clicked.
 * current_x - x coordinate of the current point.
 * current_y - y coordinate of the current point.
 * currentAngle - current fov angle
 * ratio - current aspect ratio
 * near - current distance to near
 * matrix - the matrix to change
 */
void ArcBall::applyTranslate(float start_x,float start_y,float current_x,float current_y,float currentAngle, float initFov1,float ratio,float near,GLfloat* matrix){

	float x_translate_coordinate,y_translate_coordinate;
	x_translate_coordinate = fabs(near*tan(currentAngle*M_PI/(180.0*2.0)))
			*(current_x - start_x)*TRANSLATE_FACTOR*(initFov1/currentAngle);
	y_translate_coordinate = fabs(near*tan(currentAngle*M_PI/(180.0*2.0))/ratio)
			*(current_y - start_y)*TRANSLATE_FACTOR*(initFov1/currentAngle);

	//update current matrix
	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();
	glLoadMatrixf(matrix);
	glTranslatef(x_translate_coordinate,y_translate_coordinate,0.0);
	glGetFloatv(GL_MODELVIEW_MATRIX,matrix);
	glPopMatrix();

}

/*
 * given normalized coordinates of the point where the mouse was clicked and the current point,
 * calculate the parameters to use in order to rotate the object and update the given matrix
 * accordingly. Assuming: the coordinate are normalized to the range [-1 1] but the length of each
 * vector is not normalized.
 * start_x - x coordinate of the point where the mouse was clicked.
 * start_y - y coordinate of the point where the mouse was clicked.
 * start_z - z coordinate of the point where the mouse was clicked.
 * current_x - x coordinate of the current point.
 * current_y - y coordinate of the current point.
 * current_z - z coordinate of the current point
 * matrix - the matrix to change
 */
void ArcBall::applyRotate(float start_x,float start_y,float start_z,float current_x , float current_y , float current_z,GLfloat* matrix){

		float rot_angle,x_rot_axis,y_rot_axis,z_rot_axis;

		//normalize vectors
		normalizeVector(&start_x,&start_y,&start_z);
		normalizeVector(&current_x,&current_y,&current_z);

		rot_angle = 2*180*acos(start_x*current_x + start_y*current_y+start_z*current_z)/M_PI;
		x_rot_axis = start_y*current_z - start_z*current_y;
		y_rot_axis = start_z*current_x - start_x*current_z;
		z_rot_axis = start_x*current_y - start_y*current_x;

		//update current matrix
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		glLoadMatrixf(matrix);
		glRotatef(rot_angle,x_rot_axis,y_rot_axis,z_rot_axis);
		glGetFloatv(GL_MODELVIEW_MATRIX,matrix);
		glPopMatrix();

}



/*
 * given 3 pointer to float number indicates the values of a vector,
 * normalizing the vector (to length 1: [x,y,z] = [x,y,z]/magnitude[x,y,z])
 * x - a pointer to the x coordinate of the vector
 * y - a pointer to the y coordinate of the vector
 * z - a pointer to the z coordinate of the vector
 */
void ArcBall::normalizeVector(float* x , float* y , float* z)
{
	float magnitude = sqrt(*x*(*x) + *y*(*y)+*z*(*z));

	*x = *x/magnitude;
	*y = *y/magnitude;
	*z = *z/magnitude;
}
