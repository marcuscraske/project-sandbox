precision mediump float;
varying vec2 vTextureCoord;
varying vec4 vColour;
varying vec3 vAmbientLighting;
varying vec3 vNormals;
varying vec4 vPosition;
varying vec4 vWorldPosition;
uniform sampler2D uSampler;

void main(void)
{
	vec4 texel = texture2D(uSampler, vec2(vTextureCoord.s, vTextureCoord.t));
	texel *= vColour;

	// Check if to discard texel due to alpha
	if (texel.a < 0.05)
	{
		discard;
	}

	// Light properties
	bool lightOn = true;
	float lightDistance = 400.0;
	vec3 lightColour = vec3(1.0, 1.0, 1.0);
	vec3 lightPos = vec3(0.0, 0.0, 370.0);
	float lightAngle = radians(0.0);
	float lightConeAngle = radians(65.0);

	// Light properties - attenuation
	float constantAttenuation = 0.2;
	float linearAttenuation = 0.01;
	float quadraticAttenuation = 0.000001;

	// Calculations for light
	vec3 lightRelativePosition = normalize(lightPos - vPosition.xyz);
	vec3 lightTarget = vec3(sin(lightAngle), cos(lightAngle), 370.0);
	vec3 lightDir = normalize(lightTarget - lightPos);
	float angle = acos(dot(-lightRelativePosition, lightDir));
	float l = dot(vNormals, lightRelativePosition);
    float distance = distance(vPosition.xyz, lightPos);

    // The colour of the texel from the light - can be used between multiple lights for additive colour
	vec3 additiveLightColour = vec3(1.0, 1.0, 1.0);

	if (lightOn && angle < lightConeAngle && l >= 0.0 && distance <= lightDistance)
	{
		float attenuatedLight = 1.0 / (
            constantAttenuation +
            linearAttenuation*distance +
            quadraticAttenuation*distance*distance
		);
        additiveLightColour += l * lightColour * attenuatedLight;
	}

	gl_FragColor = clamp(vec4(texel.rgb * vAmbientLighting * additiveLightColour, texel.a), 0.0, 1.0);
}
