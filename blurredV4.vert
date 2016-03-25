attribute vec2 position;
varying vec2 v_texCoord;
varying vec2 v_blurTexCoords[14];
 
void main()
{
    gl_Position = vec4(position, 0.0, 1.0);
    vec2 uv = position * 0.5 + 0.5;
    v_texCoord = uv;
    v_blurTexCoords[ 0] = uv + vec2(0.0, -0.028);
    v_blurTexCoords[ 1] = uv + vec2(0.0, -0.024);
    v_blurTexCoords[ 2] = uv + vec2(0.0, -0.020);
    v_blurTexCoords[ 3] = uv + vec2(0.0, -0.016);
    v_blurTexCoords[ 4] = uv + vec2(0.0, -0.012);
    v_blurTexCoords[ 5] = uv + vec2(0.0, -0.008);
    v_blurTexCoords[ 6] = uv + vec2(0.0, -0.004);
    v_blurTexCoords[ 7] = uv + vec2(0.0, 0.004);
    v_blurTexCoords[ 8] = uv + vec2(0.0, 0.008);
    v_blurTexCoords[ 9] = uv + vec2(0.0, 0.012);
    v_blurTexCoords[10] = uv + vec2(0.0, 0.016);
    v_blurTexCoords[11] = uv + vec2(0.0, 0.020);
    v_blurTexCoords[12] = uv + vec2(0.0, 0.024);
    v_blurTexCoords[13] = uv + vec2(0.0, 0.028);
}