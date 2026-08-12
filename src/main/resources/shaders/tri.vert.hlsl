// vk-arc 基础三角形顶点着色器（HLSL -> SPIR-V，经 libshaderc HLSL 路径）
struct VSIn {
    float3 pos : POSITION;
    float4 color : COLOR0;
};

struct VSOut {
    float4 pos : SV_POSITION;
    float4 color : COLOR0;
};

VSOut main(VSIn input) {
    VSOut output;
    output.pos = float4(input.pos, 1.0);
    output.color = input.color;
    return output;
}
