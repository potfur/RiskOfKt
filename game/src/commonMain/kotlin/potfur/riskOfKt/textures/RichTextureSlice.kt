package potfur.riskOfKt.textures

import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.Vec2f
import potfur.riskOfKt.textures.Direction.LEFT
import potfur.riskOfKt.textures.Direction.RIGHT

enum class Direction { LEFT, RIGHT }

class RichTextureSlice(
    slice: TextureSlice,
    center: Vec2f = Vec2f(slice.width / 2f, slice.height / 2f),
    val direction: Direction = RIGHT,
) : TextureSlice(slice.texture, slice.x, slice.y, slice.width, slice.height) {
    val offset = center

    val center get() = when (isFlipH) {
        false -> offset
        true -> Vec2f(width - offset.x, offset.y)
    }

    fun asRect(at: Vec2f) =
        Rect(at.x - center.x, at.y - center.y, width.toFloat(), height.toFloat())

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

fun TextureSlice.asRect(at: Vec2f) = Rect(at.x, at.y, width.toFloat(), height.toFloat())

operator fun Float.times(direction: Direction): Float = when (direction) {
    RIGHT -> this
    LEFT -> -this
}

operator fun Direction.times(value: Float): Float = value * this
