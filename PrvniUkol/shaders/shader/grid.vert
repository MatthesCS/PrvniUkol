#version 150
in vec2 inPosition; // input from the vertex buffer
out vec4 vertColor; // output from this shader to the next pipeline stage
out vec3 vertNormal;
out vec3 vertPosition;
out vec2 texCoord;
out vec3 tx;
out vec3 ty;
uniform mat4 mat; // variable constant for all vertices in a single draw
const int POCETSVETEL = 3;
uniform vec3 svetlaPozice[POCETSVETEL];
uniform vec3 oko;
uniform float svetlo;
uniform float lesklost;
uniform float utlumKonst;
uniform float utlumLin;
uniform float utlumKvadr;
uniform vec3 ambBarva;
uniform vec3 difBarva;
uniform vec3 specBarva;
uniform vec3 primBarva[POCETSVETEL];

const float PI = 3.1415927;
const float DELTA = 0.001;

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
    tx = surface(paramPos + dx) - surface(paramPos - dx);
    ty = surface(paramPos + dy) - surface(paramPos - dy);
    return normalize(cross(ty, tx));
}

void phong(vec2 paramPos, int cisloSvetla, out vec3 ambi, out vec3 diff, out vec3 spec)
{
    vec3 position = surface(paramPos);
    vec3 normal = normal(paramPos);

    vec3 smerSvetla = svetlaPozice[cisloSvetla] - position;
    float vzdalenostSvetla = length(smerSvetla);
    smerSvetla = normalize(smerSvetla);
    vec3 smerOka = normalize(oko - position);

    vec3 matDifCol = difBarva;
    vec3 matSpecCol = specBarva;
    vec3 ambientLightCol = ambBarva;
    vec3 directLightCol = primBarva[cisloSvetla];

    vec3 reflected = reflect(normalize(-smerSvetla), normal);

    float difCoef = max(0, dot(normal, smerSvetla));
    float specCoef = 0.0;
    float utlum = 1.0;
    if (difCoef > 0.0)
    {
        specCoef = max(0, pow(dot(smerOka, reflected), lesklost));
        utlum = 1.0 / (utlumKonst + utlumLin * vzdalenostSvetla + utlumKvadr * vzdalenostSvetla * vzdalenostSvetla);
    }

    ambi = ambientLightCol * matDifCol;
    diff = utlum * directLightCol * matDifCol * difCoef; 
    spec = utlum * directLightCol * matSpecCol * specCoef;
}

void blinnPhong(vec2 paramPos, int cisloSvetla, out vec3 ambi, out vec3 diff, out vec3 spec)
{
    vec3 position = surface(paramPos);
    vec3 normal = normal(paramPos);

    vec3 smerSvetla = normalize(svetlaPozice[cisloSvetla] - position);
    vec3 smerOka = normalize(oko - position);
    vec3 halfVektor = normalize(smerSvetla + smerOka);

    vec3 matDifCol = difBarva;
    vec3 matSpecCol = specBarva;
    vec3 ambientLightCol = ambBarva;
    vec3 directLightCol = primBarva[cisloSvetla];

    vec3 reflected = reflect(normalize(-smerSvetla), normal); //smerSvÄ›tla zĂˇpornÄ›

    float difCoef = max(0, dot(normal, smerSvetla));
    float specCoef = max(0, pow(dot(normal, halfVektor), lesklost));

    ambi = ambientLightCol * matDifCol;
    diff = directLightCol * matDifCol * difCoef;
    spec = directLightCol * matSpecCol * specCoef;
}

void main() {
    vec3 position = surface(inPosition);
    gl_Position = mat * vec4(position, 1.0);

    texCoord = vec2(inPosition.x, -inPosition.y + 1) * 4;

    vertNormal = normal(inPosition);
    vertPosition = position;
    vertColor = vec4(vec3(vertNormal)*0.5+0.5, 1.0);
    //vertColor = vec4(inPosition, 0.0, 1.0);
    //vertColor = vec3(position);
    //vertColor = vec4(texCoord, 0.0, 1.0);
    vertColor = vec4(1.0);

    vec3 ambientSum = vec3(0);
    vec3 diffuseSum = vec3(0);
    vec3 specSum = vec3(0);
    vec3 ambi, diff, spec;

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
