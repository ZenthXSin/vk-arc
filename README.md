# vk-arc — Mindustry/Arc 的 Vulkan 重写骨架（step1）

目标：把 Arc 引擎（原基于 LibGDX + OpenGL）与 Mindustry 重写为现代 Vulkan 引擎。

## 技术栈（已按需求锁定并验证）
- 仅桌面端；Java **25**（`/usr/lib/jvm/java-25-openjdk-amd64`）
- LWJGL **3.4.2**（BOM 管理坐标）
  - `lwjgl-glfw` 窗口/输入（取代 Arc 的 SDL 后端，GLFW client API 设为 `GLFW_NO_API`）
  - `lwjgl-vulkan` Vulkan 后端（取代 GL20/GL30）
  - `lwjgl-vma` Vulkan 内存分配器
  - `lwjgl-shaderc` SPIR-V 编译
  - `lwjgl-stb` 图片/纹理加载（取代 Arc 的 Pixmap/Buffers）
- 着色器语言 **HLSL**；`libshaderc` 走 HLSL→GLSL→SPIR-V 路径（已实测通过）
- 重写部分全部 **Kotlin 2.4.0**；**禁止 java.awt**

## step1 已交付（本次）
- Gradle 工程骨架：`settings.gradle.kts` / `build.gradle.kts`（JVM 25 + Kotlin 2.4.0）
- `Window` — GLFW 窗口封装，`GLFW_NO_API`
- `VkEngine` — Vulkan 实例(instance)创建与物理设备枚举骨架
- `ShaderCompiler` — Shaderc 封装，GLSL/HLSL → SPIR-V（含编译错误信息）
- `resources/shaders/tri.{vert,frag}.hlsl` — 基础三角形 HLSL 着色器
- `ArcApp` — 入口；启动时自检着色器编译，headless 环境缺 GLFW 平台时优雅报错

## 已实测验证
- `./gradlew build` 通过（Java 25 + Kotlin 2.4.0，无编译错误）
- HLSL→SPIR-V 运行时编译成功：`vert -> 740 bytes`、`frag -> 460 bytes`
- 本机为 headless 服务器（无 DISPLAY / 无 Vulkan ICD），GLFW/Vulkan 交换链**无法在本机运行**，需在有 Vulkan 驱动的桌面机跑 `./gradlew run`

## 依赖原生库注意
LWJGL 自动通过 `installDist` 带 `natives-linux` jar。若 `libshaderc` 初始化加载失败，需确保系统存在其依赖（通常 `libLLVM` 等）；本机实测正常。

## 路线（后续 step）
- step2：交换链 / 渲染通道 / 图形管线 / 命令缓冲（三角形成像）
- step3：VMA 分配器接入 + Mesh/Batch（取代 arc g2d Batch）
- step4：Pixmap→STB 纹理加载管线、Font、输入/相机（对标 arc graphics/input）
- step5：Mindustry core 移植到新渲染接口
