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
out vec2 texCoord;
uniform mat4 mat; 
uniform mat3 svetla[POCETSVETEL];
uniform mat4 materialy[POCETMATERIALU];
uniform vec3 oko;
uniform float svetlo;
uniform int material;
uniform float cas;

vec3 desk(vec2 paramPos)
{
    return vec3(paramPos, 2.0);
}

vec3 torus(vec2 paramPos)
{
	float s = 2 * PI * paramPos.x;
	float t = 2 * PI * paramPos.y;
	return vec3(
		3*cos(s)+cos(t)*cos(s),
		3*sin(s)+cos(t)*sin(s),
		sin(t)
	);
}

vec3 sphere(vec2 paramPos)
{
	float a = 2 * PI * paramPos.x;
	float z = PI * (0.5 - paramPos.y);
	return vec3(
		cos(a)*cos(z),
		sin(a)*cos(z),
		sin(z)
	);
}

vec3 surface(vec2 paramPos)
{
    return sphere(paramPos);
    //return desk(paramPos);
    //return torus(paramPos);

}

vec3 normal(vec2 paramPos)
{
    vec2 dx = vec2(DELTA, 0);
    vec2 dy = vec2(0, DELTA);
    vec3 tx = surface(paramPos + dx) - surface(paramPos - dx);
    vec3 ty = surface(paramPos + dy) - surface(paramPos - dy);
    return normalize(cross(ty, tx));
}

mat3 tangentMat(vec2 paramPos)
{
    vec2 dx = vec2(DELTA, 0);
    vec2 dy = vec2(0, DELTA);
    vec3 tx = surface(paramPos + dx) - surface(paramPos - dx);
    vec3 ty = surface(paramPos + dy) - surface(paramPos - dy);
    vec3 x = normalize(tx);
    vec3 y = normalize(-ty);
    vec3 z = cross(x,y);
    x = cross(y,z);
    return mat3(x,y,z);
}

void osvetleni(vec2 paramPos, int cisloSvetla, out vec4 ambi, out vec4 diff, out vec4 spec)
{
    vec3 position = surface(paramPos);
    vec3 normal = normal(paramPos);
    vec3 utlumy = svetla[cisloSvetla][2];

    vec3 smerSvetla = svetla[cisloSvetla][0] - position;
    float vzdalenostSvetla = length(smerSvetla);
    smerSvetla = normalize(smerSvetla);
    vec3 smerOka = normalize(oko - position);
    vec3 halfVektor = normalize(smerSvetla + smerOka);

    vec4 ambientLightCol = materialy[material][0];
    vec4 matDifCol = materialy[material][1];
    vec4 matSpecCol = materialy[material][2];
    vec3 directLightCol = svetla[cisloSvetla][1];
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

    ambi = ambientLightCol * matDifCol;
    diff = utlum * vec4(directLightCol, 1.0) * matDifCol * difCoef; 
    spec = utlum * vec4(directLightCol, 1.0) * matSpecCol * specCoef;
}

void main() {
    vec3 position = surface(inPosition);
    gl_Position = mat * vec4(position, 1.0);

    texCoord = vec2(inPosition.x, -inPosition.y + 1) * 4;

    vertPosition = position;
    //vertColor = vec4(vec3(vertNormal)*0.5+0.5, 1.0);
    //vertColor = vec4(inPosition, 0.0, 1.0);
    //vertColor = vec3(position);
    //vertColor = vec4(texCoord, 0.0, 1.0);
    vertColor = vec4(1.0);

    mat3 tanMat = tangentMat(inPosition);
    eyeVec = (oko - vertPosition)* tanMat;
    for(int i=0; i< POCETSVETEL; i++)
    {
        lightVec[i] = (svetla[i][0] - vertPosition) * tanMat;
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
