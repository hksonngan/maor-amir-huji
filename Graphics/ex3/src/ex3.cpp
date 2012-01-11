/********************************************************************
 * Author : maord,amirlaz
 * ID      : 300564770,300331592
 * Course  : Computer Graphics
 * Project : Ex3
 *
\******************************************************************/

//////////////////////////////
// Project includes         //
//////////////////////////////

//#include <GL/glut.h>
//#include <GL/gl.h>
//#include <GL/glu.h>

#include <iostream>
#include <stdio.h>

#include "circle.h"
#include "arcBall.h"
#include "ex3.h"

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
bool hiddenSufaceRemovalEnable = false;
short lightType = 0;


//light parameters
float spotAngle = NO_SPOT_ANGLE;
GLfloat light_ambient[] = { 0.8, 0.0, 0.0, 1.0 };
GLfloat light_diffuse[] = { 1.0, 1.0, 1.0, 1.0 };
GLfloat light_specular[] = { 1.0, 1.0, 1.0, 1.0 };
GLfloat light_position[]= { 0.0,0.0,0.0, 1.0 };
GLfloat light_position_directional[]= { 0.0,1.0,0.0, 0.0 };
GLfloat spotDirection[] = {0.0,-1.0,0.0};
GLfloat spotDirection_directional[] = {0.0,0.0,1.0};

GLfloat ambient[] = {0.1, 0.0, 0.0, 1.0};
GLfloat diffuse[] = {0.65, 0.0, 0.0, 1.0};
GLfloat specular[] = {1.0, 1.0, 1.0, 1.0};
GLfloat shine = 100.0;


//shaders variables
GLuint   phong_program_object;  // a handler to the GLSL program used to update
GLuint   phong_vertex_shader;   // a handler to vertex shader of the phong shading ('1')
GLuint   phong_fragment_shader; // a handler to fragment shader of the phong shading ('1')

GLuint   cell_program_object;
GLuint   cell_vertex_shader;   // a handler to vertex shader of the cell shading ('2')
GLuint   cell_fragment_shader; // a handler to fragment shader of the cell shading ('2')

GLuint   procedural_program_object;
GLuint   procedural_vertex_shader;   // a handler to vertex shader of the procedural shading ('3')
GLuint   procedural_fragment_shader; // a handler to fragment shader of the procedural shading ('3')

///////////////////////////// TO DELETE ////////////////////////////////////////////////////
GLuint   blinn_phong_program_object;  // a handler to the GLSL program used to update
GLuint   blinn_phong_vertex_shader;   // a handler to vertex shader of the blinn-phong shading ('5')
GLuint   blinn_phong_fragment_shader; // a handler to fragment shader of the blinn-phong shading ('5')
////////////////////////////////////////////////////////////////////////////////////////////

bool runWithShaders = false;
GLuint* lastShaderRun = NULL;


//////////////////////////////
// Functions definitions    //
//////////////////////////////

/// Print out the information log for a shader object 
/// @arg obj handle for a program object
static void printProgramInfoLog(GLuint obj)
{
	GLint infologLength = 0, charsWritten = 0;
	glGetProgramiv(obj, GL_INFO_LOG_LENGTH, &infologLength);
	if (infologLength > 2) {
		GLchar* infoLog = new GLchar [infologLength];
		glGetProgramInfoLog(obj, infologLength, &charsWritten, infoLog);
		std::cerr << infoLog << std::endl;
		delete infoLog;
	}
}



// Load shader from disk into a null-terminated string
char *LoadShaderText(const char *fileName)
{
    char *shaderText = NULL;
    GLint shaderLength = 0;
    FILE *fp;

    fp = fopen(fileName, "r");
    if (fp != NULL)
    {
        while (fgetc(fp) != EOF)
        {
            shaderLength++;
        }
        rewind(fp);
        shaderText = (GLchar *)malloc(shaderLength+1);
        if (shaderText != NULL)
        {
            fread(shaderText, 1, shaderLength, fp);
        }
        shaderText[shaderLength] = '\0';
        fclose(fp);
    }

    return shaderText;
}

