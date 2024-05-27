package potfur.riskOfKt.nodes

import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.node2d.Node2D
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits
import potfur.riskOfKt.textures.asRect

inline fun <T> SceneGraph<T>.platform(texture: TextureSlice, callback: (Platform.() -> Unit) = {}) =
    Platform(texture).also(callback).addTo(root)

class Platform(private val texture: TextureSlice) : Node2D() {

    override fun debugRender(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        super.debugRender(batch, camera, shapeRenderer)
        shapeRenderer.filledRectangle(texture.asRect(position), color= Color.MAGENTA.withAlpha(0.25f).toFloatBits())
    }

    override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        super.render(batch, camera, shapeRenderer)
        batch.draw(texture, x, y)
    }
}
