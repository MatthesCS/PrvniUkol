#version 150
in vec2 inPosition; // input from the vertex buffer
out vec3 vertColor; // output from this shader to the next pipeline stage
out vec3 vertNormal;
out vec3 vertPosition;
out vec2 texCoord;
uniform mat4 mat; // variable constant for all vertices in a single draw
uniform vec3 svetlaPozice[2];
uniform vec3 oko;
uniform float svetlo;

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
	float z = 2 * PI * paramPos.x;
	float a = PI * (0.5 - paramPos.y);
	return vec3(
		cos(z)*cos(a),
		sin(z)*cos(a),
		sin(a)
	);
        /*
        float s = 2 * PI * paramPos.x;
	float t = PI * paramPos.y;
	return vec3(
		sin(t)*cos(s),
		sin(t)*sin(s),
		cos(t)
	);*/
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
	return normalize(cross(tx, ty));
}

vec3 phong(vec2 paramPos, int cisloSvetla)
{
    vec3 position = surface(paramPos);
    vec3 normal = normal(paramPos);

    //vec3 smerSvetla = normalize(poziceSvetla - position);
    vec3 smerSvetla = normalize(svetlaPozice[cisloSvetla] - position);
    vec3 smerOka = normalize(oko - position);
    float lesklost = 70.0;

    //lepší v uniformech
    vec3 matDifCol = vec3(0.8, 0.9, 0.6);//difůzní barva (barva co sežere materiál
    vec3 matSpecCol = vec3(1.0);//zrcadlově odražená barva
    vec3 ambientLightCol = vec3(0.3, 0.1, 0.5);//barva odrazu?
    vec3 directLightCol = vec3(1.0, 0.9, 0.9);//barva světla

    vec3 reflected = reflect(normalize(-smerSvetla), normal); //smerSvětla záporně

    float difCoef = max(0, dot(normal, smerSvetla));
    float specCoef = max(0, pow(dot(smerOka, reflected), lesklost));

    vec3 ambiComponent = ambientLightCol * matDifCol; //ambientní složka (ještě může být vzdálenost světla)
    vec3 difComponent = directLightCol * matDifCol * difCoef;  //difůzní složka
    vec3 specComponent = directLightCol * matSpecCol * specCoef;

    return ambiComponent + difComponent + specComponent;
}

void main() {
    vec3 position = surface(inPosition);
    gl_Position = mat * vec4(position, 1.0);

    vertNormal = normal(inPosition);
    vertColor = vec3(vertNormal)*0.5+0.5;
    //vertColor = vec3(inPosition, 0.0);
    //vertColor = vec3(position);
    vertPosition = position;
    texCoord = mod(inPosition * 4,1);
//vertColor = vec3(texCoord, 0.0);

    if(svetlo == 1.0)
    {
	vertColor = phong(inPosition, 0) * phong(inPosition, 1);
    }
    if (svetlo == 3.0)
    {
        vertColor = vec3(1.0, 1.0, 1.0);
    }
} 
