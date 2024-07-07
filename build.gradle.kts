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

val minecraftVersion: String by rootProject
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
        processResources {
            inputs.property("version", project.version);
            filesMatching(listOf("META-INF/mods.toml", "fabric.mods.toml")) {
                expand("version" to project.version)
            }
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
    architectury {
        when(project.name) {
            "common" -> {
                common(enabledPlatforms.split(","))
            }
            "fabric" -> {
                platformSetupLoomIde()
                fabric()
            }
            "forge" -> {
                platformSetupLoomIde()
                forge()
            }
            "neoforge" -> {
                neoForge {
                    platformPackage = "forge"
                }
            }
        }
    }

    if (project.name != "common") {
        val common by configurations.creating
        val shadowCommon by configurations.creating
        val developmentFabric by configurations.getting

        configurations {
            compileClasspath.configure { extendsFrom(common) }
            runtimeClasspath.configure { extendsFrom(common) }
            developmentFabric.extendsFrom(common)
        }
    }


    dependencies {
        val minecraft by configurations.getting
        val mappings by configurations.getting
        val modImplementation by configurations.getting
        val modApi by configurations.getting
        minecraft("com.mojang:minecraft:${minecraftVersion}")

        mappings(project.the<LoomGradleExtensionAPI>().layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${minecraftVersion}:2024.06.23@zip")
        })

        if (!project.name.contains("forge")) {
            modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
        }
        modApi("dev.architectury:architectury${if (project.name == "common") "" else "-${project.name}"}:${architecturyVersion}")

        when(project.name) {
            "fabric" -> {
                modApi("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
            }
        }

        if (project.name != "common") {
            val common by configurations.getting
            val shadowCommon by configurations.getting
            common(project(path = ":common", configuration = "namedElements")) { setTransitive(false) }
            shadowCommon(project(path = ":common", configuration = "transformProductionFabric")) { setTransitive(false) }
        }

    }
    if(project.name != "common") {
        val shadowCommon by configurations.getting
        tasks {
            jar {
                archiveClassifier.set("dev")
            }

            val shadowJar = named<ShadowJar>("shadowJar") {
                if (project.name.contains("forge")) {
                    exclude("fabric.mod.json")
                }
                configurations = listOf(shadowCommon)
                archiveClassifier.set("dev-shadow")
            }

            named<RemapJarTask>("remapJar") {
                injectAccessWidener.set(true)
                inputFile.set(shadowJar.get().archiveFile)
                dependsOn(shadowJar.get())
                archiveClassifier.set(null as String?)
            }
            named<Jar>("sourcesJar") {
                val commonSourcesJar = project(":common").tasks.named<Jar>("sourcesJar").get()
                dependsOn(commonSourcesJar)
                from(commonSourcesJar.archiveFile.map {
                    zipTree(it)
                })
            }

        }
        components.getByName("java") {
            this as AdhocComponentWithVariants
            this.withVariantsFromConfiguration(project.configurations.getByName("shadowRuntimeElements")) {
                skip()
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