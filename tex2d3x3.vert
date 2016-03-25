attribute vec2 position;

uniform highp float imageWidthFactor;
uniform highp float imageHeightFactor;

varying vec2 textureCoordinate;
varying vec2 leftTextureCoordinate;
varying vec2 rightTextureCoordinate;

varying vec2 topTextureCoordinate;
varying vec2 topLeftTextureCoordinate;
varying vec2 topRightTextureCoordinate;

varying vec2 bottomTextureCoordinate;
varying vec2 bottomLeftTextureCoordinate;
varying vec2 bottomRightTextureCoordinate;

void main()
{
    gl_Position = vec4(position, 0.0, 1.0);
    
    vec2 widthStep = vec2(imageWidthFactor, 0.0);
    vec2 heightStep = vec2(0.0, imageHeightFactor);
    vec2 widthHeightStep = vec2(imageWidthFactor, imageHeightFactor);
    vec2 widthNegativeHeightStep = vec2(imageWidthFactor, -imageHeightFactor);
    
    vec2 uv = position * 0.5 + 0.5;
    
    textureCoordinate = uv;
    leftTextureCoordinate = uv - widthStep;
    rightTextureCoordinate = uv + widthStep;
    
    topTextureCoordinate = uv + heightStep;
    topLeftTextureCoordinate = uv - widthNegativeHeightStep;
    topRightTextureCoordinate = uv + widthHeightStep;
    
    bottomTextureCoordinate = uv - heightStep;
    bottomLeftTextureCoordinate = uv - widthHeightStep;
    bottomRightTextureCoordinate = uv + widthNegativeHeightStep;
}