@file:Suppress("MemberVisibilityCanBePrivate", "unused")

open class Dependency(
    val group: String,
    val name: String,
    val version: String,
    val transitive: Boolean = true,

    curseforgeSlug: String? = null,
    val referenceOnCurseforge: Boolean = true
) {
    val curseforgeSlug = curseforgeSlug ?: name

    val coordinate: String get() = "$group:$name:$version"

    override fun toString(): String = coordinate
}

open class CurseMavenDependency(
    descriptor: String,
    projectId: Int,
    fileId: Int,
    transitive: Boolean = true,

    curseforgeSlug: String? = null,
    referenceOnCurseforge: Boolean = true
) : Dependency("curse.maven", "$descriptor-$projectId", "$fileId", transitive, curseforgeSlug, referenceOnCurseforge)

object Fabric {
    object Loom {
        const val version = "0.5-SNAPSHOT"
    }

    val yarn = Dependency("net.fabricmc", "yarn", "1.16.4+build.7:v2")
    val loader = Dependency("net.fabricmc", "fabric-loader", "0.10.8", referenceOnCurseforge = false)
    val api = Dependency("net.fabricmc.fabric-api", "fabric-api", "0.32.5+1.16")
}

object Framed : Dependency("dev.alexnader", "framed", "1.1") {
    const val modrinthId = "Ix9gggiE"
    const val curseforgeId = "356723"
}

object Minecraft : Dependency("com.mojang", "minecraft", "1.16.4") {
    const val tag = "mc116"
}

val Gson = Dependency("com.google.code.gson", "gson", "2.8.6")
val Jsr305 = Dependency("com.google.code.findbugs", "jsr305", "3.0.2")

val LibGui = Dependency("io.github.cottonmc", "LibGui", "3.3.2+${Minecraft.version}", curseforgeSlug = "libgui")

val Frex = Dependency("grondag", "frex-${Minecraft.tag}", "4.5.201", transitive = false, curseforgeSlug = "frex")
val Jmx = Dependency("grondag", "jmx-${Minecraft.tag}", "1.20.179", transitive = false, curseforgeSlug = "jmx")
val Canvas = Dependency("grondag", "canvas-${Minecraft.tag}", "1.0.1267", curseforgeSlug = "canvas-renderer")

val Conrad = Dependency("dev.inkwell", "conrad", "0.1.0")

val ModMenu = Dependency("com.terraformersmc", "modmenu", "1.16.8")
val Couplings = CurseMavenDependency("couplings", 395659, 3111745)

val jijDeps = listOf(LibGui, Frex, Jmx)
val deps = listOf(Fabric.loader, Fabric.api, Conrad) + jijDeps
val runtimeDeps = listOf(Canvas, Couplings, ModMenu)
