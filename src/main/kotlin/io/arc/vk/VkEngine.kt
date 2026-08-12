package io.arc.vk

import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.vulkan.VK10
import org.lwjgl.vulkan.VK13
import org.lwjgl.vulkan.VkApplicationInfo
import org.lwjgl.vulkan.VkInstance
import org.lwjgl.vulkan.VkInstanceCreateInfo

/**
 * Vulkan 引擎上下文骨架。
 *
 * step1：实例(instance) 与物理设备探测。
 * 说明：本仓库在无显示/无 GPU 的 headless 机器上只能做“编译+逻辑验证”，
 * 完整的交换链/渲染通道（step2）在本机无法运行，需在有 Vulkan 驱动的桌面机上执行。
 */
class VkEngine(private val window: Window) {

    private var instance: VkInstance? = null
    private var debugMessenger: Long = MemoryUtil.NULL

    fun init() {
        MemoryStack.stackPush().use { stack ->
            // 实例创建信息
            val appInfo = VkApplicationInfo.calloc(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(stack.UTF8("vk-arc"))
                .applicationVersion(VK10.VK_MAKE_API_VERSION(0, 0, 1, 0))
                .pEngineName(stack.UTF8("arc-vk"))
                .engineVersion(VK10.VK_MAKE_API_VERSION(0, 0, 1, 0))
                .apiVersion(VK13.VK_API_VERSION_1_3)

            // 需要 GLFW 暴露的 surface 扩展
            val glfwExtensions: PointerBuffer? = org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions()
                ?: error("GLFW 未检测到 Vulkan 支持（headless/无 GLFW Vulkan loader）")

            val createInfo = VkInstanceCreateInfo.calloc(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pApplicationInfo(appInfo)
                .ppEnabledExtensionNames(glfwExtensions)

            val pInstance = stack.callocPointer(1)
            val result = VK10.vkCreateInstance(createInfo, null, pInstance)
            check(result == VK10.VK_SUCCESS) {
                "vkCreateInstance 失败，VkResult=$result。" +
                    "（headless/无 Vulkan 驱动环境会在此失败，需桌面 GPU 环境）"
            }
            instance = VkInstance(pInstance.get(0), createInfo)
            debugMessenger = MemoryUtil.NULL
        }
    }

    /** 枚举可用物理设备数量（仅验证 API 是否就绪）。 */
    fun physicalDeviceCount(): Int {
        val inst = instance ?: return 0
        MemoryStack.stackPush().use { stack ->
            val pCount = stack.mallocInt(1)
            check(VK10.vkEnumeratePhysicalDevices(inst, pCount, null) == VK10.VK_SUCCESS)
            return pCount.get(0)
        }
    }

    fun dispose() {
        val inst = instance
        if (inst != null) {
            if (debugMessenger != MemoryUtil.NULL) {
                // 此处省略 debug messenger 销毁（占位）
            }
            VK10.vkDestroyInstance(inst, null)
            instance = null
        }
    }
}
