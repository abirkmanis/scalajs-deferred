#extension GL_EXT_shader_texture_lod : require
precision highp float;
uniform sampler2D texture;
uniform float lod;
varying vec2 vTexCoord;


void main(){
    gl_FragColor = texture2DLodEXT(texture, vTexCoord, lod);
}