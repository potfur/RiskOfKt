package potfur.riskOfKt.textures

import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.Vec2f
import potfur.riskOfKt.textures.Direction.LEFT
import potfur.riskOfKt.textures.Direction.RIGHT

enum class Direction { LEFT, RIGHT }

class RichTextureSlice(
    slice: TextureSlice,
    private val direction: Direction,
) : TextureSlice(slice.texture, slice.x, slice.y, slice.width, slice.height) {

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
