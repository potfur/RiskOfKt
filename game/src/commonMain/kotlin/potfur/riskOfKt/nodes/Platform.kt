package potfur.riskOfKt.nodes

import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.math.Rect

inline fun World.platform(style: PlatformStyle, callback: (Platform.() -> Unit) = {}) =
    addChild(Platform(style).also(callback))

data class PlatformStyle(
    val leftCorner: TextureSlice,
    val rightCorner: TextureSlice,
    val leftWall: TextureSlice,
    val rightWall: TextureSlice,
    val floor: TextureSlice,
    val fill: TextureSlice,
) {
    val width = floor.width
    val height = floor.height
}

class Platform(val style: PlatformStyle) : RectNode2D() {
    var width: Float = style.width.toFloat()
    var height: Float = style.height.toFloat()
    val x2 get() = rect.x2
    val y2 get() = rect.y2

    override val rect by lazy { Rect(x, y, width, height) }

    override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        super.render(batch, camera, shapeRenderer)

        iterations.map { (x, y) ->
            when {
                this.y == y -> when {
                    this.x == x -> style.leftCorner
                    this.x2 == x + style.width -> style.rightCorner
                    else -> style.floor
                }

                else -> when {
                    this.x == x -> style.leftWall
                    this.x2 == x + style.width -> style.rightWall
                    else -> style.fill
                }
            }.let { Triple(x, y, it) }
        }.onEach { (x, y, texture) ->
            batch.draw(texture, x, y)
        }
    }

    override fun debugRender(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        super.debugRender(batch, camera, shapeRenderer)
        shapeRenderer.filledRectangle(rect, color = Color.MAGENTA.withAlpha(0.25f).toFloatBits())
    }

    private val iterations: List<Pair<Float, Float>> by lazy {
        val xI = x.toInt() until x2.toInt() step style.width
        val yI = y.toInt() until y2.toInt() step style.height

        xI.flatMap { x -> yI.map { y -> x.toFloat() to y.toFloat() } }
    }
}
