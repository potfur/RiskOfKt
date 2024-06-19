package potfur.riskOfKt.textures

import com.lehaine.littlekt.file.vfs.VfsFile
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graphics.Texture
import com.lehaine.littlekt.graphics.g2d.Animation
import com.lehaine.littlekt.graphics.g2d.AnimationBuilder
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.math.Vec2f
import kotlin.time.Duration


data class AnimationSet(
    private val animations: Map<Type, Animation<RichTextureSlice>>,
) {
    enum class Type { IDLE, WALK, JUMP }

    data class Builder(private val vfsFile: VfsFile) {
        val elements = mutableMapOf<Type, Animation<RichTextureSlice>>()

        suspend operator fun String.invoke(fn: suspend Builder.(Texture) -> Unit) {
            fn(this@Builder, vfsFile[this].readTexture())
        }

        infix fun Type.of(slices: List<RichTextureSlice>) =
            this to slices

        infix fun Pair<Type, List<RichTextureSlice>>.animate(fn: AnimationBuilder<RichTextureSlice>.(List<RichTextureSlice>) -> Unit) {
            elements[first] = AnimationBuilder(second).apply { fn(this, second) }.build()
        }

        infix fun Pair<Type, List<RichTextureSlice>>.duration(value: Duration) {
            animate { frames(0..second.size, 0, value) }
        }

        infix fun TextureSlice.facing(direction: Direction) = RichTextureSlice(this, direction = direction)

        infix fun RichTextureSlice.center(at: Vec2f) = RichTextureSlice(this, at, direction, box)

        infix fun RichTextureSlice.box(box: Box) = RichTextureSlice(this, center, direction, box)
    }

    companion object {
        suspend fun fromVFS(vfsFile: VfsFile, fn: suspend Builder.() -> Unit): AnimationSet =
            AnimationSet(Builder(vfsFile).apply { fn(this) }.elements)
    }

    operator fun get(key: Type) = animations[key] ?: throw NoSuchElementException("No animation for key $key")
}

