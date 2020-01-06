package dev.alexnader.framity.model

import dev.alexnader.framity.adapters.tag
import dev.alexnader.framity.util.maybe
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.util.math.Direction

fun QuadEmitter.nothing() = this

fun QuadEmitter.side(dir: Direction, actions: QuadEmitter.() -> QuadEmitter): QuadEmitter =
    this.tag(dir.tag)
        .actions()
        .emit()

fun QuadEmitter.culledSide(dir: Direction, pos: Float, cullPredicate: (Float) -> Boolean, actions: QuadEmitter.() -> QuadEmitter) =
    this.side(dir) {
        actions()
        maybe(cullPredicate(pos)) {
            cullFace(dir)
        }
    }

fun QuadEmitter.down(y: Float, actions: QuadEmitter.() -> QuadEmitter) =
    this.culledSide(Direction.DOWN, y, { it == 0f }, actions)
fun QuadEmitter.up(y: Float, actions: QuadEmitter.() -> QuadEmitter) =
    this.culledSide(Direction.UP, y, { it == 1f }, actions)
fun QuadEmitter.north(z: Float, actions: QuadEmitter.() -> QuadEmitter) =
    this.culledSide(Direction.NORTH, z, { it == 0f }, actions)
fun QuadEmitter.east(x: Float, actions: QuadEmitter.() -> QuadEmitter) =
    this.culledSide(Direction.EAST, x, { it == 1f }, actions)
fun QuadEmitter.south(z: Float, actions: QuadEmitter.() -> QuadEmitter) =
    this.culledSide(Direction.SOUTH, z, { it == 1f }, actions)
fun QuadEmitter.west(x: Float, actions: QuadEmitter.() -> QuadEmitter) =
    this.culledSide(Direction.WEST, x, { it == 0f }, actions)

fun QuadEmitter.color(color: Int): QuadEmitter = this.spriteColor(0, color, color, color, color)

fun QuadEmitter.downTriangle(y: Float, color: Int, x1: Float, z1: Float, x2: Float, z2: Float, x3: Float, z3: Float) =
    this.down(y) {
        color(color)
        pos(0, x1, y, z1)
        pos(1, x2, y, z2)
        pos(2, x3, y, z3)
        pos(3, x3, y, z3)
    }

fun QuadEmitter.upTriangle(y: Float, color: Int, x1: Float, z1: Float, x2: Float, z2: Float, x3: Float, z3: Float) =
    this.up(y) {
        color(color)
        pos(0, x1, y, z1)
        pos(1, x2, y, z2)
        pos(2, x3, y, z3)
        pos(3, x3, y, z3)
    }

fun QuadEmitter.northTriangle(z: Float, color: Int, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) =
    this.north(z) {
        color(color)
        pos(0, x1, y1, z)
        pos(1, x2, y2, z)
        pos(2, x3, y3, z)
        pos(3, x3, y3, z)
    }

fun QuadEmitter.eastTriangle(x: Float, color: Int, z1: Float, y1: Float, z2: Float, y2: Float, z3: Float, y3: Float) =
    this.east(x) {
        color(color)
        pos(0, x, y1, z1)
        pos(1, x, y2, z2)
        pos(2, x, y3, z3)
        pos(3, x, y3, z3)
    }

fun QuadEmitter.southTriangle(z: Float, color: Int, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) =
    this.south(z) {
        color(color)
        pos(0, x1, y1, z)
        pos(1, x2, y2, z)
        pos(2, x3, y3, z)
        pos(3, x3, y3, z)
    }

fun QuadEmitter.westTriangle(x: Float, color: Int, z1: Float, y1: Float, z2: Float, y2: Float, z3: Float, y3: Float) =
    this.west(x) {
        color(color)
        pos(0, x, y1, z1)
        pos(1, x, y2, z2)
        pos(2, x, y3, z3)
        pos(3, x, y3, z3)
    }

fun QuadEmitter.downSquare(y: Float, color: Int, x1: Float, z1: Float, x2: Float, z2: Float) =
    this.down(y) {
        color(color)
        pos(0, x1, y, z1)
        pos(1, x2, y, z1)
        pos(2, x2, y, z2)
        pos(3, x1, y, z2)
    }

fun QuadEmitter.upSquare(y: Float, color: Int, x1: Float, z1: Float, x2: Float, z2: Float) =
    this.up(y) {
        color(color)
        pos(0, x1, y, z2)
        pos(1, x2, y, z2)
        pos(2, x2, y, z1)
        pos(3, x1, y, z1)
    }

fun QuadEmitter.northSquare(z: Float, color: Int, x1: Float, y1: Float, x2: Float, y2: Float) =
    this.north(z) {
        color(color)
        pos(0, x1, y1, z)
        pos(1, x1, y2, z)
        pos(2, x2, y2, z)
        pos(3, x2, y1, z)
    }

fun QuadEmitter.eastSquare(x: Float, color: Int, z1: Float, y1: Float, z2: Float, y2: Float) =
    this.east(x) {
        color(color)
        pos(0, x, y2, z1)
        pos(1, x, y2, z2)
        pos(2, x, y1, z2)
        pos(3, x, y1, z1)
    }

fun QuadEmitter.southSquare(z: Float, color: Int, x1: Float, y1: Float, x2: Float, y2: Float) =
    this.south(z) {
        color(color)
        pos(0, x2, y1, z)
        pos(1, x2, y2, z)
        pos(2, x1, y2, z)
        pos(3, x1, y1, z)
    }

fun QuadEmitter.westSquare(x: Float, color: Int, z1: Float, y1: Float, z2: Float, y2: Float) =
    this.west(x) {
        color(color)
        pos(0, x, y1, z1)
        pos(1, x, y1, z2)
        pos(2, x, y2, z2)
        pos(3, x, y2, z1)
    }

fun QuadEmitter.nonCulledQuad(
    color: Int, dir: Direction,
    x1: Float, y1: Float, z1: Float,
    x2: Float, y2: Float, z2: Float,
    x3: Float, y3: Float, z3: Float,
    x4: Float, y4: Float, z4: Float
) =
    this.side(dir) {
        color(color)
        pos(0, x1, y1, z1)
        pos(1, x2, y2, z2)
        pos(2, x3, y3, z3)
        pos(3, x4, y4, z4)
    }

fun QuadEmitter.nonCulledTri(
    color: Int, dir: Direction,
    x1: Float, y1: Float, z1: Float,
    x2: Float, y2: Float, z2: Float,
    x3: Float, y3: Float, z3: Float
) =
    this.side(dir) {
        color(color)
        pos(0, x1, y1, z1)
        pos(1, x2, y2, z2)
        pos(2, x3, y3, z3)
        pos(3, x3, y3, z3)
    }

fun QuadEmitter.cube16(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Int) =
    this.cube(x1 / 16f, y1 / 16f, z1 / 16f, x2 / 16f, y2 / 16f, z2 / 16f, color)

fun QuadEmitter.cube(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Int) =
    this.downSquare(y1, color, x1, z1, x2, z2)
        .upSquare(y2, color, x1, z1, x2, z2)
        .northSquare(z1, color, x1, y1, x2, y2)
        .eastSquare(x2, color, z1, y1, z2, y2)
        .southSquare(z2, color, x1, y1, x2, y2)
        .westSquare(x1, color, z1, y1, z2, y2)
