package dev.alexnader.framity.model

import dev.alexnader.framity.util.maybe
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.util.math.Direction

fun QuadEmitter.cube(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: Int): QuadEmitter {
    this.tag(Tag.Down)
        .spriteColor(0, color, color, color, color)
        .pos(0, x1, y1, z1)
        .pos(1, x2, y1, z1)
        .pos(2, x2, y1, z2)
        .pos(3, x1, y1, z2)
        .maybe(y1 == 0f) {
            cullFace(Direction.DOWN)
        }
        .emit()

    this.tag(Tag.Up)
        .spriteColor(0, color, color, color, color)
        .pos(0, x1, y2, z2)
        .pos(1, x2, y2, z2)
        .pos(2, x2, y2, z1)
        .pos(3, x1, y2, z1)
        .maybe(y2 == 1f) {
            cullFace(Direction.UP)
        }
        .emit()

    this.tag(Tag.North)
        .spriteColor(0, color, color, color, color)
        .pos(0, x1, y1, z1)
        .pos(1, x1, y2, z1)
        .pos(2, x2, y2, z1)
        .pos(3, x2, y1, z1)
        .maybe(z1 == 0f) {
            cullFace(Direction.NORTH)
        }
        .emit()

    this.tag(Tag.South)
        .spriteColor(0, color, color, color, color)
        .pos(0, x2, y1, z2)
        .pos(1, x2, y2, z2)
        .pos(2, x1, y2, z2)
        .pos(3, x1, y1, z2)
        .maybe(z2 == 1f) {
            cullFace(Direction.SOUTH)
        }
        .emit()

    this.tag(Tag.East)
        .spriteColor(0, color, color, color, color)
        .pos(0, x2, y2, z1)
        .pos(1, x2, y2, z2)
        .pos(2, x2, y1, z2)
        .pos(3, x2, y1, z1)
        .maybe(x2 == 1f) {
            cullFace(Direction.EAST)
        }
        .emit()

    this.tag(Tag.West)
        .spriteColor(0, color, color, color, color)
        .pos(0, x1, y1, z1)
        .pos(1, x1, y1, z2)
        .pos(2, x1, y2, z2)
        .pos(3, x1, y2, z1)
        .maybe(x1 == 0f) {
            cullFace(Direction.WEST)
        }
        .emit()

    return this
}