/********************************************************************
 * Author : maord,amirlaz
 * ID      : 300564770,300331592
 * Course  : Computer Graphics
 * Project : Ex2
 *
\******************************************************************/

//////////////////////////////
// Project includes         //
//////////////////////////////
#include <GL/glut.h>
#include <GL/gl.h>
#include <GL/glu.h>
#include <iostream>
#include <stdio.h>
#include "circle.h"
#include "arcBall.h"
#include "ex2.h"

//display
float fov = INITIAL_FOV;
float objDepth = 10.0;
float objRadius = 2.0;

//global variables
float norm_x,norm_y;
float scale=1.0;
int width=WINDOW_SIZE;
int height=WINDOW_SIZE;


//translate variables
float x_start_tran=0.0,y_start_tran=0.0;
bool right_pressed=false;

//scaling variables
float start_scale,scale_factor=1.0;
bool middle_pressed=false;

//rotation variables
float start_x_rotation,start_y_rotation,start_z_rotation;
float current_x_rotation,current_y_rotation,current_z_rotation;
bool left_pressed=false;

//circle variables
float circle_radius=0.8;
Circle* circle;

//arcBall object
ArcBall* arcBall;

//Matrixes
GLfloat oldTransforms[16];
GLfloat sphereTransforms[16];
GLfloat currentTransform[16];


//Mesh objects
Mesh mesh;
Mesh::Point lowerLeft(0,0,0);
Mesh::Point upperRight(0,0,0);
Mesh::Point center;
float objectRealLength;

//state boolean variables
bool drawType=false;
bool normalTypeSmooth = false;
bool stateModelMove = true;
short lightType = 0;


//light parameters
float spotAngle = NO_SPOT_ANGLE;
//GLfloat light_ambient[] = { 0.0, 0.0, 0.0, 1.0 };
//GLfloat light_diffuse[] = { 1.0, 1.0, 1.0, 1.0 };
//GLfloat light_specular[] = { 1.0, 1.0, 1.0, 1.0 };
GLfloat light_position[]= { 0.0,0.0,0.0, 0.0 };


GLfloat ambient[] = {0.2, 0.2, 0.2, 1.0};
GLfloat diffuse[] = {1.0, 0.8, 0.0, 1.0};
GLfloat specular[] = {1.0, 1.0, 1.0, 1.0};
GLfloat shine = 100.0;

//////////////////////////////
// Functions definitions    //
//////////////////////////////


/*
 * initialize a given matrix with the identity matrix.
 * matrix - the matrix to initialize
 */
void initialMatrix(GLfloat* matrix)
{
	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();
	glLoadIdentity();
	glGetFloatv(GL_MODELVIEW_MATRIX,matrix);
	glPopMatrix();

}


/*
 * updating the oldTransforms matrix with the currentTrasform matrix.
 * The function does the following multiplication:
 * oldTransforms = currentTransfrom*oldTransforms.
 * (will be called when a button will be released).
 */
void updateOldTransformsMatrix()
{
	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();
	glLoadIdentity();
	glMultMatrixf(currentTransform);
	if (stateModelMove){
		glMultMatrixf(oldTransforms);
		glGetFloatv(GL_MODELVIEW_MATRIX,oldTransforms);
	}
	else{
		glMultMatrixf(sphereTransforms);
		glGetFloatv(GL_MODELVIEW_MATRIX,sphereTransforms);
	}
	glPopMatrix();

}



/*
 * based on the model, compute the distance between the lower left corner
 * and the top right corder. So, we get new radius and therefore new objDepth.
 * By the proper DEPTH2RADIUS_RATIO, we get similar result to school's solution.
 */
void computeObjectInitScale()
{
	float x_dif_up = upperRight[0] - center[0];
	float y_dif_up = upperRight[1] - center[1];
	float z_dif_up = upperRight[2] - center[2];

	float x_dif_low = center[0]  - lowerLeft[0];
	float y_dif_low = center[1]  - lowerLeft[1];
	float z_dif_low = center[2]  - lowerLeft[2] ;

	float diff_up = sqrt(x_dif_up*x_dif_up + y_dif_up*y_dif_up + z_dif_up*z_dif_up);
	float diff_low = sqrt(x_dif_low*x_dif_low + y_dif_low*y_dif_low + z_dif_low*z_dif_low);

	objRadius = diff_up > diff_low ? diff_up : diff_low;
	objDepth = DEPTH2RADIUS_RATIO*objRadius;
	cout << "objDepth= " <<objDepth <<endl;
}


