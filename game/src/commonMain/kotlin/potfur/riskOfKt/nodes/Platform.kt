package potfur.riskOfKt.nodes

import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.math.Rect

data class PlatformConfig(var x: Int, var y: Int, var width: Int, var height: Int)

inline fun World.platform(style: PlatformStyle, callback: (PlatformConfig.() -> Unit) = {}) =
    PlatformConfig(0, 0, style.width, style.height)
        .apply {
            callback(this)
            addChild(Platform(this, style))
        }

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

class Platform(config: PlatformConfig, val style: PlatformStyle) : RectNode2D() {
    init {
        x = config.x * style.width.toFloat()
        y = config.y * style.height.toFloat()
    }

    var width: Float = config.width * style.width.toFloat()
    var height: Float = config.height * style.height.toFloat()
    val x2 get() = x + width
    val y2 get() = y + height

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
