#include "flywheel:util/quaternion.glsl"
#include "flywheel:util/matrix.glsl"

void flw_instanceVertex(in FlwInstance instance) {
    /*flw_vertexPos.x *= instance.size.x;
    flw_vertexPos.y *= instance.size.y;
    flw_vertexPos.z *= instance.size.z;*/
    flw_vertexTexCoord.t = instance.v0 + flw_vertexPos.x * instance.vScale;
    flw_vertexTexCoord.s = instance.u0 + flw_vertexPos.z * instance.uScale;

    flw_vertexColor = instance.color;
    flw_vertexPos = instance.pose * flw_vertexPos;
    flw_vertexNormal = mat3(transpose(inverse(instance.pose))) * flw_vertexNormal;
    //flw_vertexOverlay = instance.overlay;
    flw_vertexLight = max(vec2(instance.light) / 256., flw_vertexLight);
}