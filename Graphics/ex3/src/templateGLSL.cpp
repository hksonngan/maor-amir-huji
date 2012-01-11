
#include <iostream>
#include <stdlib.h>
#include <math.h>
#include <stdio.h>     // Standard C/C++ IO  
#include "GLee.h"

#include <GL/gl.h>
#include <GL/glu.h>
#include <GL/glut.h>

GLuint   program_object22;  // a handler to the GLSL program used to update
GLuint   vertex_shader;   // a handler to vertex shader. This is used internally 
GLuint   fragment_shader; // a handler to fragment shader. This is used internally too


/// Print out the information log for a shader object 
/// @arg obj handle for a program object
static void printProgramInfoLog2(GLuint obj)
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
char *LoadShaderText2(const char *fileName)
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


// Our GL Specific Initializations
bool init(void){    
   glClearColor(0.0, 0.0, 0.0, 1.0);
   
	GLchar *vertex_source = LoadShaderText2("./shaders/blinn_vs.glsl");
	GLchar *fragment_source = LoadShaderText2("./shaders/blinn_fs.glsl");
	   
   program_object22  = glCreateProgram();    // creating a program object
   vertex_shader   = glCreateShader(GL_VERTEX_SHADER);   // creating a vertex shader object
   fragment_shader = glCreateShader(GL_FRAGMENT_SHADER); // creating a fragment shader object
   printProgramInfoLog2(program_object22);
   
   glShaderSource(vertex_shader, 1, (const GLchar**)&vertex_source, NULL); // assigning the vertex source
   glShaderSource(fragment_shader, 1, (const GLchar**)&fragment_source, NULL); // assigning the fragment source
   printProgramInfoLog2(program_object22);   // verifies if all this is ok so far
   
   // compiling and attaching the vertex shader onto program
   glCompileShader(vertex_shader);
   glAttachShader(program_object22, vertex_shader); 
   printProgramInfoLog2(program_object22);   // verifies if all this is ok so far
   
   // compiling and attaching the fragment shader onto program
   glCompileShader(fragment_shader);
   glAttachShader(program_object22, fragment_shader); 
   printProgramInfoLog2(program_object22);   // verifies if all this is ok so far
   
   // Link the shaders into a complete GLSL program.
   glLinkProgram(program_object22);
   printProgramInfoLog2(program_object22);   // verifies if all this is ok so far

   free(vertex_source);
   free(fragment_source);
    
// TODO: Works in Linux   
//   // some extra code for checking if all this initialization is ok
//   GLint prog_link_success;
//   glGetObjectParameterivARB(program_object22, GL_OBJECT_LINK_STATUS_ARB, &prog_link_success);
//   if (!prog_link_success) {
//      fprintf(stderr, "The shaders could not be linked\n");
//      exit(1);
//   }
   
	return true;
}

// Our rendering is done here
void render2(void)  {
	
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	// Clear Screen And Depth Buffer
	glLoadIdentity();									// Reset The Current Modelview Matrix
		
	gluLookAt(0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
	
	// bind the GLSL program	
	// this means that from here the GLSL program attends all OpenGL operations
	glUseProgram(program_object22);
	// painting a quad
	glBegin(GL_QUADS);
	   glVertex3f(-0.5, -0.5, 0.0);
	   glVertex3f(0.5, -0.5, 0.0);
	   glVertex3f(0.5, 0.5, 0.0);
	   glVertex3f(-0.5, 0.5, 0.0);
	glEnd();
	
	// unbind the GLSL program
	// this means that from here the OpenGL fixed functionality is used
	glUseProgram(0);
	
   // Swap The Buffers To Become Our Rendering Visible
   glutSwapBuffers( );
}

void reshape2(int w, int h){
	glViewport(0, 0, w, h);
	glMatrixMode(GL_PROJECTION);     
	glLoadIdentity();                
	if (h == 0) 
		h = 1;
	gluPerspective(45, (float)w/(float)h, 0.1, 100.0);
	glMatrixMode(GL_MODELVIEW);      
	glLoadIdentity();                
}

void keyboard2(unsigned char key, int x, int y){
	switch (key) {
		case 27:       // When escape is pressed...
			exit(0);    // Exit The Program
		   break;      
		default:       
		break;
	}
}


int main2(int argc, char** argv){	
	glutInit(&argc, argv);                          
	glutInitDisplayMode(GLUT_RGBA | GLUT_DOUBLE | GLUT_DEPTH);    
	glutCreateWindow("GLSL Hello World!");        	
	init();                                          
	glutDisplayFunc(render2);                         
	glutReshapeFunc(reshape2);                        
	glutKeyboardFunc(keyboard2);                     
	glutMainLoop();                                  
	return 0;
}
