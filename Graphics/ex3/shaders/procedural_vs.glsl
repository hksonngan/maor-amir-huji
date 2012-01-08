varying vec3 normal;
varying vec4 vertex;


void main(){	

	/* first transform the normal into eye space and normalize the result */
    normal = normalize(gl_NormalMatrix * gl_Normal);

	/* Compute the diffuse and specular terms */
	//diffuse = gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse;
	//specular = gl_FrontMaterial.specular * gl_LightSource[0].specular;

	/* send vertex position */
	vertex = gl_Vertex;

	gl_Position = ftransform();

	

}
