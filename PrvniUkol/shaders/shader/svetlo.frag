#version 150
in vec3 vertColor;
in vec3 vertPosition;
out vec4 outColor;

uniform mat4 svetlo;

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
        outColor =  vec4(vertColor * utlum, 1.0);
} 