/********************************************************************
 * Function  :  main
 * Arguments : argc  : number of arguments in argv
 * Returns   : argv  : command line arguments
 * Throws    : n/a
 *
 * Purpose   : This is the main program function, It takes care of all the program flow.
 *             It handles the command line arguments, creating the ArcBall, and throwing
 *             everything else on the GLUT to handle.
 *
\******************************************************************/
// checking
int main(int argc, char * argv[])
{
	// check correct usage  //
	if (argc != ARGUMENTS_REQUIRED)
	{
		fprintf(stderr, "Usage: %s <input_file>\n", argv[ARGUMENTS_PROGRAM]);
		return RC_INVALID_ARGUMENTS;
	}

	// Initialize GLUT  //
	glutInit(&argc, argv) ;
	glutInitDisplayMode(GLUT_DEPTH | GLUT_DOUBLE | GLUT_RGB) ;
	glutInitWindowSize(width, height);
	glutInitWindowPosition(WINDOW_POS_X, WINDOW_POS_Y);
	glutCreateWindow(argv[ARGUMENTS_PROGRAM]);

	// Initialize objects //
	circle = new Circle(circle_radius,GREEN);
	arcBall = new ArcBall();

	// Initialize openGL  //
	initGL();

	// Filling callback functions //
	glutDisplayFunc(display) ;
	glutReshapeFunc(reshape) ;
	glutKeyboardFunc(keyboard);
	glutMouseFunc(mouse);
	glutMotionFunc(motion);




	// try to load the mesh from the given input file //

	if (!OpenMesh::IO::read_mesh(mesh, argv[ARGUMENTS_INPUTFILE]))
	{
		// if we didn't make it, exit...  //
		fprintf(stderr, "Error loading mesh, Aborting.\n");
		return RC_INPUT_ERROR;
	}

	printf("number of vertices is %d\n", mesh.n_vertices());
	printf("number of faces is %d\n", mesh.n_faces());
	printf("number of edges is %d\n", mesh.n_edges());

	//initial matrixs
	initialMatrix(oldTransforms);
	initialMatrix(sphereTransforms);
	initialMatrix(currentTransform);


	computeCenterAndBoundingBox(mesh);

	// calculate new objRadius and objDepth
	computeObjectInitScale();

	//update initial light position
	light_position[1] = objRadius*1.1;

	// enter the main loop  //
	glutMainLoop();

	delete circle;
	delete arcBall;
	return RC_OK;
}

/**
 * calculating the view port according to window size.
 */
void viewPortCorrection()
{
	int maxSize = width > height ?  width : height;
	if (width > height) {
		glViewport(0,(height - width)/2.0,maxSize,maxSize);
	}
	else {
		glViewport((width - height)/2.0,0,maxSize,maxSize);
	}

}

void getRandomMaterial()
{
	float lowest=-1.0, highest=1.0;
	float range=(highest-lowest);
	int i=0;
	//set ambient array
	for (i=0;i<3;i++)
		ambient[i] = lowest+range*rand()/(RAND_MAX + 1.0);
	//set diffuse array
	i=0;
	for (i=0;i<3;i++)
		diffuse[i] = lowest+range*rand()/(RAND_MAX + 1.0);
	//set specular array
	i=0;
	for (i=0;i<3;i++)
		specular[i] = lowest+range*rand()/(RAND_MAX + 1.0);

}

void setMaterial(){
	glMaterialfv(GL_FRONT, GL_AMBIENT, ambient);
	glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse);
	glMaterialfv(GL_FRONT, GL_SPECULAR, specular);
	glMaterialf(GL_FRONT, GL_SHININESS, shine);
}



/********************************************************************
 * Function  : init
 * Arguments : n/a
 * Returns   : n/a
 * Throws    : n/a
 *
 * Purpose   : This function is used to initialize some ultra important OpenGL settings.
 *             Currently, it just takes care of the background color.
 *
\******************************************************************/
void initGL(void)
{
	//set initial normals
	mesh.request_vertex_normals();
	mesh.request_face_normals();

	//for the random numbers generator
	srand((unsigned)time(0));

	// Set the background color to black (a shocking surprise)  //
	glClearColor (0.0, 0.0, 0.0, 0.0);
	glShadeModel (GL_SMOOTH);


	//glLightfv(GL_LIGHT0, GL_AMBIENT, light_ambient);
	//glLightfv(GL_LIGHT0, GL_DIFFUSE, light_diffuse);
	//glLightfv(GL_LIGHT0, GL_SPECULAR, light_specular);


	glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, GL_TRUE);




	setMaterial();

	glEnable(GL_DEPTH_TEST);
	glEnable(GL_NORMALIZE);



	return;
}

