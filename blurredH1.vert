attribute vec2 position;
varying vec2 v_texCoord;
varying vec2 v_blurTexCoords[14];
 
void main()
{
    gl_Position = vec4(position, 0.0, 1.0);
    vec2 uv = position * 0.5 + 0.5;
    v_texCoord = uv;
    v_blurTexCoords[ 0] = uv + vec2(-0.007, 0.0);
    v_blurTexCoords[ 1] = uv + vec2(-0.006, 0.0);
    v_blurTexCoords[ 2] = uv + vec2(-0.005, 0.0);
    v_blurTexCoords[ 3] = uv + vec2(-0.004, 0.0);
    v_blurTexCoords[ 4] = uv + vec2(-0.003, 0.0);
    v_blurTexCoords[ 5] = uv + vec2(-0.002, 0.0);
    v_blurTexCoords[ 6] = uv + vec2(-0.001, 0.0);
    v_blurTexCoords[ 7] = uv + vec2( 0.001, 0.0);
    v_blurTexCoords[ 8] = uv + vec2( 0.002, 0.0);
    v_blurTexCoords[ 9] = uv + vec2( 0.003, 0.0);
    v_blurTexCoords[10] = uv + vec2( 0.004, 0.0);
    v_blurTexCoords[11] = uv + vec2( 0.005, 0.0);
    v_blurTexCoords[12] = uv + vec2( 0.006, 0.0);
    v_blurTexCoords[13] = uv + vec2( 0.007, 0.0);
}