#version 150
const int POCETSVETEL = 3;
const int POCETMATERIALU = 8;

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

void phong(int cisloSvetla, out vec4 ambi, out vec4 diff, out vec4 spec)
{
    vec3 position = vertPosition;
    vec3 utlumy = svetla[cisloSvetla][2];

    vec3 normal = texture(texturaNormal, texCoord).rgb * 2 -1;

    //vec3 smerSvetla = normalize(svetlaPozice[cisloSvetla] - position);
    //vec3 smerOka = normalize(oko - position);
    vec3 smerSvetla = normalize(lightVec[cisloSvetla]);
    float vzdalenostSvetla = length(svetla[cisloSvetla][0] - position);
    vec3 smerOka = normalize(eyeVec);

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

void blinnPhong(int cisloSvetla, out vec4 ambi, out vec4 diff, out vec4 spec)
{
    vec3 position = vertPosition;
    vec3 utlumy = svetla[cisloSvetla][2];
    float vzdalenostSvetla = length(svetla[cisloSvetla][0] - position);

    vec3 normal = texture(texturaNormal, texCoord).rgb * 2 -1;

    vec3 smerSvetla = normalize(lightVec[cisloSvetla]);
    vec3 smerOka = normalize(eyeVec);
    vec3 halfVektor = normalize(smerSvetla + smerOka);

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
        specCoef = pow(max(0, smerSvetla.z), 0.7) * max(0, pow(dot(normal, halfVektor), lesk));
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
        outColor = vertColor * texture(textura, texCoord);
	//outColor = texture(textureID, texCoord);
        //outColor = vertColor;


        if(svetlo == 3.0 || svetlo == 4.0)
        {
            vec4 ambientSum = vec4(0);
            vec4 diffuseSum = vec4(0);
            vec4 specSum = vec4(0);
            vec4 ambi, diff, spec;
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
            outColor = outColor * (ambientSum + diffuseSum + specSum);
        }
} 
