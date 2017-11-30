#version 150
in vec2 inPosition;
out vec3 vertColor;
uniform vec3 pozice;
uniform vec3 barva;
uniform mat4 mat;

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

void main() {
    vec3 position = sphere(inPosition)/5 + pozice;
    gl_Position = mat * vec4(position, 1.0);

    vertColor = barva;
} 
