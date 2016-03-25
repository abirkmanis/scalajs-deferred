precision highp float;
varying vec3 vNormal;
varying float vZ;

void main(){
  gl_FragColor = vec4(vNormal,vZ+1.0);
}