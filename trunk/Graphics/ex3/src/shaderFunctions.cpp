
#include "shaderFunctions.h"


/// Print out the information log for a shader object
/// @arg obj handle for a program object
void printProgramInfoLog(GLuint obj)
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
	size_t readNum;

	fp = fopen(fileName, "r");
	if (fp != NULL){
		while (fgetc(fp) != EOF){
			shaderLength++;
		}
		rewind(fp);
		shaderText = (GLchar *)malloc(shaderLength+1);
		if (shaderText != NULL){
			readNum = fread(shaderText, 1, shaderLength, fp);
			if (readNum == 0)
				return shaderText;
		}
		shaderText[shaderLength] = '\0';
		fclose(fp);
	}
	return shaderText;
}

/*
 * used to initialize a program running a vertex and fragment shader based on the given pointer to program object,
 * shader objects and names.
 */
void setupShaders(GLuint* program_object,GLuint* vertex_shader,GLuint* fragment_shader,const char* vshader_name,const char* fshader_name){

	*program_object = glCreateProgram();    // creating a program object

	GLchar *vertex_source;
	GLchar *fragment_source;

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



