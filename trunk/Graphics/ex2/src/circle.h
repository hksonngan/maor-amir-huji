
#ifndef CIRCLE_H_
#define CIRCLE_H_


/*
 * a simple class for drawing a 2D circle to screen
 */
class Circle{

public:

	Circle(float radius,float r_color,float g_color,float b_color);
	virtual ~Circle();
	void drawCircle();
	float getCircleRadius();
	bool insideBall(float x,float y);




private:
	float radius;
	float r_color;
	float g_color;
	float b_color;




};


#endif /* CIRCLE_H_ */
