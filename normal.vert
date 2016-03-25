uniform mat4 pvMatrix;
attribute vec3 position;
attribute vec3 normal;
varying vec3 vNormal;
varying float vZ;

void main(){
  gl_Position = pvMatrix * vec4(position, 1);
  vNormal = normal * 0.5 + 0.5;
  vZ = gl_Position.z;///gl_Position.w;
}