void setupShaders(GLuint* program_object,GLuint* vertex_shader,GLuint* fragment_shader,char* vshader_name,char* fshader_name)
{
	
	*program_object = glCreateProgram();    // creating a program object

	GLchar *vertex_source;
	GLchar *fragment_source;

	//creating and compiling the phong shaders
	vertex_source = LoadShaderText(vshader_name);
	fragment_source = LoadShaderText(fshader_name);
	   
   
   *vertex_shader   = glCreateShader(GL_VERTEX_SHADER);   // creating a vertex shader object
   *fragment_shader = glCreateShader(GL_FRAGMENT_SHADER); // creating a fragment shader object
   printProgramInfoLog(*program_object);
   
   glShaderSource(*vertex_shader, 1, (const GLchar**)&vertex_source, NULL); // assigning the vertex source
   glShaderSource(*fragment_shader, 1, (const GLchar**)&fragment_source, NULL); // assigning the fragment source
   printProgramInfoLog(*program_object);   // verifies if all this is ok so far

   // compiling and attaching the vertex shader onto program
   glCompileShader(*vertex_shader);
   glAttachShader(*program_object, *vertex_shader); 
   printProgramInfoLog(*program_object);   // verifies if all this is ok so far

   // compiling and attaching the fragment shader onto program
   glCompileShader(*fragment_shader);
   glAttachShader(*program_object, *fragment_shader); 
   printProgramInfoLog(*program_object);   // verifies if all this is ok so far
   
   // Link the shaders into a complete GLSL program.
   glLinkProgram(*program_object);
   printProgramInfoLog(*program_object);   // verifies if all this is ok so far

   free(vertex_source);
   free(fragment_source);
    
   // some extra code for checking if all this initialization is ok
   GLint prog_link_success;
   glGetObjectParameterivARB(*program_object, GL_OBJECT_LINK_STATUS_ARB, &prog_link_success);
   if (!prog_link_success) {
     fprintf(stderr, "The shaders could not be linked\n");
      exit(1);
   }
   
	return ;



}


void shadersInit(void)
{
	//init shaders
	setupShaders(&phong_program_object,&phong_vertex_shader,&phong_fragment_shader,PHONG_VS,PHONG_FS);
	setupShaders(&cell_program_object,&cell_vertex_shader,&cell_fragment_shader,CELL_VS,CELL_FS);
	setupShaders(&procedural_program_object,&procedural_vertex_shader,&procedural_fragment_shader,PROCEDURAL_VS,PROCEDURAL_FS);
	
	
	////////////////////////////////////////////////////////// TO DELETE ///////////////////////////////////////////////////////////
	setupShaders(&blinn_phong_program_object,&blinn_phong_vertex_shader,&blinn_phong_fragment_shader,BLINN_PHONG_VS,BLINN_PHONG_FS);
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//run with fixed functionality
	glUseProgram(SHADERS_FIXED_FUNCTIONALITY);
   
	return;
}



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

	//initial matrixs
	initialMatrix(oldTransforms);
	initialMatrix(sphereTransforms);
	initialMatrix(currentTransform);


	computeCenterAndBoundingBox(mesh);

	// calculate new objRadius and objDepth
	computeObjectInitScale();

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
	if (width > height)
		glViewport(0,(height - width)/2.0,maxSize,maxSize);
	else
		glViewport((width - height)/2.0,0,maxSize,maxSize);

}

/*
 * sets material array with random values.
 */
void getRandomMaterial()
{
	float lowest=-1.0, highest=1.0;
	float range=(highest-lowest);
	int i=0;
	//changing diffuse array
	for (i=0;i<3;i++)
		diffuse[i] = lowest+range*rand()/(RAND_MAX + 1.0);


}

/*
 * set material of model according to material array
 */
void setMaterial(){
	glMaterialfv(GL_FRONT, GL_AMBIENT, ambient);
	glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse);
	glMaterialfv(GL_FRONT, GL_SPECULAR, specular);
	glMaterialf(GL_FRONT, GL_SHININESS, shine);
}

/*
 * adding light to the screen according to the light type.
 */
void addLightToScene(){
	switch (lightType){
				case 0:{ //point light
				spotAngle = NO_SPOT_ANGLE;
				glLightfv(GL_LIGHT0, GL_POSITION, light_position);
				break;
			}
			case 1:{ //spot light
				glLightfv(GL_LIGHT0, GL_POSITION, light_position);
				glLightfv(GL_LIGHT0,GL_SPOT_DIRECTION,spotDirection);
				glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, spotAngle);
				break;
			}
			case 2:{ //directional
				spotAngle = NO_SPOT_ANGLE;
				glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, spotAngle);
				glLightfv(GL_LIGHT0, GL_POSITION, light_position_directional);
				break;
			}
		}
}

/*
 * adding the model to the screen according to draw type(shaded or wireframe)
 * and hidden surface removal state.
 */
