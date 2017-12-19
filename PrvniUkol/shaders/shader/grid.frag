#version 150
const int POCETSVETEL = 3;
const int POCETMATERIALU = 8;

const int LAMBERTF = 8;
const int PHONGF = 9;
const int BLINNPHONGF = 10;
const int AMBF = 11;
const int DIFFF = 12;
const int SPECPHONGF = 13;
const int SPECBLINNPHONGF = 14;

in vec4 vertColor; // input from the previous pipeline stage
in vec3 vertPosition;
in vec2 texCoord;
in vec3 eyeVec;  //normal
in vec3 lightVec[POCETSVETEL];  //normal

out vec4 outColor; // output from the fragment shader

uniform vec3 oko;
uniform mat3 svetla[POCETSVETEL];
uniform mat4 materialy[POCETMATERIALU];
uniform float svetlo;
uniform int material;
uniform sampler2D textura;
uniform sampler2D texturaNormal;
uniform sampler2D texturaVyska;

void osvetleni(int cisloSvetla, out vec4 ambi, out vec4 diff, out vec4 spec)
{
    vec3 position = vertPosition;
    vec3 utlumy = svetla[cisloSvetla][2];

    vec3 smerSvetla = normalize(lightVec[cisloSvetla]);
    float vzdalenostSvetla = length(svetla[cisloSvetla][0] - position);
    vec3 smerOka = normalize(eyeVec);
    vec3 halfVektor = normalize(smerSvetla + smerOka);

    float vyska = texture(texturaVyska, texCoord).r;
    float koefL = 0.04;
    float koefK = -0.02;
    vyska = vyska * koefL + koefK;

    vec3 smerOko = normalize(oko - position);
    vec2 posun = smerOka.xy / smerOka.z * vyska;
    posun = posun + texCoord;

    vec3 normal = texture(texturaNormal, posun).rgb * 2 -1;

    //vec3 smerSvetla = normalize(svetlaPozice[cisloSvetla] - position);
    //vec3 smerOka = normalize(oko - position);

    vec4 ambientLightCol = materialy[material][0];
    vec4 matDifCol = materialy[material][1];
    vec4 matSpecCol = materialy[material][2];
    vec3 directLightCol = svetla[cisloSvetla][1];
    float lesk = materialy[material][3].x;

    vec3 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = pow(max(0, smerSvetla.z), 0.7) * max(0, dot(normal, smerSvetla));
    float specCoef = 0;
    float utlum = 1.0;
    if (difCoef > 0.0)
    {
        specCoef = pow(max(0, smerSvetla.z), 0.7) * pow(max(0,dot(smerOka, reflected)), lesk);
        if (svetlo == BLINNPHONGF || svetlo == SPECBLINNPHONGF)
        {
            specCoef = pow(max(0, smerSvetla.z), 0.7) * max(0, pow(dot(normal, halfVektor), lesk));
        }
        float podil = utlumy.x + utlumy.y * vzdalenostSvetla + utlumy.z * vzdalenostSvetla * vzdalenostSvetla;
        if(podil > 0)
        {
            utlum /= podil;
        }
    }

    ambi = ambientLightCol * matDifCol;
    diff = utlum * vec4(directLightCol, 1.0) * matDifCol * difCoef; 
    spec = utlum * vec4(directLightCol, 1.0) * matSpecCol * specCoef;
}

void main() {
	//outColor = vertColor * (texture(texturaNormal, texCoord) - texture(textura, texCoord)) * normal;
        outColor = vertColor;
	//outColor = texture(textureID, texCoord);
        //outColor = vertColor;


        if(svetlo >= LAMBERTF && svetlo <= SPECBLINNPHONGF)
        {
            vec4 ambientSum = vec4(0);
            vec4 diffuseSum = vec4(0);
            vec4 specSum = vec4(0);
            vec4 ambi, diff, spec;
            for( int i=0; i<POCETSVETEL; ++i )
            {
                osvetleni(i, ambi, diff, spec);
                ambientSum += ambi;
                diffuseSum += diff;
                specSum += spec;
            }
            ambientSum /= POCETSVETEL;
            if(svetlo == LAMBERTF)
            {
                outColor = ambientSum + diffuseSum;
            }
            else if(svetlo == PHONGF)
            {
                outColor = ambientSum + diffuseSum + specSum;
            }
            else if(svetlo == BLINNPHONGF)
            {
                outColor = ambientSum + diffuseSum + specSum;
            }
            else if(svetlo == AMBF)
            {
                outColor = ambientSum;
            }
            else if(svetlo == DIFFF)
            {
                outColor = diffuseSum;
            }
            else if(svetlo == SPECPHONGF)
            {
                outColor = specSum;
            }
            else if(svetlo == SPECBLINNPHONGF)
            {
                outColor = specSum;
            }
        }
        outColor *=  texture(textura, texCoord);
} 
