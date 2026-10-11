#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_grassTexCoord;
varying vec2 v_sandTexCoord;
varying float v_blend;

uniform sampler2D u_grassTexture;
uniform sampler2D u_sandTexture;

void main() {
    vec4 grass = texture2D(u_grassTexture, v_grassTexCoord);
    vec4 sand = texture2D(u_sandTexture, v_sandTexCoord);

    gl_FragColor = mix(grass, sand, clamp(v_blend, 0.0, 1.0));
}
