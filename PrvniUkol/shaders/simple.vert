#version 150
in vec3 inPosition; // input from the vertex buffer
in vec3 inNormal; // input from the vertex buffer, normalizované
out vec3 vertColor; // output from this shader to the next pipeline stage
out vec3 vertNormal;
out vec3 vertPosition;
uniform mat4 mat; // variable constant for all vertices in a single draw
uniform vec3 poziceSvetla;//může být pole
uniform vec3 oko;
uniform float svetlo;

void main() {
	gl_Position = mat * vec4(inPosition, 1.0);

	vec3 smerSvetla = normalize(poziceSvetla - inPosition);
	vec3 smerOka = normalize(oko - inPosition);
	float lesklost = 70.0;

	vertColor = inNormal * 0.5 + 0.5;
	vertNormal = inNormal;
	vertPosition = inPosition;

    if(svetlo == 1.0)
    {
	//lepší v uniformech
	vec3 matDifCol = vec3(0.8, 0.9, 0.6);//difůzní barva (barva co sežere materiál
	vec3 matSpecCol = vec3(1.0);//zrcadlově odražená barva
	vec3 ambientLightCol = vec3(0.3, 0.1, 0.5);//barva odrazu?
    vec3 directLightCol = vec3(1.0, 0.9, 0.9);//barva světla

    vec3 reflected = reflect(-smerSvetla, inNormal); //smerSvětla záporně

    float difCoef = max(0, dot(inNormal, smerSvetla));
    float specCoef = max(0, pow(dot(smerOka, reflected), lesklost));

    vec3 ambiComponent = ambientLightCol * matDifCol; //ambientní složka (ještě může být vzdálenost světla)
    vec3 difComponent = directLightCol * matDifCol * difCoef;  //difůzní složka
    vec3 specComponent = directLightCol * matSpecCol * specCoef;

    vertColor = ambiComponent + difComponent + specComponent;
    }
} 
