#version 150
const int POCETSVETEL = 3;

in vec4 vertColor; // input from the previous pipeline stage
in vec3 vertPosition;
in vec2 texCoord;
in vec3 eyeVec;  //normal
in vec3 lightVec[POCETSVETEL];  //normal

out vec4 outColor; // output from the fragment shader

uniform vec3 oko;
uniform mat3 svetla[POCETSVETEL];
uniform float svetlo;
uniform float lesklost;
uniform vec3 ambBarva;
uniform vec3 difBarva;
uniform vec3 specBarva;
uniform sampler2D textura;
uniform sampler2D texturaNormal;

void phong(int cisloSvetla, out vec3 ambi, out vec3 diff, out vec3 spec)
{
    vec3 position = vertPosition;
    vec3 utlumy = svetla[cisloSvetla][2];

    vec3 normal = texture(texturaNormal, texCoord).rgb * 2 -1;

    //vec3 smerSvetla = normalize(svetlaPozice[cisloSvetla] - position);
    //vec3 smerOka = normalize(oko - position);
    vec3 smerSvetla = normalize(lightVec[cisloSvetla]);
    float vzdalenostSvetla = length(svetla[cisloSvetla][0] - position);
    vec3 smerOka = normalize(eyeVec);

    vec3 matDifCol = difBarva;
    vec3 matSpecCol = specBarva;
    vec3 ambientLightCol = ambBarva;
    vec3 directLightCol = svetla[cisloSvetla][1];

    vec3 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = pow(max(0, smerSvetla.z), 0.7) * max(0, dot(normal, smerSvetla));
    float specCoef = 0;
    float utlum = 1.0;
    if (difCoef > 0.0)
    {
        specCoef = pow(max(0, smerSvetla.z), 0.7) * pow(max(0,dot(smerOka, reflected)), lesklost);
        float podil = utlumy.x + utlumy.y * vzdalenostSvetla + utlumy.z * vzdalenostSvetla * vzdalenostSvetla;
        if(podil > 0)
        {
            utlum /= podil;
        }
    }

    ambi = ambientLightCol * matDifCol;
    diff = utlum * directLightCol * matDifCol * difCoef; 
    spec = utlum * directLightCol * matSpecCol * specCoef;
}

void blinnPhong(int cisloSvetla, out vec3 ambi, out vec3 diff, out vec3 spec)
{
    vec3 position = vertPosition;
    vec3 utlumy = svetla[cisloSvetla][2];
    float vzdalenostSvetla = length(svetla[cisloSvetla][0] - position);

    vec3 normal = texture(texturaNormal, texCoord).rgb * 2 -1;

    vec3 smerSvetla = normalize(lightVec[cisloSvetla]);
    vec3 smerOka = normalize(eyeVec);
    vec3 halfVektor = normalize(smerSvetla + smerOka);

    vec3 matDifCol = difBarva;
    vec3 matSpecCol = specBarva;
    vec3 ambientLightCol = ambBarva;
    vec3 directLightCol = svetla[cisloSvetla][1];

    vec3 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = pow(max(0, smerSvetla.z), 0.7) * max(0, dot(normal, smerSvetla));
    float specCoef = 0;
    float utlum = 1.0;
    if (difCoef > 0.0)
    {
        specCoef = pow(max(0, smerSvetla.z), 0.7) * max(0, pow(dot(normal, halfVektor), lesklost));
        float podil = utlumy.x + utlumy.y * vzdalenostSvetla + utlumy.z * vzdalenostSvetla * vzdalenostSvetla;
        if(podil > 0)
        {
            utlum /= podil;
        }
    }

    ambi = ambientLightCol * matDifCol;
    diff = utlum * directLightCol * matDifCol * difCoef; 
    spec = utlum * directLightCol * matSpecCol * specCoef;
}

void main() {
	//outColor = vertColor * (texture(texturaNormal, texCoord) - texture(textura, texCoord)) * normal;
        outColor = vertColor * texture(textura, texCoord);
	//outColor = texture(textureID, texCoord);
        //outColor = vertColor;


        if(svetlo == 3.0 || svetlo == 4.0)
        {
            vec3 ambientSum = vec3(0);
            vec3 diffuseSum = vec3(0);
            vec3 specSum = vec3(0);
            vec3 ambi, diff, spec;
            for( int i=0; i<POCETSVETEL; ++i )
            {
                if(svetlo == 3.0){
                phong(i, ambi, diff, spec);
                }
                if(svetlo == 4.0){
                blinnPhong(i, ambi, diff, spec);
                }
                ambientSum += ambi;
                diffuseSum += diff;
                specSum += spec;
            }
            ambientSum /= POCETSVETEL;
            outColor = outColor * vec4(ambientSum + diffuseSum + specSum, 1.0);
        }
} 
