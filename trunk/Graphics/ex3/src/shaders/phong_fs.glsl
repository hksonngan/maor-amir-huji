varying vec3 normal,vertexVector;
varying vec4 diffuse,ambientWithGlobal,specular;


void main(){
	

	vec3 normalizeNormal,normalizeHalfVector,lightVector,viewVector;
	float NdotL,NdotHN;
	vec4 fragColor = vec4(0.0,0.0,0.0,0.0);

	//normalizing the normal vector (after interpulation)
	normalizeNormal = normalize(normal);

	//normalizing the half vector (after interpulation)
	//normalizeHalfVector = normalize(halfVector);

	if(gl_LightSource[0].position[3]==0.0) //directional light
          lightVector = normalize(vec3(gl_LightSource[0].position) ); 
     else lightVector = normalize(vec3(gl_LightSource[0].position) - vertexVector ); //point light

	viewVector = normalize(-vertexVector);
	normalizeHalfVector = normalize(lightVector + viewVector);


	/* compute the cos of the angle between the normal and lights direction. 
    The light is directional so the direction is constant for every vertex.
    Since these two are normalized the cosine is the dot product. We also 
    need to clamp the result to the [0,1] range. */
    NdotL = max(dot(normalizeNormal, lightVector), 0.0);


	/* compute the diffuse and specular term if NdotL is  larger than zero */
    if (NdotL > 0.0)
    {
		//adding diffuse term to fragColor
		fragColor = diffuse * NdotL;

		//adding specular term to fragColor
		NdotHN = max(dot(normalizeHalfVector,normalizeNormal),0.0);
		fragColor += specular * pow(NdotHN,gl_FrontMaterial.shininess);
	}

	//adding ambient term to fragColor
	fragColor += ambientWithGlobal;

	gl_FragColor = fragColor;

}

