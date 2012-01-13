varying vec3 normal,vertexVector;
varying vec4 diffuse,ambientWithGlobal,specular;


void main(){
	

	vec3 normalizedNormal,reflectionVector,lightVector,eyeVector;
	float NdotL,NdotRE;
	vec4 fragColor = vec4(0.0,0.0,0.0,0.0);


	//normalizing the normal vector (after interpulation) (N)
	normalizedNormal = normalize(normal);

	//calclulation eye vector (E)
	eyeVector = normalize(-vertexVector);

	//calculating light vector (L)
	if(gl_LightSource[0].position[3]==0.0) //directional light
          lightVector = normalize(vec3(gl_LightSource[0].position) ); 
     else lightVector = normalize(vec3(gl_LightSource[0].position) - vertexVector ); //point light
       

	
	/* compute the cos of the angle between the normal and lights direction. 
    The light is directional so the direction is constant for every vertex.
    Since these two are normalized the cosine is the dot product. We also 
    need to clamp the result to the [0,1] range. */
    NdotL = max(dot(normalizedNormal, lightVector), 0.0);


	//computing reflection vector (R)
	reflectionVector = normalize(-reflect(lightVector,normalizedNormal));


	/* compute the diffuse and specular term if NdotL is  larger than zero */
    if (NdotL > 0.0)
    {
		//adding diffuse term to fragColor
		fragColor = diffuse * NdotL;

		//adding specular term to fragColor
		NdotRE = max(dot(reflectionVector,eyeVector),0.0);
		fragColor += specular * pow(NdotRE,gl_FrontMaterial.shininess);
	}

	//adding ambient term to fragColor
	fragColor += ambientWithGlobal;

	//updating the color
	gl_FragColor = fragColor;

}

