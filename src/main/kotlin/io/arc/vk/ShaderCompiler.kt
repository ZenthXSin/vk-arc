package io.arc.vk

import org.lwjgl.util.shaderc.Shaderc
import java.nio.ByteBuffer

/**
 * 着色器编译器：把 GLSL 或 HLSL 源码编译成 SPIR-V。
 *
 * 说明（经 LWJGL shaderc 绑定验证）：
 *  - libshaderc 原生支持 [Shaderc.shaderc_source_language_glsl] 与
 *    [Shaderc.shaderc_source_language_hlsl] 两种源语言。
 *  - 因此本骨架不需要引入 DXC，可直接用 LWJGL 的 lwjgl-shaderc 完成
 *    "HLSL -> SPIR-V" 的编译（Shaderc 内走 HLSL->GLSL->SPIR-V 的实验路径）。
 *  - 生产环境若要严格 DXIL 级别的 HLSL 语义，再切换 DXC；本骨架保持单一后端。
 */
object ShaderCompiler {

    /** 着色器阶段。值直接复用 shaderc 的 stage 常量。 */
    enum class Stage(val shadercKind: Int) {
        VERTEX(Shaderc.shaderc_glsl_vertex_shader),
        FRAGMENT(Shaderc.shaderc_glsl_fragment_shader),
        COMPUTE(Shaderc.shaderc_glsl_compute_shader),
    }

    enum class Language(val sourceLanguage: Int) {
        GLSL(Shaderc.shaderc_source_language_glsl),
        HLSL(Shaderc.shaderc_source_language_hlsl),
    }

    /** 编译结果：SPIR-V 字节码或错误信息。 */
    class Result(val spirv: ByteBuffer?, val status: Int, val message: String?) {
        val ok: Boolean get() = status == Shaderc.shaderc_compilation_status_success
    }

    fun compile(
        source: String,
        stage: Stage,
        language: Language = Language.HLSL,
        fileName: String = "shader",
    ): Result {
        val compiler = Shaderc.shaderc_compiler_initialize()
        check(compiler != 0L) { "Shaderc 编译器初始化失败（缺少 native 库？）" }
        try {
            val options = Shaderc.shaderc_compile_options_initialize()
            check(options != 0L) { "Shaderc 编译选项初始化失败" }
            // 选项句柄必须在编译完成前保持有效，不能提前 release
            try {
                Shaderc.shaderc_compile_options_set_source_language(options, language.sourceLanguage)

                val result = Shaderc.shaderc_compile_into_spv(
                    compiler, source, stage.shadercKind, fileName, "main", options,
                )
                try {
                    val status = Shaderc.shaderc_result_get_compilation_status(result)
                    val message = Shaderc.shaderc_result_get_error_message(result)?.toString()
                    val spirv = if (status == Shaderc.shaderc_compilation_status_success) {
                        val data = Shaderc.shaderc_result_get_bytes(result)
                        @Suppress("REDUNDANT_CONVERSION_CALL")
                        val size = Shaderc.shaderc_result_get_length(result).toInt()
                        val copy = ByteBuffer.allocateDirect(size)
                        copy.put(data!!.duplicate().limit(size))
                        copy.flip()
                        copy
                    } else {
                        null
                    }
                    return Result(spirv, status, message)
                } finally {
                    Shaderc.shaderc_result_release(result)
                }
            } finally {
                Shaderc.shaderc_compile_options_release(options)
            }
        } finally {
            Shaderc.shaderc_compiler_release(compiler)
        }
    }
}
