package potfur.riskOfKt.textures

import com.lehaine.littlekt.file.vfs.VfsFile
import com.lehaine.littlekt.file.vfs.readTexture
import com.lehaine.littlekt.graphics.Texture
import com.lehaine.littlekt.graphics.g2d.TextureSlice

interface TileSet

data class Tiles(
    private val tiles: Map<TileSet, TextureSlice>,
) {

    data class Builder(private val vfsFile: VfsFile) {
        val elements = mutableMapOf<TileSet, TextureSlice>()

        suspend operator fun String.invoke(fn: suspend Builder.(Texture) -> Unit) {
            fn(this@Builder, vfsFile[this].readTexture())
        }

        infix fun TileSet.of(slice: TextureSlice) {
            elements[this] = slice
        }
    }

    companion object {
        suspend fun fromVFS(vfsFile: VfsFile, fn: suspend Builder.() -> Unit): Tiles =
            Tiles(Builder(vfsFile).apply { fn(this) }.elements)
    }


    operator fun get(key: TileSet) = tiles[key] ?: throw NoSuchElementException("No tile for key $key")

}

