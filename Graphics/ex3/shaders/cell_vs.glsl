//transfer the vertex normal to the fragment shader
varying vec3 normal;

void main(){	
	
	//normalize the normal to the eye space coordinates system
	normal = gl_NormalMatrix * gl_Normal;

	//set the position of the vertex
	gl_Position = ftransform();


}
