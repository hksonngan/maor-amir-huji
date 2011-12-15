/********************************************************************
*  Author  :  
* ID      : 
* Course  : Computer Graphics
* Project : Ex2
*
* Purpose : 
*
\********************************************************************/

#ifndef __EX2_H__
#define __EX2_H__


//////////////////////////////
// Project Includes         //
//////////////////////////////

#include <stdlib.h>
#include <ctype.h>
#include <math.h>
using namespace std;

//////////////////////////////
// GL Includes              //
//////////////////////////////

#include <GL/gl.h>
#include <GL/glu.h>
#include <GL/glut.h>


//////////////////////////////
// OpenMesh Includes        //
//////////////////////////////

#include "OpenMesh/Core/IO/MeshIO.hh"
#include "OpenMesh/Core/Mesh/PolyMesh_ArrayKernelT.hh"


//////////////////////////////
// Defines                  //
//////////////////////////////

#define RC_OK                 (0) // Everything went ok                        //
#define RC_INVALID_ARGUMENTS  (1) // Invalid arguments given to the program    //
#define RC_INPUT_ERROR        (2) // Invalid input to the program              //


#define  ARGUMENTS_PROGRAM     (0) // program name position on argv            //
#define  ARGUMENTS_INPUTFILE   (1) // given input file position on argv        //
#define  ARGUMENTS_REQUIRED    (2) // number of required arguments             //

#define  WINDOW_SIZE         (600) // initial size of the window               //
#define  WINDOW_POS_X        (100) // initial X position of the window         //
#define  WINDOW_POS_Y        (100) // initial Y position of the window         //

#define INITIAL_FOV        30.0
#define DEPTH2RADIUS_RATIO 4.52
#define INITIAL_SPOT_ANGLE 10.0
#define NO_SPOT_ANGLE 180.0


//////////////////////////////
// Color Definitions        //
//////////////////////////////
#define GREEN 0.0,1.0,0.0
#define RED 1.0,0.0,0.0
#define WHITE 1.0,1.0,1.0
#define BLACK 0.0,0.0,0.0



//////////////////////////////
// Mouse Definitions        //
//////////////////////////////
#define LEFT_BUTTON 0
#define MIDDLE_BUTTON 1
#define RIGHT_BUTTON 2
#define KEY_PRESSED 0
#define KEY_REALESED 1

//////////////////////////////
// Key Definitions      //
//////////////////////////////
#define KEY_UPPER_QUIT      ('Q') // Keys used to terminate the program         //
#define KEY_QUIT            ('q')
#define KEY_UPPER_RESET     ('R') // Keys used to reset the applied TX's        //
#define KEY_RESET           ('r')


//////////////////////////////
// Type Definitions         //
//////////////////////////////

// Mesh object  //
typedef OpenMesh::PolyMesh_ArrayKernelT<> Mesh;

// Vector of 3 floats  //
typedef OpenMesh::VectorT<float, 3> Vector3F;


//////////////////////////////
// Functions declarations   //
//////////////////////////////

// initialize openGL settings //
void initGL(void);

// display callback //
void display(void);

// window reshape callback  //
void reshape(int width, int height);

// keyboard callback  //
void keyboard(unsigned char key, int x, int y);

// mouse click callback //
void mouse(int button, int state, int x, int y) ;

// mouse dragging callback  //
void motion(int x, int y) ;

// draws the model //
void drawModel(Mesh& mesh);
void drawSolid(Mesh& mesh);

/////////////////////////////
// OpenMesh usage examples //
/////////////////////////////
void faceCenter(Mesh & mesh);
void computeCenterAndBoundingBox(Mesh& mesh);
void faceValenceCounter(Mesh& mesh);
void edgesLengths(Mesh& mesh);
void faceCenter(Mesh & mesh);
void vectorDemo();

#endif

