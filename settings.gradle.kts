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

}