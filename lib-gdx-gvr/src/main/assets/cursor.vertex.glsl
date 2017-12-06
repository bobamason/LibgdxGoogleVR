attribute vec4 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
uniform float u_maxRadius;
varying vec2 v_texCoord;

void main() {
    mat4 scale = mat4(1.0);
    float s = u_maxRadius;
    scale[0][0] = s;
    scale[1][1] = s;
    scale[2][2] = s;
    vec4 pos = u_worldTrans * scale * a_position;
    v_texCoord = a_texCoord0;
	gl_Position = u_projTrans * pos;
}