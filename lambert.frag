precision highp float;
varying float vDot;
uniform sampler2D sunDepth;
uniform sampler2D rainbow;
varying vec3 vSunPosition;
uniform int special;

void main(){
    vec2 moments = texture2D(sunDepth, vSunPosition.xy).xy;
    float mean = moments.x;
    float var = moments.y-mean*mean;
    var = max(var, 0.001);
    float t = vSunPosition.z;
    float d = t - mean;

    // [0, 1]
    float pmax = var/(var+d*d);
    pmax = smoothstep(0.9,1.0,pmax);
    pmax = max(pmax, float(t <= mean));
    if (special > 0){
        gl_FragColor = vec4(texture2D(rainbow, vSunPosition.xy).rgb, 1.0);
    }
    else
        gl_FragColor = vec4(vec3(vDot*pmax), 1.0);
//    gl_FragColor = vec4(1.0-smoothstep(0.0, 0.1, abs(occZ-vSunPosition.z)), 0.0, 0.0, 1.0);

//    if (vDot == 0.0)
//        gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
//    else
}