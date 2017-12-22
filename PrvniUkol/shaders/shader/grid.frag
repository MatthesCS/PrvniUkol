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

const int NORMALA = 6;

in vec4 vertColor; // input from the previous pipeline stage
in vec3 vertPosition;
in vec3 vertNormal;
in vec2 texCoord;
in vec3 eyeVec;  //normal
in vec3 lightVec[POCETSVETEL];  //normal

out vec4 outColor; // output from the fragment shader

uniform vec3 oko;
uniform mat4 svetla[POCETSVETEL];
uniform mat4 materialy[POCETMATERIALU];
uniform float svetlo;
uniform float cas;
uniform int material;
uniform sampler2D tex1;
uniform sampler2D tex1Normal;
uniform sampler2D tex1Vyska;
uniform sampler2D tex2;
uniform sampler2D tex2Normal;
uniform sampler2D tex2Vyska;
uniform int tex;
uniform int utlum;
uniform int mapping;
uniform int obarveni;

vec2 posun(vec3 smerOka)
{
    float vyska = 0.0;
    if(tex == 1)
    {
        vyska = texture(tex1Vyska, texCoord).r;
    }
    else if(tex == 2)
    {
        vyska = texture(tex2Vyska, texCoord).r;
    }
    float koefL = 0.04;
    float koefK = -0.02;
    vyska = vyska * koefL + koefK;

    vec2 posun = smerOka.xy / smerOka.z * vyska;
    posun = posun + texCoord;
    return posun;
}

vec3 normala(int ktera, vec2 posun)
{
    vec3 norm = normalize(vertNormal);
    if(ktera == 1)
    {
        if(tex == 1)
        {
            norm = texture(tex1Normal, texCoord).rgb * 2 - 1;
        }
        else if(tex == 2)
        {
            norm = texture(tex2Normal, texCoord).rgb * 2 - 1;
        }
    }
    else if (ktera == 2)
    {
        if(tex == 1)
        {
            norm = texture(tex1Normal, posun).rgb * 2 - 1;
        }
        else if(tex == 2)
        {
            norm = texture(tex2Normal, posun).rgb * 2 - 1;
        }
    }
    return norm;
}

void osvetleni(int cisloSvetla, out vec4 ambi, out vec4 diff, out vec4 spec)
{
    vec3 position = vertPosition;
    vec3 utlumy = vec3(0,0,0);
    if(utlum == 1)
    {
        utlumy = svetla[cisloSvetla][2].xyz;
    }

    vec3 smerSvetla = normalize(svetla[cisloSvetla][0].xyz - position);
    vec3 smerOka = normalize(oko - position);
    if(mapping > 0)
    {
        smerSvetla = normalize(lightVec[cisloSvetla]);
        smerOka = normalize(eyeVec);
    }
    float vzdalenostSvetla = length(svetla[cisloSvetla][0].xyz - position);
    vec3 halfVektor = normalize(smerSvetla + smerOka);

    vec2 posun = posun(smerOka);
    vec3 normal = normala(mapping, posun);

    vec4 ambientLightCol = materialy[material][0];
    vec4 matDifCol = materialy[material][1];
    vec4 matSpecCol = materialy[material][2];
    vec3 directLightCol = svetla[cisloSvetla][1].xyz;
    float lesk = materialy[material][3].x;

    vec3 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = max(0, dot(normal, smerSvetla));
    if(mapping > 0)
    {
        difCoef *= pow(max(0, smerSvetla.z), 0.7);
    }
    float specCoef = 0;
    float utlum = 1.0;
    if (difCoef > 0.0)
    {
        specCoef = pow(max(0,dot(smerOka, reflected)), lesk);
        if(mapping > 0)
        {
            specCoef *=  pow(max(0, smerSvetla.z), 0.7);
        }
        if (svetlo == BLINNPHONGF || svetlo == SPECBLINNPHONGF)
        {
            specCoef = max(0, pow(dot(normal, halfVektor), lesk));
            if(mapping > 0)
            {
                specCoef *=  pow(max(0, smerSvetla.z), 0.7);
            }
        }
        float podil = utlumy.x + utlumy.y * vzdalenostSvetla + utlumy.z * vzdalenostSvetla * vzdalenostSvetla;
        if(podil > 0)
        {
            utlum /= podil;
        }
    }
    
    vec3 smerSviceni = svetla[cisloSvetla][3].xyz;
    float uhelSviceni = svetla[cisloSvetla][3].w;

    float sviceni = degrees(acos(dot(normalize(smerSviceni), normalize(- (svetla[cisloSvetla][0].xyz - position)))));

    float rozmazani = clamp((sviceni - uhelSviceni)/(1-uhelSviceni),0.0,1.0);

    ambi = ambientLightCol * matDifCol;
    if(sviceni > uhelSviceni)
    {
        diff = vec4(0);
        spec = vec4(0);
    }
    else
    {
        diff = utlum * vec4(directLightCol, 1.0) * matDifCol * difCoef; 
        spec = utlum * vec4(directLightCol, 1.0) * matSpecCol * specCoef;
        diff *= rozmazani;
        spec *= rozmazani;
    }
}

void main() {
        outColor = vertColor;
        
        vec3 smerOka = normalize(oko - vertPosition);
        if(mapping > 0)
        {
            smerOka = normalize(eyeVec);    
        }
        vec2 posun = posun(smerOka);
        if(obarveni == NORMALA)
        {
            outColor = vec4(normala(mapping, posun), 1.0);
        }

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
                outColor *= ambientSum + diffuseSum;
            }
            else if(svetlo == PHONGF)
            {
                outColor *= ambientSum + diffuseSum + specSum;
            }
            else if(svetlo == BLINNPHONGF)
            {
                outColor *= ambientSum + diffuseSum + specSum;
            }
            else if(svetlo == AMBF)
            {
                outColor *= ambientSum;
            }
            else if(svetlo == DIFFF)
            {
                outColor *= diffuseSum;
            }
            else if(svetlo == SPECPHONGF)
            {
                outColor *= specSum;
            }
            else if(svetlo == SPECBLINNPHONGF)
            {
                outColor *= specSum;
            }
        }
        if (tex == 1)
        {
            outColor *=  texture(tex1, texCoord);
        }
        else if (tex == 2)
        {
            outColor *=  texture(tex2, texCoord);
        }
} 
