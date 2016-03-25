uniform mat4 sunMatrix;
attribute vec3 position;
varying float vZ;

void main(){
  gl_Position = sunMatrix * vec4(position, 1);
  vZ = gl_Position.z * 0.5 + 0.5;
}
