#version 150
const int POCETSVETEL = 3;
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
uniform vec3 oko;
uniform float svetlo;
uniform float lesklost;
uniform vec3 ambBarva;
uniform vec3 difBarva;
uniform vec3 specBarva;

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

void phong(vec2 paramPos, int cisloSvetla, out vec3 ambi, out vec3 diff, out vec3 spec)
{
    vec3 position = surface(paramPos);
    vec3 normal = normal(paramPos);
    vec3 utlumy = svetla[cisloSvetla][2];

    vec3 smerSvetla = svetla[cisloSvetla][0] - position;
    float vzdalenostSvetla = length(smerSvetla);
    smerSvetla = normalize(smerSvetla);
    vec3 smerOka = normalize(oko - position);

    vec3 matDifCol = difBarva;
    vec3 matSpecCol = specBarva;
    vec3 ambientLightCol = ambBarva;
    vec3 directLightCol = svetla[cisloSvetla][1];

    vec3 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = max(0, dot(normal, smerSvetla));
    float specCoef = 0.0;
    float utlum = 1.0;
    if (difCoef > 0.0)
    {
        specCoef = max(0, pow(dot(smerOka, reflected), lesklost));
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

void blinnPhong(vec2 paramPos, int cisloSvetla, out vec3 ambi, out vec3 diff, out vec3 spec)
{
    vec3 position = surface(paramPos);
    vec3 normal = normal(paramPos);
    vec3 utlumy = svetla[cisloSvetla][2];

    vec3 smerSvetla = svetla[cisloSvetla][0] - position;
    float vzdalenostSvetla = length(smerSvetla);
    smerSvetla = normalize(smerSvetla);
    
    vec3 smerOka = normalize(oko - position);
    vec3 halfVektor = normalize(smerSvetla + smerOka);

    vec3 matDifCol = difBarva;
    vec3 matSpecCol = specBarva;
    vec3 ambientLightCol = ambBarva;
    vec3 directLightCol = svetla[cisloSvetla][1];

    vec3 reflected = reflect(normalize(-smerSvetla), normal); //smerSvÄ›tla zĂˇpornÄ›

    float difCoef = max(0, dot(normal, smerSvetla));

    float specCoef = 0.0;
    float utlum = 1.0;
    if (difCoef > 0.0)
    {
        specCoef = max(0, pow(dot(normal, halfVektor), lesklost));
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
    vec3 position = surface(inPosition);
    gl_Position = mat * vec4(position, 1.0);

    texCoord = vec2(inPosition.x, -inPosition.y + 1) * 4;

    vertPosition = position;
    //vertColor = vec4(vec3(vertNormal)*0.5+0.5, 1.0);
    //vertColor = vec4(inPosition, 0.0, 1.0);
    //vertColor = vec3(position);
    //vertColor = vec4(texCoord, 0.0, 1.0);
    vertColor = vec4(1.0);

    vec3 ambientSum = vec3(0);
    vec3 diffuseSum = vec3(0);
    vec3 specSum = vec3(0);
    vec3 ambi, diff, spec;

    mat3 tanMat = tangentMat(inPosition);
    eyeVec = (oko - vertPosition)* tanMat;
    for(int i=0; i< POCETSVETEL; i++)
    {
        lightVec[i] = (svetla[i][0] - vertPosition) * tanMat;
    }


    if(svetlo == 1.0 || svetlo == 2.0)
    {
	for( int i=0; i<POCETSVETEL; ++i )
        {
            if(svetlo == 1.0){
            phong(inPosition, i, ambi, diff, spec);
            }
            if(svetlo == 2.0){
            blinnPhong(inPosition, i, ambi, diff, spec);
            }
            ambientSum += ambi;
            diffuseSum += diff;
            specSum += spec;
        }
        ambientSum /= POCETSVETEL;
        vertColor = vec4(ambientSum + diffuseSum + specSum, 1.0);
    }
} 
