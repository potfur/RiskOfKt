package potfur.riskOfKt.nodes

import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.node2d.Node2D
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits

inline fun <T> SceneGraph<T>.world(callback: (World.() -> Unit) = {}) =
    World().also(callback).addTo(root)

class World() : Node2D() {
    private val gridH = mutableMapOf<Pair<Int, Int>, RectNode2D>()
    private val gridV = mutableMapOf<Pair<Int, Int>, RectNode2D>()

    fun hasCollisionH(x: Int, y: Int): RectNode2D? = gridH[x to y]
    fun hasCollisionV(x: Int, y: Int): RectNode2D? = gridV[x to y]

    override fun ready() {
        super.ready()
        gridH.clear()
        nodes.nodesOfType<Platform>().forEach {
            addToGrid(it)
        }
    }

    override fun debugRender(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        super.debugRender(batch, camera, shapeRenderer)
        (gridH.keys + gridV.keys).forEach { (x, y) ->
            shapeRenderer.rectangle(x.toFloat(), y.toFloat(), 1f, 1f, color = Color.RED.withAlpha(0.5f).toFloatBits())
        }
    }

    private fun addToGrid(node: RectNode2D) {
        node.rect.let { rect ->
            (rect.x.toInt()..rect.x2.toInt()).forEach { x ->
                gridV[x to rect.y.toInt()] = node
                gridV[x to rect.y2.toInt()] = node
            }
            (rect.y.toInt()..rect.y2.toInt()).forEach { y ->
                gridH[rect.x.toInt() to y] = node
                gridH[rect.x2.toInt() to y] = node
            }
        }
    }
}
