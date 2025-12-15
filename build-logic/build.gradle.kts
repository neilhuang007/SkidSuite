plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))

    // ⭐ 必须：提供 org.apache.tools.zip.*
    implementation("org.apache.ant:ant:1.10.14")
    // 这里用插件依赖是OK的，因为 build-logic 会作为“插件工程”参与解析
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.0.0-beta12")
}
