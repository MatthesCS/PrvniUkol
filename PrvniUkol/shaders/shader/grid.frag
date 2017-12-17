#version 150
const int POCETSVETEL = 3;
in vec4 vertColor; // input from the previous pipeline stage
out vec4 outColor; // output from the fragment shader
in vec3 vertNormal;
in vec3 vertPosition;
in vec2 texCoord;
in vec3 tx;
in vec3 ty;
in vec3 eyeVec;  //normal
in vec3 lightVec[POCETSVETEL];  //normal
uniform vec3 oko;
uniform vec3 svetlaPozice[POCETSVETEL];
uniform float svetlo;
uniform float lesklost;
uniform vec3 ambBarva;
uniform vec3 difBarva;
uniform vec3 specBarva;
uniform vec3 primBarva[POCETSVETEL];
uniform sampler2D textura;
uniform sampler2D texturaNormal;

void phong(int cisloSvetla, out vec3 ambi, out vec3 diff, out vec3 spec)
{
    vec3 position = vertPosition;

    vec3 normal = texture(texturaNormal, texCoord).rgb * 2 -1;

    //vec3 smerSvetla = normalize(svetlaPozice[cisloSvetla] - position);
    //vec3 smerOka = normalize(oko - position);
    vec3 smerSvetla = normalize(lightVec[cisloSvetla]);
    vec3 smerOka = normalize(eyeVec);

    vec3 matDifCol = difBarva;
    vec3 matSpecCol = specBarva;
    vec3 ambientLightCol = ambBarva;
    vec3 directLightCol = primBarva[cisloSvetla];

    vec3 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = pow(max(0, smerSvetla.z), 0.7) * max(0, dot(normal, smerSvetla));
    float specCoef = 0;
    if (difCoef > 0.0)
    {
        float specCoef = pow(max(0, smerSvetla.z), 0.7) * pow(max(0,dot(smerOka, reflected)), lesklost);
    }

    ambi = ambientLightCol * matDifCol;
    diff = directLightCol * matDifCol * difCoef;
    spec = directLightCol * matSpecCol * specCoef;
}

void blinnPhong(int cisloSvetla, out vec3 ambi, out vec3 diff, out vec3 spec)
{
    vec3 position = vertPosition;

    vec3 normal = texture(texturaNormal, texCoord).rgb * 2 -1;

    vec3 smerSvetla = normalize(lightVec[cisloSvetla]);
    vec3 smerOka = normalize(eyeVec);
    vec3 halfVektor = normalize(smerSvetla + smerOka);

    vec3 matDifCol = difBarva;
    vec3 matSpecCol = specBarva;
    vec3 ambientLightCol = ambBarva;
    vec3 directLightCol = primBarva[cisloSvetla];

    vec3 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = pow(max(0, smerSvetla.z), 0.7) * max(0, dot(normal, smerSvetla));
    float specCoef = pow(max(0, smerSvetla.z), 0.7) * max(0, pow(dot(normal, halfVektor), lesklost));

    ambi = ambientLightCol * matDifCol;
    diff = directLightCol * matDifCol * difCoef;
    spec = directLightCol * matSpecCol * specCoef;
}

void main() {
        vec3 normal = normalize(vertNormal);
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
