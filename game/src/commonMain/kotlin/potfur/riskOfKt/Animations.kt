package potfur.riskOfKt

import com.lehaine.littlekt.file.vfs.VfsFile
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graphics.Texture
import com.lehaine.littlekt.graphics.g2d.Animation
import com.lehaine.littlekt.graphics.g2d.AnimationBuilder
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import potfur.riskOfKt.DirectionalTextureSlice.Direction
import potfur.riskOfKt.DirectionalTextureSlice.Direction.LEFT
import potfur.riskOfKt.DirectionalTextureSlice.Direction.RIGHT
import kotlin.time.Duration

data class Animations(
    private val animations: Map<String, Animation<DirectionalTextureSlice>>,
) {
    data class Builder(private val vfsFile: VfsFile) {
        val elements = mutableMapOf<String, Animation<DirectionalTextureSlice>>()

        suspend operator fun String.invoke(fn: suspend Builder.(Texture) -> Unit) {
            fn(this@Builder, vfsFile[this].readTexture())
        }

        infix fun String.of(slices: List<DirectionalTextureSlice>) =
            this to slices

        infix fun Pair<String, List<DirectionalTextureSlice>>.animate(fn: AnimationBuilder<DirectionalTextureSlice>.(List<DirectionalTextureSlice>) -> Unit) {
            elements[first] = AnimationBuilder(second).apply { fn(this, second) }.build()
        }

        infix fun Pair<String, List<DirectionalTextureSlice>>.duration(value: Duration) {
            animate { frames(0..second.size, 0, value) }
        }

    }

    companion object {
        suspend fun fromVFS(vfsFile: VfsFile, fn: suspend Builder.() -> Unit): Animations =
            Animations(Builder(vfsFile).apply { fn(this) }.elements)
    }

    operator fun get(key: String) = animations[key] ?: throw NoSuchElementException("No animation for key $key")
}

class DirectionalTextureSlice(
    slice: TextureSlice,
    private val direction: Direction,
) : TextureSlice(slice.texture, slice.x, slice.y, slice.width, slice.height) {
    enum class Direction { LEFT, RIGHT }

    fun face(direction: Direction) {
        when {
            this.direction == LEFT -> when {
                direction == RIGHT && !isFlipH -> flipH()
                direction == LEFT && isFlipH -> flipH()
            }

            this.direction == RIGHT -> when {
                direction == RIGHT && isFlipH -> flipH()
                direction == LEFT && !isFlipH -> flipH()
            }
        }
    }
}

fun TextureSlice.faces(direction: Direction) = DirectionalTextureSlice(this, direction)
fun TextureSlice.faceLeft() = DirectionalTextureSlice(this, LEFT)
fun TextureSlice.faceRight() = DirectionalTextureSlice(this, RIGHT)
