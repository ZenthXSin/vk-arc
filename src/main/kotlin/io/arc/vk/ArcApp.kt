package io.arc.vk

import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFW.glfwInit
import org.lwjgl.glfw.GLFW.glfwTerminate
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFW.glfwDestroyWindow

/**
 * 桌面端入口。
 *
 * 重写路线（step1：引擎骨架）：
 *  - GLFW 取代 Arc 的 SDL 后端（窗口/输入）
 *  - Vulkan + VMA 取代 Arc 的 GL20/GL30 渲染后端
 *  - libshaderc 把 HLSL/GLSL 编译成 SPIR-V（见 [ShaderCompiler]）
 *  - 禁止 java.awt；重写部分全部 Kotlin 2.4.0
 */
fun main() {
    // GLFW 错误回调必须保留，否则 LWJGL-GLFW 会直接抛 IllegalStateException
    GLFWErrorCallback.createPrint(System.err).set()

    selfTestShaders()

    if (!glfwInit()) {
        error("无法初始化 GLFW（本机若缺图形环境，Vulkan 交换链创建会在后续失败）")
    }

    val window = Window.create(1280, 720, "vk-arc")
    val engine = VkEngine(window)

    try {
        engine.init()
        println("physical device count = ${engine.physicalDeviceCount()}")
        // 主循环：无图形环境（headless CI）时 windowShouldClose 会立刻为真
        while (!glfwWindowShouldClose(window.handle)) {
            glfwPollEvents()
        }
    } catch (t: Throwable) {
        System.err.println("!! vk-arc 引擎初始化失败: $t")
        t.printStackTrace()
    } finally {
        engine.dispose()
        glfwDestroyWindow(window.handle)
        glfwTerminate()
    }
}

/** 启动自检：把 resources/shaders 下的 HLSL 编译成 SPIR-V，验证 Shaderc 链路可用。 */
private fun selfTestShaders() {
    fun load(name: String): String {
        val data = checkNotNull(ShaderCompiler::class.java.getResourceAsStream("/shaders/$name")) {
            "缺少内置着色器 resources/shaders/$name"
        }
        return data.readBytes().toString(Charsets.UTF_8)
    }

    val vert = ShaderCompiler.compile(load("tri.vert.hlsl"), ShaderCompiler.Stage.VERTEX)
    val frag = ShaderCompiler.compile(load("tri.frag.hlsl"), ShaderCompiler.Stage.FRAGMENT)
    for ((label, r) in listOf("vert" to vert, "frag" to frag)) {
        if (r.ok) println("[shader] $label -> SPIR-V ${r.spirv!!.remaining().toLong()} bytes")
        else println("[shader] $label 编译失败: ${r.message}")
    }
}
