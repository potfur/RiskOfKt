package potfur.riskOfKt.textures

import com.lehaine.littlekt.file.vfs.VfsFile
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graphics.Texture
import com.lehaine.littlekt.graphics.g2d.TextureSlice


data class TileSet(
    private val tiles: Map<Type, TextureSlice>,
) {
    enum class Type { LEFT_CORNER, RIGHT_CORNER, LEFT_WALL, RIGHT_WALL, FLOOR_A, FLOOR_B, FLOOR_C, FILL }

    data class Builder(private val vfsFile: VfsFile) {
        val elements = mutableMapOf<Type, TextureSlice>()

        suspend operator fun String.invoke(fn: suspend Builder.(Texture) -> Unit) {
            fn(this@Builder, vfsFile[this].readTexture())
        }

        infix fun Type.of(slice: TextureSlice) {
            elements[this] = slice
        }
    }

    companion object {
        suspend fun fromVFS(vfsFile: VfsFile, fn: suspend Builder.() -> Unit): TileSet =
            TileSet(Builder(vfsFile).apply { fn(this) }.elements)
    }


    operator fun get(key: Type) = tiles[key] ?: throw NoSuchElementException("No tile for key $key")

}

