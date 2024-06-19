package potfur.riskOfKt.nodes

import com.lehaine.littlekt.math.Vec2f
import com.lehaine.littlekt.util.milliseconds
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration

data class Acceleration(val defaults: Vec2f, val limit: Vec2f, var vector: Vec2f = defaults) {
    val x get() = vector.x
    val y get() = vector.y

    fun update(dt: Duration) {
        vector -= (vector - defaults) / dt.milliseconds
    }

    fun set(value: Vec2f) = set(value.x, value.y)

    fun set(x: Float = this.x, y: Float = this.y) = Acceleration(
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
