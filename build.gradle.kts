import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.0"
    application
}

group = "io.arc.vk"
version = "0.1.0"

// 只考虑桌面端：Java 25 + LWJGL 3.4.2 Vulkan 栈
val lwjglVersion = "3.4.2"

// LWJGL 平台 native 坐标通用后缀（当前以 Linux/Desktop 为主）
val nativeSuffix: String = when (val os = System.getProperty("os.name").lowercase()) {
    in listOf("linux") -> "natives-linux"
    in listOf("mac os x", "darwin") -> "natives-macos"
    in listOf("windows") -> "natives-windows"
    else -> error("Unsupported OS: $os")
}

// 用当前 JVM（要求 JAVA_HOME 指向 Java 25）编译，目标字节码 25
java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

dependencies {
    // LWJGL BOM
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    // 核心：GLFW(窗口) + Vulkan 后端
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-vulkan")
    implementation("org.lwjgl:lwjgl-vma")      // Vulkan 内存分配
    implementation("org.lwjgl:lwjgl-shaderc")  // SPIR-V 编译（GLSL->SPIR-V；HLSL 需 DXC，见 README）
    implementation("org.lwjgl:lwjgl-stb")      // stb_image 等

    // 桌面端 native
    runtimeOnly("org.lwjgl:lwjgl::$nativeSuffix")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$nativeSuffix")
    runtimeOnly("org.lwjgl:lwjgl-vma::$nativeSuffix")
    runtimeOnly("org.lwjgl:lwjgl-shaderc::$nativeSuffix")
    runtimeOnly("org.lwjgl:lwjgl-stb::$nativeSuffix")
}

application {
    mainClass.set("io.arc.vk.ArcAppKt")
}
