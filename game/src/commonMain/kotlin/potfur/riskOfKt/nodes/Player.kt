package potfur.riskOfKt.nodes

import com.lehaine.littlekt.graph.SceneGraph
import com.lehaine.littlekt.graph.node.addTo
import com.lehaine.littlekt.graph.node.node2d.Node2D
import com.lehaine.littlekt.graphics.Camera
import com.lehaine.littlekt.graphics.g2d.AnimationPlayer
import com.lehaine.littlekt.graphics.g2d.Batch
import com.lehaine.littlekt.graphics.g2d.shape.ShapeRenderer
import com.lehaine.littlekt.input.Input
import com.lehaine.littlekt.input.Key
import potfur.riskOfKt.DirectionalTextureSlice
import potfur.riskOfKt.DirectionalTextureSlice.Direction
import potfur.riskOfKt.DirectionalTextureSlice.Direction.LEFT
import potfur.riskOfKt.DirectionalTextureSlice.Direction.RIGHT
import kotlin.time.Duration

inline fun <T> SceneGraph<T>.player(animations: AnimationPlayer<DirectionalTextureSlice>, input: Input, callback: Player.() -> Unit = {}) =
    Player(animations, input).also(callback).addTo(root)

class Player(
    val ani: AnimationPlayer<DirectionalTextureSlice>,
    val input: Input,
) : Node2D() {

    private var facing: Direction = RIGHT
    var shouldJump = false
    var shouldShot = false
    var shouldWalk = false

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
    }

    override fun render(batch: Batch, camera: Camera, shapeRenderer: ShapeRenderer) {
        super.render(batch, camera, shapeRenderer)
        ani.currentKeyFrame?.let {
            it.face(facing)

            batch.draw(it, x, y)
        }
    }
}
