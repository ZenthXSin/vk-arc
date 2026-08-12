// vk-arc 基础三角形片元着色器（HLSL -> SPIR-V）
struct PSIn {
    float4 pos : SV_POSITION;
    float4 color : COLOR0;
};

float4 main(PSIn input) : SV_TARGET {
    return input.color;
}
