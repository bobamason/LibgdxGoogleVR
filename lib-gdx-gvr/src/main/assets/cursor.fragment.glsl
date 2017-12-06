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
uniform vec4 u_color;
uniform float u_activation;
uniform float u_minRadius;
uniform float u_maxRadius;
varying vec2 v_texCoord;

void main(){
    gl_FragColor = u_color;
    float r = length(v_texCoord - vec2(0.5, 0.5)) * 2.0 * u_maxRadius;
    float max = mix(u_minRadius, u_maxRadius, u_activation);
    float min = max - u_minRadius * mix(1.0, 0.5, u_activation);
    if (r >= max)             	       
        discard;
    else if (r < min)             	       
        discard;
}