varying vec3 normal;
varying vec4 vertex;

uniform float objRadius;

void main(){

	vec4 diffuse,specular;
	vec3 normalizedNormal,reflectionVector,lightVector,eyeVector,vertexVector;
	vec4 fragColor = vec4(0.0,0.0,0.0,0.0);
	float NdotL,NdotRE;
	

	/* set the checkerboard colors */
	if (mod(floor(4.0*vertex.x/objRadius) + floor(4.0*vertex.y/objRadius) + floor(2.0*vertex.z/objRadius),2.0) < 1.0)
	{	
		diffuse = vec4(0.0,0.0,0.0,1.0);
		}
	else{ 
	if (gl_FrontFacing)
		diffuse = vec4(1.0,1.0,1.0,1.0);
	else    diffuse = vec4(0.0,0.5,0.5,1.0);

	}



	/* Compute the diffuse and specular terms */
	diffuse = diffuse * gl_LightSource[0].diffuse;
	specular = gl_FrontMaterial.specular * gl_LightSource[0].specular;

	//move vertex coordinates to eye space coordinates
	vertexVector = vec3(gl_ModelViewMatrix*vertex);

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
    if (gl_FrontFacing)
	    NdotL = max(dot(normalizedNormal, lightVector), 0.0);
    else  NdotL = max(dot(-normalizedNormal, lightVector), 0.0);


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

	//updating the color
	gl_FragColor = fragColor;





}
