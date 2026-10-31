#version 330

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:matrix.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 texProj0;
in vec4 vertexColor;

const mat4 SCALE_TRANSLATE = mat4(
    0.5, 0.0, 0.0, 0.25,
    0.0, 0.5, 0.0, 0.25,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
);

mat4 end_portal_layer(float layer) {
    mat4 translate = mat4(
        1.0, 0.0, 0.0, 17.0 / layer,
        0.0, 1.0, 0.0, (2.0 + layer / 1.5) * (GameTime * 1.5),
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    );

    mat2 rotate = mat2_rotate_z(radians((layer * layer * 4321.0 + layer * 9.0) * 2.0));

    mat2 scale = mat2((4.5 - layer / 4.0) * 2.0);

    return mat4(scale * rotate) * translate * SCALE_TRANSLATE;
}

out vec4 fragColor;

void main() {
    vec4 color = textureProj(Sampler0, texProj0) * vec4(vertexColor[0], vertexColor[1], vertexColor[2], 0.5);
    for (int i = 0; i < PORTAL_LAYERS; i++) {
        // integer division as the original's 1/(i+1): full alpha for the first layer, none for the others
        color += textureProj(Sampler1, texProj0 * end_portal_layer(float(i + 1))) * vec4(vertexColor[0], vertexColor[1], vertexColor[2], float(1 / (i + 1)));
    }
    fragColor = color;
}