/********************************************************************
 * Function  :  display
 * Arguments : n/a
 * Returns   : n/a
 * Throws    : n/a
 *
 * Purpose   : This function is used to display the current animation frame.
 *
 *
\******************************************************************/
void display(void)
{


	cout << "lightPos: " << light_position[0] << "," << light_position[1] << "," << light_position[2] << "," << light_position[3] << endl;
	//glLightfv(GL_LIGHT0, GL_POSITION, light_position);
	//glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, spotAngle);

	//GLfloat light_position[] = { 0.0,circle->getCircleRadius()*1.1,-temp, 0.0 };


	// Clear the screen buffer  //
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT) ;

	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();

	// and set the perspective with the current field of view, and the current height and width //
	gluPerspective(fov, 1.0, objDepth - 2*objRadius, objDepth + 2*objRadius);

	//draws the model
	glColor3f(RED);
	glMatrixMode(GL_MODELVIEW) ;
	glLoadIdentity();
	glTranslatef(0.0,0.0, (-1)*objDepth);
	if (stateModelMove)
		glMultMatrixf(currentTransform);
	glMultMatrixf(oldTransforms);


	//fixing the model to fit our screen
	glTranslatef(-center[0],-center[1],-center[2]);

	//updating normals
	mesh.update_normals();

	if (drawType){
		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		drawSolid(mesh);
	}
	else{
		glDisable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		drawModel(mesh);
	}

	//drawing light source

	//in case of LIGHTING enable closes the light source.otherwise doesn't change nothing
	glDisable(GL_LIGHTING);
	glColor3f(WHITE);
	glMatrixMode(GL_MODELVIEW) ;
	glLoadIdentity() ;

	glTranslatef(0.0,0.0,-objDepth);
	if (!stateModelMove)
		glMultMatrixf(currentTransform);
	glMultMatrixf(sphereTransforms);
	glTranslatef(0.0,1.1*objRadius,0.0);


	// drawing a 3D light sphere
	if (drawType) glutSolidSphere(0.07*objRadius, 16,16);
	else glutWireSphere(0.07*objRadius, 16,16);
	glLightfv(GL_LIGHT0, GL_POSITION, light_position);
	glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, spotAngle);

	//drawing 2D circle
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	gluOrtho2D(-1.0,1.0,-1.0,1.0);
	glMatrixMode(GL_MODELVIEW) ;
	glLoadIdentity() ;
	circle->drawCircle();

	//in case of LIGHTING enable closes the light source.otherwise doesn't change nothing
	glEnable(GL_LIGHTING);



	// Make sure everything gets painted  //
	glFlush() ;

	// Swap those buffers so someone will actually see the results... //
	glutSwapBuffers();
}


/********************************************************************
 * Function  :  reshape
 * Arguments : w  : new width of the window
 *              h  : new height of the window
 * Returns   : n/a
 * Throws    : n/a
 *
 * Purpose   : this function handles change of the window dimensions.
 *
\******************************************************************/
void reshape(int w, int h)
{

	width = w;
	height = h;
	glMatrixMode(GL_PROJECTION);
	viewPortCorrection();
	glutPostRedisplay();

	return;
}


