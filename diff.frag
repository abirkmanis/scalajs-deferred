#extension GL_OES_standard_derivatives : enable
precision highp float;
uniform sampler2D normalTexture;
varying vec2 vTexCoord;

void main(){
    float dx = dFdx(vTexCoord.x);
    float dy = dFdy(vTexCoord.y);
    float ddzdx = 2.0*texture2D(normalTexture,vTexCoord).a-texture2D(normalTexture,vTexCoord-vec2(dx,0)).a-texture2D(normalTexture,vTexCoord+vec2(dx,0)).a;
    float ddzdy = 2.0*texture2D(normalTexture,vTexCoord).a-texture2D(normalTexture,vTexCoord-vec2(0,dy)).a-texture2D(normalTexture,vTexCoord+vec2(0,dy)).a;
    gl_FragColor = vec4(100.0*(ddzdx*ddzdx+ddzdy*ddzdy),0,0,1);
}