void addModelToScene(){
	if (drawType){ //case of shaded model to be drawn
		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		drawModel(mesh);
	}
	else{ //case of wireframe model to be drawn
		glDisable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		if (hiddenSufaceRemovalEnable){ //case hidden surface removal is enabled
			glEnable(GL_DEPTH_TEST);
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			glColor3f(RED);
			drawModel(mesh);

			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			glEnable(GL_POLYGON_OFFSET_FILL);
			glPolygonOffset(1.0, 1.0);
			glColor3f(BLACK);
			drawModel(mesh);
			glDisable(GL_POLYGON_OFFSET_FILL);
			}
		else
			drawModel(mesh);
	}
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

	//initialize shaders
	shadersInit();

	//set initial normals
	mesh.request_vertex_normals();
	mesh.request_face_normals();

	//for the random numbers generator
	srand((unsigned)time(0));

	// Set the background color to black (a shocking surprise)  //
	glClearColor (0.0, 0.0, 0.0, 0.0);
	glShadeModel (GL_SMOOTH);

	// Set light parameters //
	glLightfv(GL_LIGHT0, GL_AMBIENT, light_ambient);
	glLightfv(GL_LIGHT0, GL_DIFFUSE, light_diffuse);
	glLightfv(GL_LIGHT0, GL_SPECULAR, light_specular);
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
	// Clear the screen buffer  //
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT) ;

	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();

	// and set the perspective with the current field of view, and the current height and width //
	gluPerspective(fov, 1.0, objDepth - 2*objRadius, objDepth + 2*objRadius);

	//draw light sphere with fixed functionality
	if (lastShaderRun != NULL)
		glUseProgram(0);

	//draws the lightSource
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
	if (drawType) {
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glutSolidSphere(0.07*objRadius, 16,16);
	}
	else glutWireSphere(0.07*objRadius, 16,16);

	//return to run with last shader
	if (lastShaderRun != NULL)
		glUseProgram(*lastShaderRun);

	//adding light according to light type
	addLightToScene();

	//draws the model
	glEnable(GL_LIGHTING);
	glColor3f(RED);
	glMatrixMode(GL_MODELVIEW) ;
	glLoadIdentity();
	glTranslatef(0.0,0.0, (-1)*objDepth);
	if (stateModelMove)	glMultMatrixf(currentTransform);
	glMultMatrixf(oldTransforms);

	//fixing the model to fit our screen
	glTranslatef(-center[0],-center[1],-center[2]);

	//updating normals
	mesh.update_normals();

	//drawing the model
	addModelToScene();

	//in case of LIGHTING enable closes the light source.otherwise doesn't change nothing
	glDisable(GL_LIGHTING);

	//draw 2D circl with fixed functionality
	if (lastShaderRun != NULL)
		glUseProgram(0);

	//drawing 2D circle
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	gluOrtho2D(-1.0,1.0,-1.0,1.0);
	glMatrixMode(GL_MODELVIEW) ;
	glLoadIdentity() ;
	circle->drawCircle();
	
	//return to run with last shader
	if (lastShaderRun != NULL)
		glUseProgram(*lastShaderRun);

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
		lightType=0;
		spotAngle = NO_SPOT_ANGLE;
		light_position[3] = 1.0;
		drawType = false;
		runWithShaders = false;
		lastShaderRun = NULL;
		glUseProgram(SHADERS_FIXED_FUNCTIONALITY);
		break;
	}

	case 'W':
	case 'w':
	{	
		drawType = !drawType;
		if (runWithShaders){
			glUseProgram(SHADERS_FIXED_FUNCTIONALITY);
			runWithShaders = false;
			lastShaderRun = NULL;
		}
		break;
	}

	case 'N':
	case 'n':
	{
		normalTypeSmooth = !normalTypeSmooth;
		break;
	}

	case 'A':
	case 'a':
	{
		stateModelMove = !stateModelMove;
		break;
	}

	case 'M':
	case 'm':
	{
		getRandomMaterial();
		setMaterial();
		break;
	}

	case 'h':
	case 'H':{
		hiddenSufaceRemovalEnable = !hiddenSufaceRemovalEnable;
		break;
	}

	case 'L':
	case 'l':
	{
		if (!runWithShaders){
		if (drawType){
		lightType = (lightType+1) % 3;
		if (lightType == 1) spotAngle = INITIAL_SPOT_ANGLE;
		}
		}
		break;
	}

	case '+':
	{
		if (lightType == 1){
		if (spotAngle < NO_SPOT_ANGLE/2)
			spotAngle++;
		}
		break;
	}

	case '-':{
		if (lightType == 1){
		if (spotAngle > INITIAL_SPOT_ANGLE)
			spotAngle--;
		}
		break;
	}
	case '0':{
		runWithShaders = false;
		lastShaderRun = NULL;
		glUseProgram(SHADERS_FIXED_FUNCTIONALITY);
		break;
		}

	case '1':{
		drawType = true;
		runWithShaders = true;
		lightType = 2;
		lastShaderRun = &phong_program_object;
		glUseProgram(phong_program_object);
		break;
	}
	case '2':{
		drawType = true;
		runWithShaders = true;
		lastShaderRun = &cell_program_object;
		lightType = 2;
		glUseProgram(cell_program_object);
		break;
		}
	case '3':{
		drawType = true;
		runWithShaders = true;
		lightType = 2;
		lastShaderRun = &procedural_program_object;
		glUseProgram(procedural_program_object);
		break;
		}
	 ///////////////////////////// TO DELETE /////////////////////////
     case '5':{
		drawType = true;
		runWithShaders = true;
		lightType = 2;
		lastShaderRun = &blinn_phong_program_object;
		glUseProgram(blinn_phong_program_object);
		break;
		}
	///////////////////////////////////////////////////////////////////

	}
	glutPostRedisplay();
	return;
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


//////////////////////////////
// OpenMesh functions       //
//////////////////////////////
/*
 * iterating over the faces in the model. Drawing every face as a polygon to the screen.
 * mesh - the mesh object of the model
 */
void drawModel(Mesh& mesh)
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