/********************************************************************
 * Function  :  keyboard
 * Arguments :  key : the key that was pressed
 *             x   : x value of the current mouse location
 *             y   : y value of the current mouse location
 * Returns   : n/a
 * Throws    : n/a
 *
 * Purpose   : This function handles all the keyboard input from the user.
 *             It supports terminating the application when the KEY_QUIT is pressed.
 *
\******************************************************************/
void keyboard(unsigned char key, int x, int y)
{

	switch (key)
	{
	case KEY_UPPER_QUIT:
	case KEY_QUIT:
	{
		exit(0);
		break;

	}
	case KEY_UPPER_RESET:
	case KEY_RESET:
	{

		initialMatrix(currentTransform);
		initialMatrix(oldTransforms);
		initialMatrix(sphereTransforms);
		fov = INITIAL_FOV;
		//glutPostRedisplay();
		break;
	}

	case 'W':
	case 'w':
	{
		drawType = !drawType;
		//glutPostRedisplay();
		break;
	}

	case 'N':
	case 'n':
	{
		normalTypeSmooth = !normalTypeSmooth;
		//glutPostRedisplay();
		break;
	}


	case 'A':
	case 'a':
	{
		stateModelMove = !stateModelMove;
		//glutPostRedisplay();
		break;
	}

	case 'M':
	case 'm':
	{
		getRandomMaterial();
		setMaterial();
		//glutPostRedisplay();
		break;
	}

	case 'L':
	case 'l':
	{
		lightType = (lightType+1) % 3;
		cout << "Light type: " << lightType << endl;
		switch (lightType){
		case 0:{ //point light
			cout << "here in 0" << endl;
			spotAngle = NO_SPOT_ANGLE;
			light_position[3] = 0.0;
			glLightfv(GL_LIGHT0, GL_POSITION, light_position);
			glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, spotAngle);
			break;
		}
		case 1:{ //spot light
			cout << "here in 1" << endl;
			//GLfloat spotDirection[] = {light_position[0],light_position[1],light_position[2]};
			//glLightfv(GL_LIGHT0, GL_SPOT_DIRECTION, spotDirection);
			light_position[3] = 1.0;
			spotAngle = INITIAL_SPOT_ANGLE;
			glLightfv(GL_LIGHT0, GL_POSITION, light_position);
			glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, spotAngle);
			break;
		}
		case 2:{
			cout << "here in 2" << endl;
			spotAngle = NO_SPOT_ANGLE;
			light_position[3] = 1.0;
			glLightfv(GL_LIGHT0, GL_POSITION, light_position);
			glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, spotAngle);
			break;
		}
		}
		break;
	}

	case '+':
	{
		if (lightType == 1){
		if (spotAngle < NO_SPOT_ANGLE/2)
			cout <<"SpotAngle: " << spotAngle++ << endl;
		glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, spotAngle);
		//glutPostRedisplay();
		}
		break;
	}

	case '-':{

		if (lightType == 1){
		if (spotAngle > INITIAL_SPOT_ANGLE)
			spotAngle--;
		cout <<"SpotAngle: " << spotAngle << endl;
		glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, spotAngle);
		//glutPostRedisplay();
		}
		break;
	}
	}
	glutPostRedisplay();
}




/*
 * given the coordinates of the mouse, normalize them the the range [-1 1]
 * from [0 width/height]
 */
void normalize_mouse_coordinates(float x,float y)
{
	norm_x = 2*((float)x/width - 0.5);
	norm_y = -2*((float)y/height - 0.5);
}


/********************************************************************
 * Function  :   mouse
 * Arguments :   button  : the button that was engaged in some action
 *               state   : the new state of that button
 *               x       : x value of the current mouse location
 *               y       : y value of the current mouse location
 * Returns   :   n/a
 * Throws    :   n/a
 *
 * Purpose   :   This function handles mouse actions.
 *
\******************************************************************/


void mouse(int button, int state, int x, int y)
{
	normalize_mouse_coordinates(x,y);


	if (button==RIGHT_BUTTON){
		if (state==KEY_PRESSED){
			x_start_tran = norm_x;
			y_start_tran = norm_y;
			right_pressed=true;

		}
		else {
			right_pressed=false;
			updateOldTransformsMatrix();
			initialMatrix(currentTransform);
		}
	}

	if (button==MIDDLE_BUTTON){
		if (state==KEY_PRESSED) {
			middle_pressed = true;
			start_scale = norm_y;
		}
		else middle_pressed = false;
	}

	if (button==LEFT_BUTTON){
		if (state == KEY_PRESSED && circle->insideBall(norm_x,norm_y)) {
			left_pressed = true;
			start_x_rotation = norm_x;
			start_y_rotation = norm_y;
			start_z_rotation = sqrt(circle->getCircleRadius()*circle->getCircleRadius() -norm_x*norm_x-norm_y*norm_y);
		}
		else{
			left_pressed=false;
			updateOldTransformsMatrix();
			initialMatrix(currentTransform);
		}
	}
	return;
}

