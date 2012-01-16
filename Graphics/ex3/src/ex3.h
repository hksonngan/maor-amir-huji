/********************************************************************
*  Author  :  
* ID      : 
* Course  : Computer Graphics
* Project : Ex3
*
* Purpose : 
*
\********************************************************************/

#ifndef __EX3_H__
#define __EX3_H__


//////////////////////////////
// Project Includes         //
//////////////////////////////

#include <iostream>
#include <stdlib.h>
#include <ctype.h>
#include <math.h>
using namespace std;


#include "openMeshIncludes.h"

//////////////////////////////
// GL Includes              //
//////////////////////////////
#include "GLee.h"
#include <GL/glut.h>
#include <GL/gl.h>
#include <GL/glu.h>

#include "circle.h"
#include "arcBall.h"
#include "shaderFunctions.h"
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
// Shaders Definitions      //
//////////////////////////////
#define PHONG_VS "./shaders/phong_vs.glsl"
#define PHONG_FS "./shaders/phong_fs.glsl"
#define CELL_VS "./shaders/cell_vs.glsl"
#define CELL_FS "./shaders/cell_fs.glsl"
#define PROCEDURAL_VS "./shaders/procedural_vs.glsl"
#define PROCEDURAL_FS "./shaders/procedural_fs.glsl"
#define SHADERS_FIXED_FUNCTIONALITY 0


//////////////////////////////
// Shader variables        //
//////////////////////////////
GLuint   phong_program_object;  // a handler to the GLSL program used to update
GLuint   phong_vertex_shader;   // a handler to vertex shader of the phong shading ('1')
GLuint   phong_fragment_shader; // a handler to fragment shader of the phong shading ('1')

GLuint   cell_program_object;
GLuint   cell_vertex_shader;   // a handler to vertex shader of the cell shading ('2')
GLuint   cell_fragment_shader; // a handler to fragment shader of the cell shading ('2')

GLuint   procedural_program_object;
GLuint   procedural_vertex_shader;   // a handler to vertex shader of the procedural shading ('3')
GLuint   procedural_fragment_shader; // a handler to fragment shader of the procedural shading ('3')
GLint    objRadiusInShader;          // a handler to the uniform variable in the shader saves the object radius

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

// shader init //
void shadersInit(void);

//catmull-clark subdivision function
Mesh* subDivitionWithCutmullClark(Mesh* mesh);

#endif

