varying vec3 normal,vertexVector;
varying vec4 diffuse,ambientWithGlobal,specular;


void main()
{
    
    /* first transform the normal into eye space and normalize the result */
    normal = normalize(gl_NormalMatrix * gl_Normal);
	
       
    /* Compute the diffuse,specular and ambient and globalAmbient terms */
	diffuse = gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse;
    ambientWithGlobal = gl_FrontMaterial.ambient * gl_LightSource[0].ambient;
    ambientWithGlobal += gl_LightModel.ambient * gl_FrontMaterial.ambient;
	specular = gl_FrontMaterial.specular * gl_LightSource[0].specular;
    
	/* Computing the vertex vector for the fragment shader */
    vertexVector = vec3(gl_ModelViewMatrix*gl_Vertex);
	
    gl_Position = ftransform();
}