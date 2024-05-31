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
import com.lehaine.littlekt.math.castRay
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

    override val rect: Rect get() = ani.currentKeyFrame?.asRect(position) ?: Rect(position.x, position.y, 0f, 0f)

    private var facing = RIGHT
    var shouldJump = false
    var shouldShot = false
    var shouldWalk = false

    private val walkingRay get() = Vec2f(x + 20f * facing.asModifier(), y)
    private val jumpingRay get() = Vec2f(x, y - 20f)
    private val fallingRay get() = Vec2f(x, y + 20f)

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
        if (!shouldJump && castRay(fallingRay)) y += 0.97f
        if (shouldJump && castRay(jumpingRay)) y -= 0.45f
        if (shouldWalk && castRay(walkingRay)) x += 0.25f * facing.asModifier()
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
            shapeRenderer.filledRectangle(it.asRect(position), color = Color.MAGENTA.withAlpha(0.25f).toFloatBits())
        }
        listOf(walkingRay, jumpingRay, fallingRay).forEach {
            shapeRenderer.line(position, it, Color.CYAN.withAlpha(0.75f).toFloatBits())
        }
        shapeRenderer.filledCircle(x, y, 2f, color = Color.CYAN.withAlpha(0.75f).toFloatBits())
        rayPoints.forEach {
            shapeRenderer.filledCircle(it.x, it.y, 2f, color = Color.YELLOW.toFloatBits())
        }
    }

    // TODO - thick ray seems ot be broken (?) - define bounding box to calc edge + mid rays
    private fun castRay(to: Vec2f): Boolean =
        castRay(x.toInt(), y.toInt(), to.x.toInt(), to.y.toInt()) { x, y ->
            rayPoints.add(Vec2f(x.toFloat(), y.toFloat()))
            world.hasCollision(x, y) == null
        }
}
