//package dev.alexnader.framity.model
//
//import dev.alexnader.framity.adapters.tag
//import dev.alexnader.framity.util.maybe
//import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
//import net.minecraft.util.math.Direction
//
///**
// * Emits nothing.
// */
//fun QuadEmitter.nothing() = this
//
///**
// * Scope function which auto-tags as [dir] and auto-emits whatever is defined via [actions].
// */
//fun QuadEmitter.side(dir: Direction, actions: QuadEmitter.() -> QuadEmitter): QuadEmitter =
//    this.tag(dir.tag)
//        .actions()
//        .emit()
//
///**
// * Equivalent to [side] but also possibly culls a side based on [pos] and [cullPredicate].
// */
//fun QuadEmitter.culledSide(dir: Direction, pos: Float, cullPredicate: (Float) -> Boolean, actions: QuadEmitter.() -> QuadEmitter) =
//    this.side(dir) {
//        actions()
//        maybe(cullPredicate(pos)) {
//            cullFace(dir)
//        }
//    }
//
///**
// * Wrapper of [culledSide] for [Direction.DOWN].
// */
//fun QuadEmitter.down(y: Float, actions: QuadEmitter.() -> QuadEmitter) =
//    this.culledSide(Direction.DOWN, y, { it == 0f }, actions)
///**
// * Wrapper of [culledSide] for [Direction.UP].
// */
//fun QuadEmitter.up(y: Float, actions: QuadEmitter.() -> QuadEmitter) =
//    this.culledSide(Direction.UP, y, { it == 1f }, actions)
///**
// * Wrapper of [culledSide] for [Direction.NORTH].
// */
//fun QuadEmitter.north(z: Float, actions: QuadEmitter.() -> QuadEmitter) =
//    this.culledSide(Direction.NORTH, z, { it == 0f }, actions)
///**
// * Wrapper of [culledSide] for [Direction.EAST].
// */
//fun QuadEmitter.east(x: Float, actions: QuadEmitter.() -> QuadEmitter) =
//    this.culledSide(Direction.EAST, x, { it == 1f }, actions)
///**
// * Wrapper of [culledSide] for [Direction.SOUTH].
// */
//fun QuadEmitter.south(z: Float, actions: QuadEmitter.() -> QuadEmitter) =
//    this.culledSide(Direction.SOUTH, z, { it == 1f }, actions)
///**
// * Wrapper of [culledSide] for [Direction.WEST].
// */
//fun QuadEmitter.west(x: Float, actions: QuadEmitter.() -> QuadEmitter) =
//    this.culledSide(Direction.WEST, x, { it == 0f }, actions)
//
///**
// * Wrapper for [QuadEmitter.spriteColor] which applies [color] to index 0 in all channels.
// */
//fun QuadEmitter.color(color: Int): QuadEmitter = this.spriteColor(0, color, color, color, color)
//
///**
// * Emits a down-face triangle with [color] along ([x1], [z1]), ([x2], [z2]), ([x3], [z3]) at [y].
// */
//fun QuadEmitter.downTriangle(y: Float, color: Int, x1: Float, z1: Float, x2: Float, z2: Float, x3: Float, z3: Float) =
//    this.down(y) {
//        color(color)
//        pos(0, x1, y, z1)
//        pos(1, x2, y, z2)
//        pos(2, x3, y, z3)
//        pos(3, x3, y, z3)
//    }
//
///**
// * Emits a up-face triangle with [color] along ([x1], [z1]), ([x2], [z2]), ([x3], [z3]) at [y].
// */
//fun QuadEmitter.upTriangle(y: Float, color: Int, x1: Float, z1: Float, x2: Float, z2: Float, x3: Float, z3: Float) =
//    this.up(y) {
//        color(color)
//        pos(0, x1, y, z1)
//        pos(1, x2, y, z2)
//        pos(2, x3, y, z3)
//        pos(3, x3, y, z3)
//    }
//
///**
// * Emits a north-face triangle with [color] along ([x1], [y1]), ([x2], [y2]), ([x3], [y3]) at [z].
// */
//fun QuadEmitter.northTriangle(z: Float, color: Int, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) =
//    this.north(z) {
//        color(color)
//        pos(0, x1, y1, z)
//        pos(1, x2, y2, z)
//        pos(2, x3, y3, z)
//        pos(3, x3, y3, z)
//    }
//
///**
// * Emits a east-face triangle with [color] along ([z1], [y1]), ([z2], [y2]), ([z3], [y3]) at [x].
// */
//fun QuadEmitter.eastTriangle(x: Float, color: Int, z1: Float, y1: Float, z2: Float, y2: Float, z3: Float, y3: Float) =
//    this.east(x) {
//        color(color)
//        pos(0, x, y1, z1)
//        pos(1, x, y2, z2)
//        pos(2, x, y3, z3)
//        pos(3, x, y3, z3)
//    }
//
///**
// * Emits a south-face triangle with [color] along ([x1], [y1]), ([x2], [y2]), ([x3], [y3]) at [z].
// */
//fun QuadEmitter.southTriangle(z: Float, color: Int, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) =
//    this.south(z) {
//        color(color)
//        pos(0, x1, y1, z)
//        pos(1, x2, y2, z)
//        pos(2, x3, y3, z)
//        pos(3, x3, y3, z)
//    }
//
///**
// * Emits a west-face triangle with [color] along ([z1], [y1]), ([z2], [y2]), ([z3], [y3]) at [x].
// */
//fun QuadEmitter.westTriangle(x: Float, color: Int, z1: Float, y1: Float, z2: Float, y2: Float, z3: Float, y3: Float) =
//    this.west(x) {
//        color(color)
//        pos(0, x, y1, z1)
//        pos(1, x, y2, z2)
//        pos(2, x, y3, z3)
//        pos(3, x, y3, z3)
//    }
//
///**
// * Emits a down-face rect with [color] along ([x1], [z1]), ([x2], [z2]) at [y].
// */
//fun QuadEmitter.downRect(y: Float, color: Int, x1: Float, z1: Float, x2: Float, z2: Float) =
//    this.down(y) {
//        color(color)
//        pos(0, x1, y, z1)
//        pos(1, x2, y, z1)
//        pos(2, x2, y, z2)
//        pos(3, x1, y, z2)
//    }
//
///**
// * Emits a up-face rect with [color] along ([x1], [z1]), ([x2], [z2]) at [y].
// */
//fun QuadEmitter.upRect(y: Float, color: Int, x1: Float, z1: Float, x2: Float, z2: Float) =
//    this.up(y) {
//        color(color)
//        pos(0, x1, y, z2)
//        pos(1, x2, y, z2)
//        pos(2, x2, y, z1)
//        pos(3, x1, y, z1)
//    }
//
///**
// * Emits a north-face rect with [color] along ([x1], [y1]), ([x2], [y2]) at [z].
// */
//fun QuadEmitter.northRect(z: Float, color: Int, x1: Float, y1: Float, x2: Float, y2: Float) =
//    this.north(z) {
//        color(color)
//        pos(0, x1, y1, z)
//        pos(1, x1, y2, z)
//        pos(2, x2, y2, z)
//        pos(3, x2, y1, z)
//    }
//
///**
// * Emits a east-face rect with [color] along ([z1], [y1]), ([z2], [y2]) at [x].
// */
//fun QuadEmitter.eastRect(x: Float, color: Int, z1: Float, y1: Float, z2: Float, y2: Float) =
//    this.east(x) {
//        color(color)
//        pos(0, x, y2, z1)
//        pos(1, x, y2, z2)
//        pos(2, x, y1, z2)
//        pos(3, x, y1, z1)
//    }
//
///**
// * Emits a south-face rect with [color] along ([x1], [y1]), ([x2], [y2]) at [z].
// */
//fun QuadEmitter.southRect(z: Float, color: Int, x1: Float, y1: Float, x2: Float, y2: Float) =
//    this.south(z) {
//        color(color)
//        pos(0, x2, y1, z)
//        pos(1, x2, y2, z)
//        pos(2, x1, y2, z)
//        pos(3, x1, y1, z)
//    }
//
///**
// * Emits a west-face rect with [color] along ([z1], [y1]), ([z2], [y2]) at [x].
// */
//fun QuadEmitter.westRect(x: Float, color: Int, z1: Float, y1: Float, z2: Float, y2: Float) =
//    this.west(x) {
//        color(color)
//        pos(0, x, y1, z1)
//        pos(1, x, y1, z2)
//        pos(2, x, y2, z2)
//        pos(3, x, y2, z1)
//    }
//
///**
// * Emits a non-culled quad with [color] and [dir] along
// * ([x1], [y1], [z1]), ([x2], [y2], [z2]), ([x3], [y3], [z3]), ([x4], [y4], [z4]).
// */
//fun QuadEmitter.nonCulledQuad(
//    color: Int, dir: Direction,
//    x1: Float, y1: Float, z1: Float,
//    x2: Float, y2: Float, z2: Float,
//    x3: Float, y3: Float, z3: Float,
//    x4: Float, y4: Float, z4: Float
//) =
//    this.side(dir) {
//        color(color)
//        pos(0, x1, y1, z1)
//        pos(1, x2, y2, z2)
//        pos(2, x3, y3, z3)
//        pos(3, x4, y4, z4)
//    }
//
///**
// * Emits a non-tri quad with [color] and [dir] along
// * ([x1], [y1], [z1]), ([x2], [y2], [z2]), ([x3], [y3], [z3]).
// */
//fun QuadEmitter.nonCulledTri(
//    color: Int, dir: Direction,
//    x1: Float, y1: Float, z1: Float,
//    x2: Float, y2: Float, z2: Float,
//    x3: Float, y3: Float, z3: Float
//) =
//    this.side(dir) {
//        color(color)
//        pos(0, x1, y1, z1)
//        pos(1, x2, y2, z2)
//        pos(2, x3, y3, z3)
//        pos(3, x3, y3, z3)
//    }
//
///**
// * Emits a cube along with [color] ([x1], [y1], [z1]), ([x2], [y2], [z2]).
// * Coordinates should be in range [0, 16]
// */
//fun QuadEmitter.cube16(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Int) =
//    this.cube(x1 / 16f, y1 / 16f, z1 / 16f, x2 / 16f, y2 / 16f, z2 / 16f, color)
//
///**
// * Emits a cube along with [color] ([x1], [y1], [z1]), ([x2], [y2], [z2]).
// * Coordinates should be in range [0, 1]
// */
//fun QuadEmitter.cube(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Int) =
//    this.downRect(y1, color, x1, z1, x2, z2)
//        .upRect(y2, color, x1, z1, x2, z2)
//        .northRect(z1, color, x1, y1, x2, y2)
//        .eastRect(x2, color, z1, y1, z2, y2)
//        .southRect(z2, color, x1, y1, x2, y2)
//        .westRect(x1, color, z1, y1, z2, y2)
