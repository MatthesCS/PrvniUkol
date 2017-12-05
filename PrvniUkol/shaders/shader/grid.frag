#version 150
in vec4 vertColor; // input from the previous pipeline stage
out vec4 outColor; // output from the fragment shader
in vec3 vertNormal;
in vec3 vertPosition;
in vec2 texCoord;
uniform vec3 oko;
const int POCETSVETEL = 3;
uniform vec3 svetlaPozice[POCETSVETEL];
uniform float svetlo;
uniform vec3 ambBarva;
uniform vec3 difBarva;
uniform vec3 specBarva;
uniform vec3 primBarva[POCETSVETEL];
uniform sampler2D textura;
uniform sampler2D texturaNormal;

void phong(int cisloSvetla, out vec3 ambi, out vec3 diff, out vec3 spec)
{
    vec3 position = vertPosition;
    vec3 normal = normalize(vertNormal);


    vec3 smerSvetla = normalize(svetlaPozice[cisloSvetla] - position);
    vec3 smerOka = normalize(oko - position);
    float lesklost = 70.0;

    vec3 matDifCol = difBarva;
    vec3 matSpecCol = specBarva;
    vec3 ambientLightCol = ambBarva;
    vec3 directLightCol = primBarva[cisloSvetla];

    vec3 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = max(0, dot(normal, smerSvetla));
    float specCoef = max(0, pow(dot(smerOka, reflected), lesklost));

    ambi = ambientLightCol * matDifCol;
    diff = directLightCol * matDifCol * difCoef;
    spec = directLightCol * matSpecCol * specCoef;
}

void blinnPhong(int cisloSvetla, out vec4 ambi, out vec4 diff, out vec4 spec)
{
    vec4 position = vec4(vertPosition, 1.0);
    vec4 normal = vec4(normalize(vertNormal), 1.0);
    //normal = (texture(texturaNormal, texCoord) - texture(textura, texCoord)) * normal;

    vec4 smerSvetla = normalize(vec4(svetlaPozice[cisloSvetla], 1.0) - position);
    vec4 smerOka = normalize(vec4(oko, 1.0) - position);
    vec4 halfVektor = normalize(smerSvetla + smerOka);
    float lesklost = 70.0;

    vec4 matDifCol = vec4(difBarva, 1.0);
    vec4 matSpecCol = vec4(specBarva, 1.0);
    vec4 ambientLightCol = vec4(ambBarva, 1.0);
    vec4 directLightCol = vec4(primBarva[cisloSvetla], 1.0);

    vec4 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = max(0, dot(normal, smerSvetla));
    float specCoef = max(0, pow(dot(normal, halfVektor), lesklost));

    ambi = ambientLightCol * matDifCol;
    diff = directLightCol * matDifCol * difCoef;
    spec = directLightCol * matSpecCol * specCoef;
}

void main() {
        vec3 normal = normalize(vertNormal);
	//outColor = vertColor * (texture(texturaNormal, texCoord) - texture(textura, texCoord)) * normal;
        outColor = vertColor * texture(textura, texCoord);
	//outColor = texture(textureID, texCoord);
        //outColor = vec4(vertColor, 1.0);


        if(svetlo == 3.0)
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
                //blinnPhong(i, ambi, diff, spec);
                }
                ambientSum += ambi;
                diffuseSum += diff;
                specSum += spec;
            }
            ambientSum /= POCETSVETEL;
            outColor = outColor * vec4(ambientSum + diffuseSum + specSum, 1.0);
        }
        if (svetlo == 4.0)
        {
            vec4 ambientSum = vec4(0);
            vec4 diffuseSum = vec4(0);
            vec4 specSum = vec4(0);
            vec4 ambi, diff, spec;
            for( int i=0; i<POCETSVETEL; ++i )
            {
                if(svetlo == 3.0){
                //phong(i, ambi, diff, spec);
                }
                if(svetlo == 4.0){
                blinnPhong(i, ambi, diff, spec);
                }
                ambientSum += ambi;
                diffuseSum += diff;
                specSum += spec;
            }
            ambientSum /= POCETSVETEL;
            outColor = outColor * ambientSum + diffuseSum + specSum;
        }
} 
