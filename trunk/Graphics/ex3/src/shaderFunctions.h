#ifndef __SHADERFUNCTIONS_H__
#define __SHADERFUNCTIONS_H__

#include <iostream>
#include <stdio.h>
#include <stdlib.h>
using namespace std;

#include "GLee.h"
#include <GL/glut.h>
#include <GL/gl.h>
#include <GL/glu.h>


// shaders reletad functions
char *LoadShaderText(const char *fileName);
void printProgramInfoLog(GLuint obj);
void setupShaders(GLuint* program_object,GLuint* vertex_shader,GLuint* fragment_shader,const char* vshader_name,const char* fshader_name);


#endif
