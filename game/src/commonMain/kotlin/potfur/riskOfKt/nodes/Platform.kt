package potfur.riskOfKt.nodes

import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits
import potfur.riskOfKt.textures.asRect

inline fun World.platform(texture: TextureSlice, callback: (Platform.() -> Unit) = {}) =
    addChild(Platform(texture).also(callback))

class Platform(private val texture: TextureSlice) : RectNode2D() {

    override val rect get() = texture.asRect(position)

    override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        super.render(batch, camera, shapeRenderer)
        batch.draw(texture, x, y)
    }

    override fun debugRender(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        super.debugRender(batch, camera, shapeRenderer)
        shapeRenderer.filledRectangle(texture.asRect(position), color= Color.MAGENTA.withAlpha(0.25f).toFloatBits())
    }
}
