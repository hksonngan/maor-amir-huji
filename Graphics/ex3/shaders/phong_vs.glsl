varying vec3 normal,vertexVector;
varying vec4 diffuse,ambientWithGlobal,specular;


void main()
{
    vec3 viewVector;
    
    /* first transform the normal into eye space and normalize the result */
    normal = normalize(gl_NormalMatrix * gl_Normal);
	
    /* now normalize the light's direction. Note that according to the
    OpenGL specification, the light is stored in eye space. Also since 
    we're talking about a directional light, the position field is actually 
    direction */
   // lightVector = normalize(vec3(gl_LightSource[0].position - gl_ModelViewMatrix*gl_Vertex));
    
    
    /* Compute the diffuse,specular and ambient and globalAmbient terms */
	diffuse = gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse;
    ambientWithGlobal = gl_FrontMaterial.ambient * gl_LightSource[0].ambient;
    ambientWithGlobal += gl_LightModel.ambient * gl_FrontMaterial.ambient;
	specular = gl_FrontMaterial.specular * gl_LightSource[0].specular;
    
	/* normalize the halfVector to send it to the fragment shader */
    //viewVector = -normalize(vec3(gl_ModelViewMatrix*gl_Vertex));
	//halfVector = normalize(lightVector + viewVector);
	
	vertexVector = vec3(gl_ModelViewMatrix*gl_Vertex);


    gl_Position = ftransform();
}