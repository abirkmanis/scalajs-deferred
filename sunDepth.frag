precision highp float;
varying float vZ;

void main(){
  gl_FragColor = vec4(vZ, vZ*vZ, 1.0, 1.0);
}