#version 150
in vec3 vertColor;
in vec3 vertPosition;
in vec2 pom;
out vec4 outColor;

uniform mat4 svetlo;
uniform mat4 screenMat;

void main() {
	outColor = vec4(vertColor, 1.0);
        vec3 utlumy = svetlo[2].xyz;
        float vzdalenostSvetla = length(vertPosition - svetlo[0].xyz);
        float utlum = 1.0;
        float podil = utlumy.x + utlumy.y * vzdalenostSvetla + utlumy.z * vzdalenostSvetla * vzdalenostSvetla;
        if(podil > 0)
        {
            utlum /= podil;
        }
        outColor =  vec4(vertColor * utlum, 0.5);
        /*float alpha = screenMat[int(mod(vertPosition.x*100*vertPosition.z*100,4))]
                                [int(mod(vertPosition.y*100*vertPosition.z*100,4))];
        if(alpha < outColor.a)
        {
            discard;
        }*/
} 
