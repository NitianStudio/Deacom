pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.architectury.dev/") }
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven {
            url = uri("https://maven.parchmentmc.org")
        }
        gradlePluginPortal()
    }
}

listOf("common", "fabric").forEach {
    include(it)
    val project = project(":${it}")
    project.name = it
    project.projectDir = file("build/${it}")
    if (it.contains("forge")) {
        val properties = File(project.projectDir, "gradle.properties")
        if (!properties.exists()) {
            properties.bufferedWriter(Charsets.UTF_8).use {
                it.write("loom.platform=${it}")
                it.close()
            }
        }
    }

}