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
import potfur.riskOfKt.textures.Direction.RIGHT
import potfur.riskOfKt.textures.Tiles
import kotlin.time.Duration.Companion.milliseconds

val Tiles.Companion.PLATFORM_PLAIN_BIG get() = "PLATFORM_PLAIN_BIG"
val Tiles.Companion.PLATFORM_PLAIN_MEDIUM get() = "PLATFORM_PLAIN_MEDIUM"

val Animations.Companion.ENEMY_ALIEN_5_IDLE get() = "ENEMY_ALIEN_5_IDLE"
val Animations.Companion.ENEMY_ALIEN_5_WALK get() = "ENEMY_ALIEN_5_WALK"
val Animations.Companion.ENEMY_ALIEN_5_JUMP get() = "ENEMY_ALIEN_5_JUMP"
val Animations.Companion.ENEMY_ALIEN_5_SHOT get() = "ENEMY_ALIEN_5_SHOT"

class Game(context: Context) : ContextListener(context) {

    init {
        Logger.setLevels(Logger.Level.DEBUG)
    }

    override suspend fun Context.start() {
        val viewport = ExtendViewport(200, 200)

        val tiles = Tiles.fromVFS(resourcesVfs) {
            "Tileset/Tileset.png" {
                Tiles.PLATFORM_PLAIN_BIG of TextureSlice(it, 0, 0, 48, 48)
                Tiles.PLATFORM_PLAIN_MEDIUM of TextureSlice(it, 0, 48, 48, 16)
            }
        }

        val animations = Animations.fromVFS(resourcesVfs) {
            "Enemies/Alien5.png" {
                Animations.ENEMY_ALIEN_5_IDLE of List(4) { i ->
                    TextureSlice(it, i * 64, 0, 64, 64) facing RIGHT center Vec2f(24f, 32f)
                } duration 150.milliseconds

                Animations.ENEMY_ALIEN_5_WALK of List(8) { i ->
                    TextureSlice(it, i * 64, 64, 64, 64) facing RIGHT center Vec2f(24f, 32f)
                } duration 150.milliseconds

                Animations.ENEMY_ALIEN_5_JUMP of List(6) { i ->
                    TextureSlice(it, i * 64, 192, 64, 64) facing RIGHT center Vec2f(24f, 32f)
                } duration 150.milliseconds

                Animations.ENEMY_ALIEN_5_SHOT of List(8) { i ->
                    TextureSlice(it, i * 64, 128, 64, 64) facing RIGHT center Vec2f(24f, 32f)
                } duration 150.milliseconds
            }
        }

        val scene = sceneGraph(this, viewport) {
            requestShowDebugInfo = true

            world {
                player(AnimationPlayer(), input) {
                    ani.registerState(animations[Animations.ENEMY_ALIEN_5_JUMP], 4) { shouldJump }
                    ani.registerState(animations[Animations.ENEMY_ALIEN_5_SHOT], 3) { shouldShot }
                    ani.registerState(animations[Animations.ENEMY_ALIEN_5_WALK], 2) { shouldWalk }
                    ani.registerState(animations[Animations.ENEMY_ALIEN_5_IDLE], 1)
                    position = Vec2f(30f, -40f)
                }

                platform(tiles[Tiles.PLATFORM_PLAIN_BIG]) {
                    position = Vec2f(0f, 0f)
                }

                platform(tiles[Tiles.PLATFORM_PLAIN_MEDIUM]) {
                    position = Vec2f(-48f, -16f)
                }

                platform(tiles[Tiles.PLATFORM_PLAIN_MEDIUM]) {
                    position = Vec2f(+48f, 16f)
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
