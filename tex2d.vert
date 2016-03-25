attribute vec2 position;
varying vec2 vTexCoord;

void main(){
    gl_Position = vec4(position, 0.0, 1.0);
    vTexCoord = position * 0.5 + 0.5;
}
