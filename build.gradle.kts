@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("fabric-loom").version(Fabric.Loom.version)
    id("maven-publish")
}

group = Framed.group

base {
    archivesBaseName = Framed.name
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenLocal {
        name = "Maven Local"
    }
    maven(url = "https://maven.fabricmc.net/") {
        name = "Fabric"
    }
    maven(url = "https://server.bbkr.space/artifactory/libs-release") {
        name = "Cotton"
    }
    maven(url = "https://maven.dblsaiko.net/") {
        name = "dblsaiko"
    }
    maven(url = "https://cursemaven.com/") {
        name = "Curse Maven"
        content {
            includeGroup("curse.maven")
        }
    }
    maven(url = "https://jitpack.io/") {
        name = "JitPack"
    }
}

dependencies {
    fun ExternalModuleDependency?.excludeFabricApi() {
        this?.run {
            exclude(group = "net.fabricmc.fabric-api")
        }
    }

    minecraft(Minecraft.coordinate)
    mappings(Fabric.yarn.coordinate)

    modImplementation(Fabric.loader.coordinate)
    modImplementation(Fabric.api.coordinate)

    implementation(Gson.coordinate)

    include(modImplementation(LibGui.coordinate) { excludeFabricApi() })

    compileOnly(Jsr305.coordinate)

    include(modImplementation(Frex.coordinate) {
        excludeFabricApi()
        isTransitive = false
    })
    include(modImplementation(Jmx.coordinate) {
        excludeFabricApi()
        isTransitive = false
    })

    modRuntime(Canvas.coordinate)
    modRuntime(Couplings.coordinate)
}

tasks.getByName<ProcessResources>("processResources") {
    inputs.property("version", Framed.version)
    filesMatching("fabric.mod.json") {
        expand("version" to Framed.version)
    }
}

val remapJar = tasks.getByName<RemapJarTask>("remapJar")

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = Framed.name

        artifact(remapJar) {
            classifier = null
            builtBy(remapJar)
        }
    }
}
