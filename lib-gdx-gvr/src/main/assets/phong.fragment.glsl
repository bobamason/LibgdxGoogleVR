#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

varying vec4 v_position;

#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#if defined(specularFlag) || defined(fogFlag)
#define cameraPositionFlag
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#ifdef normalFlag
varying vec3 v_normal;
#endif //normalFlag

#if defined(colorFlag)
varying vec4 v_color;
#endif

#ifdef blendedFlag
varying float v_opacity;
#ifdef alphaTestFlag
varying float v_alphaTest;
#endif //alphaTestFlag
#endif //blendedFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

#ifdef diffuseTextureFlag
varying MED vec2 v_diffuseUV;
#endif

#ifdef specularTextureFlag
varying MED vec2 v_specularUV;
#endif

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef ambientColorFlag
uniform vec4 u_ambientColor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef specularColorFlag
uniform vec4 u_specularColor;
#endif

#ifdef specularTextureFlag
uniform sampler2D u_specularTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
#endif

#ifdef lightingFlag
vec3 v_lightDiffuse;

#ifdef ambientLightFlag
uniform vec3 u_ambientLight;
#endif // ambientLightFlag

#ifdef ambientCubemapFlag
uniform vec3 u_ambientCubemap[6];
#endif // ambientCubemapFlag 

#ifdef sphericalHarmonicsFlag
uniform vec3 u_sphericalHarmonics[9];
#endif //sphericalHarmonicsFlag

#ifdef specularFlag
vec3 v_lightSpecular;
#endif // specularFlag

#ifdef shininessFlag
uniform float u_shininess;
#else
const float u_shininess = 20.0;
#endif // shininessFlag

#ifdef cameraPositionFlag
uniform vec4 u_cameraPosition;
#endif // cameraPositionFlag

#ifdef fogFlag
float v_fog;
#endif // fogFlag


#if numDirectionalLights > 0
struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif // numDirectionalLights

#if numPointLights > 0
struct PointLight
{
	vec3 color;
	vec3 position;
};
uniform PointLight u_pointLights[numPointLights];
#endif // numPointLights

#if	defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif //ambientFlag

#if defined(ambientFlag) && defined(separateAmbientFlag)
vec3 v_ambientLight;
#endif //separateAmbientFlag

#ifdef shadowMapFlag
uniform sampler2D u_shadowTexture;
uniform float u_shadowPCFOffset;
varying vec3 v_shadowMapUv;
#define separateAmbientFlag

float getShadowness(vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));//+(1.0/255.0));	
}

