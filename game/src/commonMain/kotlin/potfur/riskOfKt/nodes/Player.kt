package potfur.riskOfKt.nodes

import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.AnimationPlayer
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.input.Input
import com.lehaine.littlekt.input.Key
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.Vec2f
import potfur.riskOfKt.textures.Direction.LEFT
import potfur.riskOfKt.textures.Direction.RIGHT
import potfur.riskOfKt.textures.RichTextureSlice
import kotlin.time.Duration

inline fun World.player(animations: AnimationPlayer<RichTextureSlice>, input: Input, callback: Player.() -> Unit = {}) =
    addChild(Player(this, animations, input).also(callback))

class Player(
    val world: World,
    val ani: AnimationPlayer<RichTextureSlice>,
    val input: Input,
) : RectNode2D() {

    override val rect: Rect get() = ani.currentKeyFrame?.centered(position) ?: Rect(x, y, 0f, 0f)

    private var facing = RIGHT
    var shouldJump = false
    var shouldShot = false
    var shouldWalk = false

    private val rayPoints = mutableSetOf<Vec2f>()

    override fun update(dt: Duration) {
        super.update(dt)

        facing = when {
            input.isKeyPressed(Key.ARROW_LEFT) -> LEFT
            input.isKeyPressed(Key.ARROW_RIGHT) -> RIGHT
            else -> facing
        }

        shouldShot = input.isKeyPressed(Key.SPACE)
        shouldJump = input.isKeyPressed(Key.ARROW_UP)
        shouldWalk = input.isKeyPressed(Key.ARROW_LEFT) || input.isKeyPressed(Key.ARROW_RIGHT)

        ani.update(dt)
        rayPoints.clear()
        if (!shouldJump && hasNoFloor()) y += 0.97f
        if (shouldJump && hasNoRoof()) y -= 0.45f
        if (shouldWalk && hasNoWall()) x += 0.25f * facing.asModifier()
    }


    override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        super.render(batch, camera, shapeRenderer)
        ani.currentKeyFrame?.let {
            it.face(facing)

            batch.draw(it, x, y, it.center.x, it.center.y)
        }
    }

    override fun debugRender(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        super.debugRender(batch, camera, shapeRenderer)
        ani.currentKeyFrame?.let {
            shapeRenderer.filledRectangle(it.centered(position), color = Color.MAGENTA.withAlpha(0.25f).toFloatBits())
        }
        shapeRenderer.filledCircle(x, y, 2f, color = Color.CYAN.withAlpha(0.75f).toFloatBits())
        rayPoints.forEach {
            shapeRenderer.filledCircle(it.x, it.y, 1f, color = Color.YELLOW.toFloatBits())
        }
    }

    private fun hasNoRoof() =
        rect
            .let { r -> listOf(x, r.x + 1, r.x2 - 1).map { it to y - r.height / 2 } }
            .all { (x, y) ->
                rayPoints.add(Vec2f(x, y))
                world.hasCollisionV(x.toInt(), y.toInt()) == null
            }

    private fun hasNoFloor() =
        rect
            .let { r -> listOf(x, r.x + 1, r.x2 - 1).map { it to y + rect.height / 2 } }
            .all { (x, y) ->
                rayPoints.add(Vec2f(x, y))
                world.hasCollisionV(x.toInt(), y.toInt()) == null
            }

    private fun hasNoWall() =
        rect
            .let { r -> listOf(y, r.y + 1, r.y2 - 1).map { x + rect.width / 2 * facing.asModifier() to it } }
            .all { (x, y) ->
                rayPoints.add(Vec2f(x, y))
                world.hasCollisionH(x.toInt(), y.toInt()) == null
            }
}
