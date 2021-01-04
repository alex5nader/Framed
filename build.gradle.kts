@file:Suppress("UnstableApiUsage")

import java.util.Properties
import net.fabricmc.loom.task.RemapJarTask
import com.modrinth.minotaur.TaskModrinthUpload
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.CurseUploadTask
import com.matthewprenger.cursegradle.Options

plugins {
    java
    `java-library`
    id("fabric-loom").version(Fabric.Loom.version)

    `maven-publish`
    id("com.matthewprenger.cursegradle").version("1.4.0")
    id("com.modrinth.minotaur").version("1.1.0")
}

version = Framed.version
group = Framed.group

base.archivesBaseName = Framed.name

configure<JavaPluginConvention> {
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
    minecraft(Minecraft.coordinate)
    mappings(Fabric.yarn.coordinate)

    deps.forEach {
        modImplementation(it.coordinate) {
            if (it.group != Fabric.api.group) {
                exclude(group = Fabric.api.group)
            }
            if (!it.transitive) {
                isTransitive = false
            }
        }
    }

    jijDeps.forEach { include(it.coordinate) }
    runtimeDeps.forEach { modRuntime(it.coordinate) }

    implementation(Gson.coordinate)
    compileOnly(Jsr305.coordinate)
}

tasks.getByName<ProcessResources>("processResources") {
    inputs.properties("version" to Framed.version)
    filesMatching("fabric.mod.json") {
        expand(
            "version" to Framed.version,
            "fapiVersion" to ">=${Fabric.api.version}",
            "libguiVersion" to ">=${LibGui.version}",
            "jmxVersion" to ">=${Jmx.version}",
            "frexVersion" to ">=${Frex.version}"
        )
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

val jar = tasks.getByName<Jar>("jar")

val apiKeys by lazy {
    val input = file("apiKeys.properties").inputStream()
    Properties().apply { load(input) }
}

val publishModrinth = tasks.create<TaskModrinthUpload>("publishModrinth") {
    val modrinthApiKey: String by apiKeys

    token = modrinthApiKey
    projectId = Framed.modrinthId

    changelog = "See https://github.com/alex5nader/Framed/projects for changelogs"

    versionNumber = Framed.version
    versionName = "Version ${Framed.version}"
    releaseType = "release"

    uploadFile = remapJar

    addGameVersion(Minecraft.version)
    addLoader("fabric")
}

curseforge {
    val curseforgeApiKey: String by apiKeys

    apiKey = curseforgeApiKey

    project(closureOf<CurseProject> {
        id = Framed.curseforgeId

        mainArtifact(remapJar)

        releaseType = "release"

        addGameVersion(Minecraft.version)
        addGameVersion("Fabric")

        relations(closureOf<CurseRelation> {
            jijDeps.forEach { dep ->
                dep.curseforgeSlug
                    .takeIf { dep.referenceOnCurseforge }
                    ?.let {
                        embeddedLibrary(it)
                    }
            }
            deps.forEach { dep ->
                dep.curseforgeSlug
                    .takeIf { dep.referenceOnCurseforge }
                    ?.takeIf { dep !in jijDeps }
                    ?.let {
                        requiredDependency(it)
                    }
            }
            runtimeDeps.forEach { dep ->
                dep.curseforgeSlug
                    .takeIf { dep.referenceOnCurseforge }
                    ?.let {
                        optionalDependency(it)
                    }
            }
        })

        changelog = "See https://github.com/alex5nader/Framed/projects for changelogs"

        mainArtifact(
            file("${project.buildDir}/libs/${base.archivesBaseName}-$version.jar"),
            closureOf<CurseArtifact> {
                displayName = "Version $version"
            }
        )
    })

    options(closureOf<Options> {
        forgeGradleIntegration = false
        debug = true
    })
}

project.afterEvaluate {
    tasks.getByName<CurseUploadTask>("curseforge${Framed.curseforgeId}") {
        dependsOn(remapJar)
    }
}
