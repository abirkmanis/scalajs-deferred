 precision highp float;

 varying vec2 textureCoordinate;
 varying vec2 leftTextureCoordinate;
 varying vec2 rightTextureCoordinate;

 varying vec2 topTextureCoordinate;
 varying vec2 topLeftTextureCoordinate;
 varying vec2 topRightTextureCoordinate;

 varying vec2 bottomTextureCoordinate;
 varying vec2 bottomLeftTextureCoordinate;
 varying vec2 bottomRightTextureCoordinate;

 uniform sampler2D texture;

 void main()
 {
    float i00   = texture2D(texture, textureCoordinate).a;
    float im1m1 = texture2D(texture, bottomLeftTextureCoordinate).a;
    float ip1p1 = texture2D(texture, topRightTextureCoordinate).a;
    float im1p1 = texture2D(texture, topLeftTextureCoordinate).a;
    float ip1m1 = texture2D(texture, bottomRightTextureCoordinate).a;
    float im10 = texture2D(texture, leftTextureCoordinate).a;
    float ip10 = texture2D(texture, rightTextureCoordinate).a;
    float i0m1 = texture2D(texture, bottomTextureCoordinate).a;
    float i0p1 = texture2D(texture, topTextureCoordinate).a;

//    float h = -im1p1 - 2.0 * i0p1 - ip1p1 + im1m1 + 2.0 * i0m1 + ip1m1;
//    float v = -im1m1 - 2.0 * im10 - im1p1 + ip1m1 + 2.0 * ip10 + ip1p1;
//    float mag = length(vec2(h, v));

//    float l = smoothstep(0.0,0.1,8.0 * i00 - im1m1 - im10 - im1p1 - ip1m1 - ip10 - ip1p1 - i0m1 - i0p1);

    float l = step(1.0,1.0-im1m1 * im10 * im1p1 * ip1m1 * ip10 * ip1p1 * i0m1 * i0p1)*(1.0-step(1.0,1.0-i00));

    gl_FragColor = vec4(l, 0.0, 0.0, 1.0);
 }