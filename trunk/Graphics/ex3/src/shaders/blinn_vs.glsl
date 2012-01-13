void main()
{
    vec3 normal, lightVector, viewVector, halfVector;
    vec4 diffuse, ambient, globalAmbient, specular = vec4(0.0);
    float NdotL,NdotHN;
    
    /* first transform the normal into eye space and normalize the result */
    normal = normalize(gl_NormalMatrix * gl_Normal);
	
    /* now normalize the light's direction. Note that according to the
    OpenGL specification, the light is stored in eye space. Also since 
    we're talking about a directional light, the position field is actually 
    direction */
    lightVector = normalize(vec3(gl_LightSource[0].position - gl_ModelViewMatrix*gl_Vertex));

    /* compute the cos of the angle between the normal and lights direction. 
    The light is directional so the direction is constant for every vertex.
    Since these two are normalized the cosine is the dot product. We also 
    need to clamp the result to the [0,1] range. */
    NdotL = max(dot(normal, lightVector), 0.0);
    
    /* Compute the diffuse, ambient and globalAmbient terms */
	diffuse = gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse;
    ambient = gl_FrontMaterial.ambient * gl_LightSource[0].ambient;
    globalAmbient = gl_LightModel.ambient * gl_FrontMaterial.ambient;
    
    viewVector = -normalize(vec3(gl_ModelViewMatrix*gl_Vertex));
	halfVector = normalize(lightVector + viewVector);
	
    /* compute the specular term if NdotL is  larger than zero */
    if (NdotL > 0.0)
    {
		NdotHN = max(dot(halfVector,normal),0.0);

		// to use the halfway vector
        specular = gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHN,gl_FrontMaterial.shininess);
    }
    
    gl_FrontColor = globalAmbient + NdotL*diffuse + ambient + specular;
    gl_Position = ftransform();
}