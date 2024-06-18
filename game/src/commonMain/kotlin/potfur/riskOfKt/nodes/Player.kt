package potfur.riskOfKt.nodes

import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.AnimationPlayer
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.graphics.toFloatBits
import com.lehaine.littlekt.input.Input
import com.lehaine.littlekt.input.Key.ARROW_LEFT
import com.lehaine.littlekt.input.Key.ARROW_RIGHT
import com.lehaine.littlekt.input.Key.ARROW_UP
import com.lehaine.littlekt.math.Rect
import com.lehaine.littlekt.math.Vec2f
import com.lehaine.littlekt.math.floor
import com.lehaine.littlekt.util.milliseconds
import potfur.riskOfKt.textures.Direction.LEFT
import potfur.riskOfKt.textures.Direction.RIGHT
import potfur.riskOfKt.textures.RichTextureSlice
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.time.Duration

inline fun World.player(animations: AnimationPlayer<RichTextureSlice>, input: Input, callback: Player.() -> Unit = {}) =
    addChild(Player(this, animations, input).also(callback))

data class Acc(val defaults: Vec2f, val limit: Vec2f, var vector: Vec2f = defaults) {
    val x get() = vector.x
    val y get() = vector.y

    fun update(dt: Duration) {
        vector -= (vector - defaults) / dt.milliseconds
    }

    fun set(value: Vec2f) = set(value.x, value.y)

    fun set(x: Float = this.x, y: Float = this.y) = Acc(
        defaults = defaults,
        limit = limit,
        vector = Vec2f(max(min(x, limit.x), -limit.x).round(), max(min(y, limit.y), -limit.y).round())
    )

    operator fun plus(other: Vec2f) = set(vector + other)
    operator fun minus(other: Vec2f) = set(vector - other)

    private fun Float.round(precision: Int = 1000): Float =
        (floor(this * precision) / precision.toFloat())
            .let { if (it.absoluteValue < 0.02) 0f else it }
}

class Player(
    val world: World,
    val ani: AnimationPlayer<RichTextureSlice>,
    val input: Input,
) : RectNode2D() {

    override val rect: Rect get() = ani.currentKeyFrame?.centered(position) ?: Rect(x, y, 0f, 0f)

    private var facing = RIGHT
    private var acc = Acc(Vec2f(0f, 1.97f), Vec2f(1f, 2f))
    val shouldJump get() = acc.y < 0f
    val shouldWalk get() = acc.x != 0f

    private val rayPoints = mutableSetOf<Vec2f>()

    override fun update(dt: Duration) {
        super.update(dt)
        ani.update(dt)
        acc.update(dt)

        rayPoints.clear()

        facing = when {
            input.isKeyPressed(ARROW_LEFT) -> LEFT
            input.isKeyPressed(ARROW_RIGHT) -> RIGHT
            else -> facing
        }

        if (input.isKeyPressed(ARROW_UP)) acc += Vec2f(0f, -1.45f)
        if (input.areAnyKeysPressed(ARROW_LEFT, ARROW_RIGHT)) acc += Vec2f(0.25f * facing.asModifier(), 0f)

        if (!shouldJump) acc = limitByFloor(acc)
        if (shouldJump) acc = limitByCelling(acc)
        if (shouldWalk) acc = limitByWall(acc)

        x += acc.x
        y += acc.y
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

    private fun limitByCelling(acc: Acc): Acc =
        rect
            .let { r -> listOf(x, r.x + 1, r.x2 - 1).map { Vec2f(it, y - r.height / 2) } }
            .map { it to it + Vec2f(0f, acc.y) }
            .castRay { x, y -> world.hasCollisionV(x.toInt(), y.toInt()) }
            ?.let { acc.set(y = 0f) }
            ?: acc


    private fun limitByFloor(acc: Acc): Acc =
        rect
            .let { r -> listOf(x, r.x, r.x2).map { Vec2f(it, y + r.height / 2) } }
            .map { it to it + Vec2f(0f, acc.y) }
            .castRay { x, y -> world.hasCollisionV(x.toInt(), y.toInt()) }
            ?.let { acc.set(y = 0f) }
            ?: acc

    private fun limitByWall(acc: Acc): Acc =
        rect
            .let { r -> listOf(y, r.y + 1, r.y2 - 1).map { Vec2f(x + r.width / 2 * facing.asModifier(), it) } }
            .map { it to it + Vec2f(acc.x, 0f) }
            .castRay { x, y -> world.hasCollisionH(x.toInt(), y.toInt()) }
            ?.let { acc.set(x = 0f) }
            ?: acc

    private fun List<Pair<Vec2f, Vec2f>>.castRay(fn: (Float, Float) -> RectNode2D?): RectNode2D? {
        return this.firstNotNullOfOrNull { (from, to) ->
            (from iterateTo to).firstNotNullOfOrNull {
                rayPoints.add(it)
                fn(it.x, it.y)
            }
        }
    }
}

operator fun Vec2f.plus(other: Vec2f) = Vec2f(x + other.x, y + other.y)
operator fun Vec2f.minus(other: Vec2f) = Vec2f(x - other.x, y - other.y)
operator fun Vec2f.times(other: Float) = Vec2f(x * other, y * other)
operator fun Vec2f.div(other: Float) = Vec2f(x / other, y / other)


infix fun Vec2f.iterateTo(other: Vec2f): List<Vec2f> {
    val xD = (this.x iterateTo other.x).toList()
    val yD = (this.y iterateTo other.y).toList()

    val result = when {
        xD.size > yD.size -> {
            val d = yD.size.toFloat() / xD.size.toFloat()
            xD.mapIndexed { i, it -> it to yD[(d * i).floor().roundToInt()] }
        }

        xD.size < yD.size -> {
            val d = xD.size.toFloat() / yD.size.toFloat()
            yD.mapIndexed { i, it -> xD[(d * i).floor().roundToInt()] to it }
        }

        else -> xD zip yD
    }

    return result.map { (x, y) -> Vec2f(x, y) }
}

private infix fun Float.iterateTo(other: Float): List<Float> =
    when {
        this <= other -> this.roundToLong()..other.roundToLong()
        else -> this.roundToLong() downTo other.roundToLong()
    }.map { it.toFloat() }
