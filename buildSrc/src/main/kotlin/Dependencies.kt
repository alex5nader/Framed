@file:Suppress("MemberVisibilityCanBePrivate")

open class Dependency(val group: String, val name: String, val version: String, val transitive: Boolean = true) {
    val coordinate: String get() = "$group:$name:$version"

    override fun toString(): String = coordinate
}

open class CurseMavenDependency(descriptor: String, projectId: Int, fileId: Int, transitive: Boolean = true) : Dependency("curse.maven", "$descriptor-$projectId", "$fileId", transitive)

object Fabric {
    object Loom {
        const val version = "0.5-SNAPSHOT"
    }

    val yarn = Dependency("net.fabricmc", "yarn", "1.16.4+build.7:v2")
    val loader = Dependency("net.fabricmc", "fabric-loader", "0.10.8")
    val api = Dependency("net.fabricmc.fabric-api", "fabric-api", "0.28.4+1.16")
}

val Framed = Dependency("dev.alexnader", "framed", "1.0")

object Minecraft : Dependency("com.mojang", "minecraft", "1.16.4") {
    const val tag = "mc116"
}

val Gson = Dependency("com.google.code.gson", "gson", "2.8.6")
val Jsr305 = Dependency("com.google.code.findbugs", "jsr305", "3.0.2")

val LibGui = Dependency("io.github.cottonmc", "LibGui", "3.3.2+${Minecraft.version}")

val Frex = Dependency("grondag", "frex-${Minecraft.tag}", "4.2.183", transitive = false)
val Jmx = Dependency("grondag", "jmx-${Minecraft.tag}", "1.19.168", transitive = false)
val Canvas = Dependency("grondag", "canvas-${Minecraft.tag}", "1.0.1262")

val Couplings = CurseMavenDependency("couplings", 395659, 3111745)

val jijDeps = listOf(LibGui, Frex, Jmx)
val deps = listOf(Fabric.loader, Fabric.api) + jijDeps
val runtimeDeps = listOf(Canvas, Couplings)