float getShadow() 
{
	return (//getShadowness(vec2(0,0)) + 
			getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
			getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
			getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
			getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.25;
}
#endif //shadowMapFlag

#if defined(ambientFlag) && defined(separateAmbientFlag)
varying vec3 v_ambientLight;
#endif //separateAmbientFlag

#endif //lightingFlag

#ifdef fogFlag
uniform vec4 u_fogColor;
varying float v_fog;
#endif // fogFlag

void main() {
	#if defined(normalFlag) 
		vec3 normal = v_normal;
	#endif // normalFlag
	
	vec4 pos = v_position;

////////////--------------start lighting--------------------

    #ifdef fogFlag
        vec3 flen = u_cameraPosition.xyz - pos.xyz;
        float fog = dot(flen, flen) * u_cameraPosition.w;
        v_fog = min(fog, 1.0);
    #endif

	#ifdef lightingFlag
		#if	defined(ambientLightFlag)
        	vec3 ambientLight = u_ambientLight;
		#elif defined(ambientFlag)
        	vec3 ambientLight = vec3(0.0);
		#endif
		
		#ifdef ambientColorFlag
        	ambientLight *= u_ambientColor.rgb;
		#endif
			
		#ifdef ambientCubemapFlag 		
			vec3 squaredNormal = normal * normal;
			vec3 isPositive  = step(0.0, normal);
			ambientLight += squaredNormal.x * mix(u_ambientCubemap[0], u_ambientCubemap[1], isPositive.x) +
					squaredNormal.y * mix(u_ambientCubemap[2], u_ambientCubemap[3], isPositive.y) +
					squaredNormal.z * mix(u_ambientCubemap[4], u_ambientCubemap[5], isPositive.z);
		#endif // ambientCubemapFlag

		#ifdef sphericalHarmonicsFlag
			ambientLight += u_sphericalHarmonics[0];
			ambientLight += u_sphericalHarmonics[1] * normal.x;
			ambientLight += u_sphericalHarmonics[2] * normal.y;
			ambientLight += u_sphericalHarmonics[3] * normal.z;
			ambientLight += u_sphericalHarmonics[4] * (normal.x * normal.z);
			ambientLight += u_sphericalHarmonics[5] * (normal.z * normal.y);
			ambientLight += u_sphericalHarmonics[6] * (normal.y * normal.x);
			ambientLight += u_sphericalHarmonics[7] * (3.0 * normal.z * normal.z - 1.0);
			ambientLight += u_sphericalHarmonics[8] * (normal.x * normal.x - normal.y * normal.y);			
		#endif // sphericalHarmonicsFlag

		#ifdef ambientFlag
			#ifdef separateAmbientFlag
				v_ambientLight = ambientLight;
				v_lightDiffuse = vec3(0.0);
			#else
				v_lightDiffuse = ambientLight;
			#endif //separateAmbientFlag
		#else
	        v_lightDiffuse = vec3(0.0);
		#endif //ambientFlag

			
		#ifdef specularFlag
			v_lightSpecular = vec3(0.0);
			vec3 viewVec = normalize(u_cameraPosition.xyz - pos.xyz);
		#endif // specularFlag
			
		#if (numDirectionalLights > 0) && defined(normalFlag)
			for (int i = 0; i < numDirectionalLights; i++) {
				vec3 lightDir = -u_dirLights[i].direction;
				float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
				vec3 value = u_dirLights[i].color * NdotL;
				v_lightDiffuse += value;
				#ifdef specularFlag
					float halfDotView = max(0.0, dot(normal, normalize(lightDir + viewVec)));
					v_lightSpecular += value * pow(halfDotView, u_shininess);
				#endif // specularFlag
			}
		#endif // numDirectionalLights

		#if (numPointLights > 0) && defined(normalFlag)
			for (int i = 0; i < numPointLights; i++) {
				vec3 lightDir = u_pointLights[i].position - pos.xyz;
				float dist2 = dot(lightDir, lightDir);
				lightDir *= inversesqrt(dist2);
				float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
				vec3 value = u_pointLights[i].color * (NdotL / (1.0 + dist2));
				v_lightDiffuse += value;
				#ifdef specularFlag
					float halfDotView = max(0.0, dot(normal, normalize(lightDir + viewVec)));
					v_lightSpecular += value * pow(halfDotView, u_shininess);
				#endif // specularFlag
			}
		#endif // numPointLights
	#endif // lightingFlag

////////////---------------end lighting--------------------
		
	#if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor * v_color;
	#elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor;
	#elif defined(diffuseTextureFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * v_color;
	#elif defined(diffuseTextureFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV);
	#elif defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = u_diffuseColor * v_color;
	#elif defined(diffuseColorFlag)
		vec4 diffuse = u_diffuseColor;
	#elif defined(colorFlag)
		vec4 diffuse = v_color;
	#else
		vec4 diffuse = vec4(1.0);
	#endif

	#if (!defined(lightingFlag))  
    		gl_FragColor.rgb = diffuse.rgb;
    	#elif (!defined(specularFlag))
    		#if defined(ambientFlag) && defined(separateAmbientFlag)
    			#ifdef shadowMapFlag
    				gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + getShadow() * v_lightDiffuse));
    				//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
    			#else
    				gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + v_lightDiffuse));
    			#endif //shadowMapFlag
    		#else
    			#ifdef shadowMapFlag
    				gl_FragColor.rgb = getShadow() * (diffuse.rgb * v_lightDiffuse);
    			#else
    				gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse);
    			#endif //shadowMapFlag
    		#endif
    	#else
    		#if defined(specularTextureFlag) && defined(specularColorFlag)
    			vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * u_specularColor.rgb * v_lightSpecular;
    		#elif defined(specularTextureFlag)
    			vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * v_lightSpecular;
    		#elif defined(specularColorFlag)
    			vec3 specular = u_specularColor.rgb * v_lightSpecular;
    		#else
    			vec3 specular = v_lightSpecular;
    		#endif
    			
    		#if defined(ambientFlag) && defined(separateAmbientFlag)
    			#ifdef shadowMapFlag
    			gl_FragColor.rgb = (diffuse.rgb * (getShadow() * v_lightDiffuse + v_ambientLight)) + specular;
    				//gl_FragColor.rgb = texture2D(u_shadowTexture, v_shadowMapUv.xy);
    			#else
    				gl_FragColor.rgb = (diffuse.rgb * (v_lightDiffuse + v_ambientLight)) + specular;
    			#endif //shadowMapFlag
    		#else
    			#ifdef shadowMapFlag
    				gl_FragColor.rgb = getShadow() * ((diffuse.rgb * v_lightDiffuse) + specular);
    			#else
    				gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse) + specular;
    			#endif //shadowMapFlag
    		#endif
    	#endif //lightingFlag

	#ifdef fogFlag
		gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
	#endif // end fogFlag

	#ifdef blendedFlag
		gl_FragColor.a = diffuse.a * v_opacity;
		#ifdef alphaTestFlag
			if (gl_FragColor.a <= v_alphaTest)
				discard;
		#endif
	#else
		gl_FragColor.a = 1.0;
	#endif

}