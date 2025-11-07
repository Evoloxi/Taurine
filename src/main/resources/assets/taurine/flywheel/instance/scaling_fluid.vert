#include "flywheel:util/quaternion.glsl"
#include "flywheel:util/matrix.glsl"

void flw_instanceVertex(in FlwInstance instance) {
    vec3 absNormal = abs(flw_vertexNormal);
    vec2 uv;
    if (absNormal.y > 0.5) {
        uv = vec2(flw_vertexPos.x, flw_vertexPos.z);
    } else if (absNormal.x > 0.5) {
        uv = vec2(flw_vertexPos.z, flw_vertexPos.y);
    } else {
        uv = vec2(flw_vertexPos.x, flw_vertexPos.y);
    }

    flw_vertexTexCoord.s = instance.u0 + uv.x * instance.uScale;
    flw_vertexTexCoord.t = instance.v0 + uv.y * instance.vScale;


    flw_vertexColor = instance.color;
    flw_vertexPos = instance.pose * flw_vertexPos;
    flw_vertexNormal = mat3(transpose(inverse(instance.pose))) * flw_vertexNormal;
    flw_vertexLight = max(vec2(instance.light) / 256., flw_vertexLight);
}