/********************************************************************
 * Function  :   motion
 * Arguments :   x   : x value of the current mouse location
 *               y   : y value of the current mouse location
 * Returns   :   n/a
 * Throws    :   n/a
 *
 * Purpose   :   This function handles mouse dragging events.
 *
\******************************************************************/
void motion(int x, int y)
{
	normalize_mouse_coordinates(x,y);
	initialMatrix(currentTransform);
	if (middle_pressed) arcBall->applyScale(&start_scale,norm_y,&fov);
	if (left_pressed)
		arcBall->applyRotate(start_x_rotation,start_y_rotation,start_z_rotation,norm_x,norm_y,circle->insideBall(norm_x,norm_y) ?
				sqrt(circle->getCircleRadius() * circle->getCircleRadius()-norm_x*norm_x -norm_y*norm_y) : 0.0,currentTransform);
	if (right_pressed)  arcBall->applyTranslate(x_start_tran,y_start_tran,norm_x,norm_y,
			fov,INITIAL_FOV,(float)height/width,objDepth -2*objRadius,currentTransform);

	glutPostRedisplay();
	return;
}


void drawSolid(Mesh& mesh)
{

	Mesh::FaceIter fIter;
	Mesh::FaceHandle fHandle;
	Mesh::VertexIter vIter;
	Mesh::VertexHandle vHandle;
	Mesh::Point point;
	Mesh::FaceVertexIter fvIter;
	Vector3F normal;

	{

		for (fIter = mesh.faces_begin(); fIter != mesh.faces_end(); ++fIter)
		{
			fHandle = fIter.handle();
			fvIter = mesh.fv_iter(fHandle);
			glBegin(GL_POLYGON);
			for (fvIter = mesh.fv_iter(fHandle); fvIter ;++fvIter)
			{
				vHandle = fvIter.handle();
				point = mesh.point(vHandle);
				// pressing N changes normal calculation
				if (normalTypeSmooth)
					normal = mesh.normal(vHandle);
				else
					normal = mesh.normal(fHandle);

				glNormal3f(normal[0],normal[1],normal[2]);
				glVertex3d(point[0],point[1],point[2]);

			}
			glEnd();


		}
	}
}



	//////////////////////////////
	// OpenMesh functions       //
	//////////////////////////////

	/*
	 * iterating over the edges in the model. Drawing every edge to the screen.
	 * mesh - the mesh object of the model
	 */
	void drawModel(Mesh& mesh)
	{
		Mesh::EdgeIter eIter;
		Mesh::EdgeHandle eHandle;
		Mesh::VertexHandle vHandle1,vHandle2;
		Mesh::Point p1,p2;




		glBegin(GL_LINES);
		for (eIter = mesh.edges_begin();eIter != mesh.edges_end(); ++eIter)
		{  //glViewport(0,0,width,height);
			eHandle = eIter.handle();
			vHandle1 = mesh.from_vertex_handle(mesh.halfedge_handle(eHandle,0));
			vHandle2 = mesh.to_vertex_handle(mesh.halfedge_handle(eHandle,0));


			p1 = mesh.point(vHandle1);
			p2 = mesh.point(vHandle2);


			glVertex3f(p1[0],p1[1],p1[2]);
			glVertex3f(p2[0],p2[1],p2[2]);





		}

		glEnd();
	}

	/*
  This function computes the geometrical center and
  the axis aligned bounding box of the object.
  The bounding box is represented by the lower left and upper right
  corners.
	 */

	void computeCenterAndBoundingBox(Mesh& mesh)
	{
		/* Vertex iterator is an iterator which goes over all the vertices of the mesh */
		Mesh::VertexIter vertexIter;

		/* This is the specific class used to store the geometrical position of the vertices of the mesh */
		Mesh::Point p;

		/* number of vertices in the mesh */
		int vNum = mesh.n_vertices();


		vertexIter = mesh.vertices_begin();
		lowerLeft = upperRight = mesh.point(vertexIter);

		/* This is how to go over all the vertices in the mesh */
		for (vertexIter = mesh.vertices_begin();vertexIter != mesh.vertices_end();++vertexIter)
		{
			/* this is how to get the point associated with the vertex */
			p = mesh.point(vertexIter);
			center += p;
			for (int i = 0;i < 3;i++)
			{
				if (lowerLeft[i] > p[i])
					lowerLeft[i] = p[i];
				if (upperRight[i] < p[i])
					upperRight[i] = p[i];
			}
		}
		center /= (double)vNum;
	}

