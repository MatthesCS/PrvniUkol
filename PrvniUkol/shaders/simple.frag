#version 150
in vec3 vertColor; // input from the previous pipeline stage
out vec4 outColor; // output from the fragment shader
in vec3 vertNormal;
in vec3 vertPosition;
uniform vec3 poziceSvetla;//může být pole
uniform vec3 oko;
uniform float svetlo;
void main() {
	outColor = vec4(vertColor, 1.0);

        vec3 inPosition = vertPosition;
        vec3 inNormal = normalize(vertNormal);
    	vec3 smerSvetla = normalize(poziceSvetla - inPosition);
    	vec3 smerOka = normalize(oko - inPosition);
    	float lesklost = 70.0;


        if(svetlo == 2.0)
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

        vec3 vertColor = ambiComponent + difComponent + specComponent;
        outColor = vec4(vertColor, 1.0);
        }
} 
