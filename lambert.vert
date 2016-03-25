uniform mat4 pvMatrix;
uniform mat4 sunMatrix;
uniform vec3 sunDirection;
attribute vec3 position;
attribute vec3 normal;
varying float vDot;
varying vec3 vSunPosition;

void main(){
  gl_Position = pvMatrix * vec4(position, 1);
  vec4 sp = sunMatrix * vec4(position, 1);
  vSunPosition = sp.xyz * 0.5 + 0.5;
  vDot = clamp(dot(normal, sunDirection), 0.0, 1.0);
}
