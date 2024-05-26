package potfur.riskOfKt

import com.lehaine.littlekt.file.vfs.VfsFile
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graphics.Texture
import com.lehaine.littlekt.graphics.g2d.TextureSlice

data class Tiles(
    private val tiles: Map<String, TextureSlice>,
) {

    data class Builder(private val vfsFile: VfsFile) {
        val elements = mutableMapOf<String, TextureSlice>()

        suspend operator fun String.invoke(fn: suspend Builder.(Texture) -> Unit) {
            fn(this@Builder, vfsFile[this].readTexture())
        }

        infix fun String.of(slice: TextureSlice) {
            elements[this] = slice
        }
    }

    companion object {
        suspend fun fromVFS(vfsFile: VfsFile, fn: suspend Builder.() -> Unit): Tiles =
            Tiles(Builder(vfsFile).apply { fn(this) }.elements)
    }


    operator fun get(key: String) = tiles[key] ?: throw NoSuchElementException("No tile for key $key")

}

