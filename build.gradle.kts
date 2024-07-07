import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.architectury.plugin)
    alias(libs.plugins.architectury.loom).apply(false)
    alias(libs.plugins.shadow).apply(false)
}

val minecraftVersion = libs.versions.minecraft.version.get()
val enabledPlatforms: String by rootProject
val fabricLoaderVersion: String by rootProject
val architecturyVersion: String by rootProject
val fabricApiVersion: String by rootProject

architectury {
    minecraft = minecraftVersion
}

val archivesName: String by rootProject
val modVersion: String by rootProject
val mavenGroup: String by rootProject

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    base.archivesName = archivesName
    version =  modVersion
    group = mavenGroup

    repositories {
        maven {
            name = "fabric MC"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "architectury"
            url = uri("https://maven.architectury.dev/")
        }
        maven {
            name = "forge"
            url = uri("https://maven.minecraftforge.net/")
        }
        maven {
            name = "parchment maven"
            url = uri("https://maven.parchmentmc.org/")
        }
    }
    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release.set(21)
        }
        java {
            withSourcesJar()
        }
    }
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "com.github.johnrengelman.shadow")
    configure<LoomGradleExtensionAPI> {
        silentMojangMappingsLicense()
    }
    dependencies {
        "minecraft"("com.mojang:minecraft:${minecraftVersion}")

        "mappings"(project.the<LoomGradleExtensionAPI>().layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${minecraftVersion}:2024.06.23@zip")
        })

    }

    dependencies {
        "modImplementation"("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
        "modApi"("dev.architectury:architectury${if (project.name == "common") "" else "-${project.name}"}:${architecturyVersion}")
    }

    when(project.name) {
        "common" -> {
            architectury {
                common(enabledPlatforms.split(","))
            }
            dependencies {

            }
        }
        "fabric" -> {
            architectury {
                platformSetupLoomIde()
                fabric()
            }


        }
        "forge" -> {

        }
        "neoforge" -> {

        }

    }


    tasks {
        jar {
            archiveClassifier.set("dev")
        }
        named<RemapJarTask>("remapJar") {
            archiveClassifier.set(null as String?)
        }

        processResources {
            inputs.property("version", project.version);
            filesMatching(listOf("META-INF/mods.toml", "fabric.mods.toml")) {
                expand("version" to project.version)
            }
        }
    }
    publishing {
        publications {
            create<MavenPublication>("${project.name}Maven") {
                artifactId = "${archivesName}-${project.name}-${minecraftVersion}"
                from(components["java"])
            }
        }
    }

    sourceSets {
        main {
            java {
                setSrcDirs(setOf(rootProject.file("src/${project.name}/java")))
            }
            resources {
                setSrcDirs(setOf(rootProject.file("src/${project.name}/resources")))
            }
        }
        test {
            java {
                setSrcDirs(HashSet<File>())
            }
            resources {
                setSrcDirs(HashSet<File>())
            }
        }
    }
}