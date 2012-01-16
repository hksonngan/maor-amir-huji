
varying vec3 normal,vertexVector;

void main(){
	
	float normLightDot;
	vec4 fragColor;
	
	vec3 lightVector;

	vec4 colors[5]; 
	colors[0] = vec4(1.0,0.5,0.5,1.0);
	colors[1] = vec4(0.6,0.3,0.3,1.0);
	colors[2] = vec4(0.4,0.2,0.2,1.0);
	colors[3] = vec4(0.2,0.2,0.1,1.0);
	colors[4] =	vec4(0.1,0.05,0.05,1.0);

	//normalizing normal vector after interpolation
	vec3 norm = normalize(normal);
	
	//calculating light vector (L)
	if(gl_LightSource[0].position[3]==0.0) //directional light
          lightVector = normalize(vec3(gl_LightSource[0].position) ); 
     else lightVector = normalize(vec3(gl_LightSource[0].position) - vertexVector ); //point light
	
	//calculating the dot product.(A bigger result means smaller angle)
	if (gl_FrontFacing)
		normLightDot = dot(norm,lightVector);
	else normLightDot = dot(-norm,lightVector);
	//choosing the based on the dot product
	if (normLightDot > 0.8)
		fragColor = colors[0];
	else if (normLightDot > 0.6)
			fragColor = colors[1];
		 else if (normLightDot > 0.4)
				fragColor = colors[2];
			   else if (normLightDot > 0.2)
						fragColor = colors[3];
					else fragColor = colors[4];

	gl_FragColor = fragColor;

}
