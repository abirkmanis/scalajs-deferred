precision highp float;
uniform sampler2D normalTexture;
uniform sampler2D depthTexture;
varying vec2 vTexCoord;

void main(){
    //gl_FragColor = texture2D(depthTexture,vTexCoord);
    gl_FragColor = max(texture2D(normalTexture, vTexCoord), texture2D(depthTexture, vTexCoord));
}