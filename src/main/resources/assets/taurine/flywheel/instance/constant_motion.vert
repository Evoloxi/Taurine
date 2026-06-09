void flw_instanceVertex(in FlwInstance i) {
    float dt = flw_renderSeconds - i.anchorTime;
    flw_vertexPos = i.pose * flw_vertexPos;
    flw_vertexPos.xyz += i.motion * dt;
    flw_vertexNormal = mat3(transpose(inverse(i.pose))) * flw_vertexNormal;
    // Some drivers have a bug where uint over float division is invalid, so use an explicit cast.
    flw_vertexLight = max(vec2(i.light) / 256.0, flw_vertexLight);
}
