#version 150
in vec2 inPosition;
out vec3 vertColor;
out vec3 vertPosition;
uniform mat4 svetlo;
uniform mat4 mat;
uniform mat3 rotacniMat;
uniform int vyska;

const float PI = 3.1415927;

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

vec3 kuzel(vec2 paramPos)
{
    float s = 2 * PI * paramPos.x;
    float t = paramPos.y;
    float prumer = tan(radians(svetlo[3].w)) * vyska;
    float x = t*cos(s) * prumer;
    float y = t*sin(s) * prumer;
    float z = t * vyska;
    return vec3(x,y,z);
}

void main() {
    vec3 pozice = svetlo[0].xyz;
    vec3 position = vec3(0.0);
    if(svetlo[3].w > abs(90))
    {
        position = sphere(inPosition)/5 + pozice;
    }
    else
    {
        position = rotacniMat * kuzel(inPosition)/5 + pozice; 
    }
    gl_Position = mat * vec4(position, 1.0);

    vertPosition = position;
    vertColor = svetlo[1].rgb;
} 
