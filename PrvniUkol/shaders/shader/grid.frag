#version 150
in vec3 vertColor; // input from the previous pipeline stage
out vec4 outColor; // output from the fragment shader
in vec3 vertNormal;
in vec3 vertPosition;
in vec2 texCoord;
uniform vec3 oko;
uniform vec3 svetlaPozice[2];
uniform float svetlo;
uniform vec3 ambBarva;
uniform vec3 difBarva;
uniform vec3 specBarva;
uniform vec3 primBarva;
uniform sampler2D textureID;

vec3 phong(int cisloSvetla)
{
    vec3 position = vertPosition;
    vec3 normal = normalize(vertNormal);


    vec3 smerSvetla = normalize(svetlaPozice[cisloSvetla] - position);
    vec3 smerOka = normalize(oko - position);
    float lesklost = 70.0;

    vec3 matDifCol = difBarva;
    vec3 matSpecCol = specBarva;
    vec3 ambientLightCol = ambBarva;
    vec3 directLightCol = primBarva;

    vec3 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = max(0, dot(normal, smerSvetla));
    float specCoef = max(0, pow(dot(smerOka, reflected), lesklost));

    vec3 ambiComponent = ambientLightCol * matDifCol;
    vec3 difComponent = directLightCol * matDifCol * difCoef;
    vec3 specComponent = directLightCol * matSpecCol * specCoef;

    return ambiComponent + difComponent + specComponent;
}

void main() {
	outColor = vec4(vertColor, 1.0) * texture(textureID, texCoord);
	//outColor = texture(textureID, texCoord);
        //outColor = vec4(vertColor, 1.0);


        /*if(svetlo == 2.0)
        {
            outColor = outColor *  vec4(phong(0) * phong(1), 1.0);
        }*/
} 
