#version 150
const int POCETSVETEL = 3;
const int POCETMATERIALU = 8;

const int LAMBERTV = 1;
const int PHONGV = 2;
const int BLINNPHONGV = 3;
const int AMBV = 4;
const int DIFFV = 5;
const int SPECPHONGV = 6;
const int SPECBLINNPHONGV = 7;

const float PI = 3.1415927;
const float DELTA = 0.001;

in vec2 inPosition;
out vec4 vertColor;
out vec3 eyeVec;
out vec3 lightVec[POCETSVETEL];
out vec3 vertPosition;
out vec3 vertNormal;
out vec2 texCoord;
uniform mat4 mat; 
uniform mat4 svetla[POCETSVETEL];
uniform mat4 materialy[POCETMATERIALU];
uniform vec3 oko;
uniform vec4 pozice;
uniform float svetlo;
uniform int material;

vec3 desk(vec2 paramPos)
{
    if(pozice.w == 1)
    {
        return vec3(pozice.x, paramPos * pozice.yz - pozice.yz/2);
        velikost = pozice.yz;
    }
    else if (pozice.w == 2)
    {
        return vec3(paramPos.x * pozice.x - pozice.x/2, pozice.y, paramPos.y * pozice.z - pozice.z/2);
        velikost = pozice.xz;
    }
    else if (pozice.w == 3)
    {
        return vec3(paramPos * pozice.xy - pozice.xy/2, pozice.z);
        velikost = pozice.xy;
    }
    return vec3(0,0,0);
}

vec3 normal(vec2 paramPos)
{
     vec2 dx = vec2(DELTA, 0);
    vec2 dy = vec2(0, DELTA);
    vec3 tx = desk(paramPos + dx) - desk(paramPos - dx);
    vec3 ty = desk(paramPos + dy) - desk(paramPos - dy);
    bool otoc = false;
    if(pozice.w == 1)
    {
        otoc = (pozice.x < 0);
    }
    else if (pozice.w == 2)
    {
        otoc = (pozice.y > 0);
    }
    else if (pozice.w == 3)
    {
        otoc = (pozice.z < 0);
    }
    if(otoc)
    {
        return normalize(cross(tx, ty));
    }
    return normalize(cross(ty, tx));
}

mat3 tangentMat(vec2 paramPos)
{
    vec2 dx = vec2(DELTA, 0);
    vec2 dy = vec2(0, DELTA);
    vec3 tx = desk(paramPos + dx) - desk(paramPos - dx);
    vec3 ty = desk(paramPos + dy) - desk(paramPos - dy);
    bool otoc = false;
    if(pozice.w == 1)
    {
        otoc = (pozice.x < 0);
    }
    else if (pozice.w == 2)
    {
        otoc = (pozice.y > 0);
    }
    else if (pozice.w == 3)
    {
        otoc = (pozice.z < 0);
    }
    vec3 x,y;
    if(otoc)
    {
        x = normalize(ty);
        y = normalize(-tx);
    }
    else
    {
        x = normalize(tx);
        y = normalize(-ty);
    }
    vec3 z = cross(x,y);
    x = cross(y,z);
    return mat3(x,y,z);
}

void osvetleni(vec2 paramPos, int cisloSvetla, out vec4 ambi, out vec4 diff, out vec4 spec)
{
    vec3 position = desk(paramPos);
    vec3 normal = normal(paramPos);
    vec3 utlumy = svetla[cisloSvetla][2].xyz;
    //vec3 utlumy = vec3(0,0,0);

    vec3 smerSvetla = svetla[cisloSvetla][0].xyz - position;
    float vzdalenostSvetla = length(smerSvetla);
    smerSvetla = normalize(smerSvetla);
    vec3 smerOka = normalize(oko - position);
    vec3 halfVektor = normalize(smerSvetla + smerOka);

    vec4 ambientLightCol = materialy[material][0];
    vec4 matDifCol = materialy[material][1];
    vec4 matSpecCol = materialy[material][2];
    vec3 directLightCol = svetla[cisloSvetla][1].xyz;
    float lesk = materialy[material][3].x;

    vec3 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = max(0, dot(normal, smerSvetla));
    float specCoef = 0.0;
    float utlum = 1.0;
    if (difCoef > 0.0)
    {
        specCoef = max(0, pow(dot(smerOka, reflected), lesk));
        if(svetlo == BLINNPHONGV || svetlo == SPECBLINNPHONGV)
        specCoef = max(0, pow(dot(normal, halfVektor), lesk));
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
        diff = mix(vec4(0.0), diff, rozmazani);
        spec = mix(vec4(0.0), spec, rozmazani);
        //mix(x,y,a) = x*(1-a)+y*(a) -> mix(0,y,a) = y*a
    }
}

void main() {
    vec3 position = desk(inPosition);
    gl_Position = mat * vec4(position, 1.0);

    texCoord = vec2(inPosition.x, -inPosition.y + 1) * 5;

    vertPosition = position;
    vertNormal = normal(inPosition);
    vertColor = vec4(1.0);

    mat3 tanMat = tangentMat(inPosition);
    eyeVec = (oko - vertPosition)* tanMat;
    for(int i=0; i< POCETSVETEL; i++)
    {
        lightVec[i] = (svetla[i][0].xyz - vertPosition) * tanMat;
    }

    vec4 ambientSum = vec4(0);
    vec4 diffuseSum = vec4(0);
    vec4 specSum = vec4(0);
    vec4 ambi, diff, spec;

    if(svetlo >= LAMBERTV && svetlo <= SPECBLINNPHONGV)
    {
	for( int i=0; i<POCETSVETEL; ++i )
        {
            osvetleni(inPosition, i, ambi, diff, spec);
            ambientSum += ambi;
            diffuseSum += diff;
            specSum += spec;
        }
        ambientSum /= POCETSVETEL;

        if(svetlo == LAMBERTV)
        {
            vertColor = ambientSum + diffuseSum;
        }
        else if(svetlo == PHONGV)
        {
            vertColor = ambientSum + diffuseSum + specSum;
        }
        else if(svetlo == BLINNPHONGV)
        {
            vertColor = ambientSum + diffuseSum + specSum;
        }
        else if(svetlo == AMBV)
        {
            vertColor = ambientSum;
        }
        else if(svetlo == DIFFV)
        {
            vertColor = diffuseSum;
        }
        else if(svetlo == SPECPHONGV)
        {
            vertColor = specSum;
        }
        else if(svetlo == SPECBLINNPHONGV)
        {
            vertColor = specSum;
        }
    }
} 
