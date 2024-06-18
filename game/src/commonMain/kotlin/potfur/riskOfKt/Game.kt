package potfur.riskOfKt

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.ContextListener
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.g2d.AnimationPlayer
import com.lehaine.littlekt.graphics.g2d.TextureSlice
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.log.Logger
import com.lehaine.littlekt.math.Vec2f
import com.lehaine.littlekt.util.viewport.ExtendViewport
import potfur.riskOfKt.nodes.platform
import potfur.riskOfKt.nodes.player
import potfur.riskOfKt.nodes.world
import potfur.riskOfKt.textures.Animations
import potfur.riskOfKt.textures.Box
import potfur.riskOfKt.textures.Direction.RIGHT
import potfur.riskOfKt.textures.Tiles
import kotlin.time.Duration.Companion.milliseconds

val Tiles.Companion.DIRT_GRASS_A get() = "DIRT_GRASS_A"

val Animations.Companion.KING_IDLE get() = "KING_IDLE"
val Animations.Companion.KING_WALK get() = "KING_WALK"
val Animations.Companion.KING_JUMP get() = "KING_JUMP"

class Game(context: Context) : ContextListener(context) {

    init {
        Logger.setLevels(Logger.Level.DEBUG)
    }

    override suspend fun Context.start() {
        val viewport = ExtendViewport(200, 200)

        val tiles = Tiles.fromVFS(resourcesVfs) {
            "tiles.png" {
                Tiles.DIRT_GRASS_A of TextureSlice(it, 128, 0, 16, 16)
            }
        }

        val animations = Animations.fromVFS(resourcesVfs) {
            "characters.png" {
                Animations.KING_IDLE of listOf(
                    TextureSlice(it, 0, 40, 24, 24) facing RIGHT center Vec2f(16f, 12f) box Box(16f, 24f)
                ) duration 150.milliseconds

                Animations.KING_WALK of (0..4).map { i ->
                    TextureSlice(it, i * 32, 40, 24, 24) facing RIGHT center Vec2f(16f, 12f) box Box(16f, 24f)
                } duration 150.milliseconds

                Animations.KING_JUMP of listOf(
                    TextureSlice(it, 6 * 32, 40, 24, 24) facing RIGHT center Vec2f(16f, 12f) box Box(16f, 24f),
                ) duration 150.milliseconds
            }
        }

        val scene = sceneGraph(this, viewport) {
            requestShowDebugInfo = false

            world {
                player(AnimationPlayer(), input) {
                    ani.registerState(animations[Animations.KING_JUMP], 4) { shouldJump }
                    ani.registerState(animations[Animations.KING_WALK], 2) { shouldWalk }
                    ani.registerState(animations[Animations.KING_IDLE], 1)
                    position = Vec2f(0f, -40f)
                }

                platform(tiles[Tiles.DIRT_GRASS_A]) {
                    position = Vec2f(0f, 0f)
                }
                platform(tiles[Tiles.DIRT_GRASS_A]) {
                    position = Vec2f(16f, 16f)
                }
                platform(tiles[Tiles.DIRT_GRASS_A]) {
                    position = Vec2f(32f, 32f)
                }
            }
        }.apply {
            initialize()
        }

        onResize { width, height ->
            viewport.update(width, height, context)
        }

        onRender { dt ->
            gl.clearColor(Color.GRAY)
            gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)

            scene.update(dt)
            scene.render()
        }
    }
}
