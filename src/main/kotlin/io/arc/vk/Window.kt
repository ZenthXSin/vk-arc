package io.arc.vk

import org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR
import org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR
import org.lwjgl.glfw.GLFW.GLFW_CLIENT_API
import org.lwjgl.glfw.GLFW.GLFW_NO_API
import org.lwjgl.glfw.GLFW.glfwCreateWindow
import org.lwjgl.glfw.GLFW.glfwDefaultWindowHints
import org.lwjgl.glfw.GLFW.glfwDestroyWindow
import org.lwjgl.glfw.GLFW.glfwWindowHint
import org.lwjgl.glfw.GLFW.GLFW_TRUE
import org.lwjgl.glfw.GLFW.GLFW_RESIZABLE

/**
 * 极简窗口封装。替代 Arc 的 SdlApplication / SdlWindow。
 *
 * Vulkan 后端下 GLFW 窗口不需要任何 GL 上下文，因此 client API 设为 GLFW_NO_API。
 */
class Window(val handle: Long, val width: Int, val height: Int) {
    companion object {
        fun create(width: Int, height: Int, title: String): Window {
            glfwDefaultWindowHints()
            // Vulkan 渲染，不创建 OpenGL 上下文
            glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
            // Vulkan 不需要也不应该设置 GL 上下文版本，仅标注意图
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 0)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0)

            val handle = glfwCreateWindow(width, height, title, 0L, 0L)
            if (handle == 0L) error("GLFW 窗口创建失败")
            return Window(handle, width, height)
        }
    }

    fun destroy() = glfwDestroyWindow(handle)
}
