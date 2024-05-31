package potfur.riskOfKt.textures

import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.Vec2f
import potfur.riskOfKt.textures.Direction.LEFT
import potfur.riskOfKt.textures.Direction.RIGHT

enum class Direction {
    LEFT, RIGHT;

    fun asModifier(): Float = when (this) {
        RIGHT -> 1f
        LEFT -> -1f
    }
}

data class Box(val width: Float, val height: Float)

class RichTextureSlice(
    slice: TextureSlice,
    center: Vec2f = Vec2f(slice.width / 2f, slice.height / 2f),
    val direction: Direction = RIGHT,
    val box: Box = Box(slice.width.toFloat(), slice.height.toFloat()),
) : TextureSlice(slice.texture, slice.x, slice.y, slice.width, slice.height) {
    val offset = center

    val center
        get() = when (isFlipH) {
            false -> offset
            true -> Vec2f(width - offset.x, offset.y)
        }

    fun centered(at: Vec2f) = Rect(at.x - box.width / 2, at.y - box.height / 2, box.width, box.height)

    fun face(direction: Direction) {
        when (this.direction) {
            LEFT -> when {
                direction == RIGHT && !isFlipH -> flipH()
                direction == LEFT && isFlipH -> flipH()
            }

            RIGHT -> when {
                direction == RIGHT && isFlipH -> flipH()
                direction == LEFT && !isFlipH -> flipH()
            }
        }
    }
}
