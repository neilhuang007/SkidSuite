plugins {
    `java-library`
}

fun DependencyHandlerScope.externalLib(libraryName: String) {
    implementation(files("${rootProject.rootDir}/libs/$libraryName.jar"))
}

dependencies {
    implementation("commons-io:commons-io")

    implementation("org.apache.commons:commons-configuration2")
    implementation("commons-beanutils:commons-beanutils")

    api("org.ow2.asm:asm-tree")
    implementation("org.ow2.asm:asm")
    implementation("org.ow2.asm:asm-analysis")
    implementation("org.ow2.asm:asm-util")
    implementation("org.ow2.asm:asm-commons")

    implementation("com.github.leibnitz27:cfr") { isChanging = true }
    implementation("org.vineflower:vineflower:1.10.1")
    implementation("ch.qos.logback:logback-classic")
    implementation("software.coley:cafedude-core:2.1.1")

//    externalLib("fernflower-15-05-20")
}
