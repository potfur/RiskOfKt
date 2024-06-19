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
import potfur.riskOfKt.nodes.PlatformStyle
import potfur.riskOfKt.nodes.platform
import potfur.riskOfKt.nodes.player
import potfur.riskOfKt.nodes.world
import potfur.riskOfKt.textures.AnimationSet
import potfur.riskOfKt.textures.AnimationSet.Type.IDLE
import potfur.riskOfKt.textures.AnimationSet.Type.JUMP
import potfur.riskOfKt.textures.AnimationSet.Type.WALK
import potfur.riskOfKt.textures.Box
import potfur.riskOfKt.textures.Direction.RIGHT
import potfur.riskOfKt.textures.TileSet
import potfur.riskOfKt.textures.TileSet.Type.FILL
import potfur.riskOfKt.textures.TileSet.Type.FLOOR_A
import potfur.riskOfKt.textures.TileSet.Type.FLOOR_B
import potfur.riskOfKt.textures.TileSet.Type.FLOOR_C
import potfur.riskOfKt.textures.TileSet.Type.LEFT_CORNER
import potfur.riskOfKt.textures.TileSet.Type.LEFT_WALL
import potfur.riskOfKt.textures.TileSet.Type.RIGHT_CORNER
import potfur.riskOfKt.textures.TileSet.Type.RIGHT_WALL
import kotlin.time.Duration.Companion.milliseconds

class Game(context: Context) : ContextListener(context) {

    init {
        Logger.setLevels(Logger.Level.DEBUG)
    }

    override suspend fun Context.start() {
        val viewport = ExtendViewport(200, 200)

        val platformStyle = TileSet.fromVFS(resourcesVfs) {
            "tiles.png" {
                FLOOR_B of TextureSlice(it, 128, 0, 16, 15)
                FLOOR_A of TextureSlice(it, 112, 0, 16, 15)
                FLOOR_C of TextureSlice(it, 144, 0, 16, 15)
                LEFT_CORNER of TextureSlice(it, 128, 16, 16, 15)
                RIGHT_CORNER of TextureSlice(it, 144, 16, 16, 15)
                LEFT_WALL of TextureSlice(it, 160, 16, 16, 15)
                RIGHT_WALL of TextureSlice(it, 192, 16, 16, 15)
                FILL of TextureSlice(it, 176, 16, 16, 15)
            }
        }.let {
            PlatformStyle(
                leftCorner = it[LEFT_CORNER],
                rightCorner = it[RIGHT_CORNER],
                leftWall = it[LEFT_WALL],
                rightWall = it[RIGHT_WALL],
                floor = it[FLOOR_B],
                fill = it[FILL],
            )
        }

        val player = AnimationSet.fromVFS(resourcesVfs) {
            "characters.png" {
                IDLE of listOf(
                    TextureSlice(it, 0, 40, 24, 24) facing RIGHT center Vec2f(16f, 12f) box Box(16f, 22f)
                ) duration 150.milliseconds

                WALK of (0..4).map { i ->
                    TextureSlice(it, i * 32, 40, 24, 24) facing RIGHT center Vec2f(16f, 12f) box Box(16f, 22f)
                } duration 150.milliseconds

                JUMP of listOf(
                    TextureSlice(it, 6 * 32, 40, 24, 24) facing RIGHT center Vec2f(16f, 12f) box Box(16f, 22f),
                ) duration 150.milliseconds
            }
        }

        val scene = sceneGraph(this, viewport) {
            requestShowDebugInfo = false

            world {
                player(AnimationPlayer(), input) {
                    ani.registerState(player[JUMP], 4) { shouldJump }
                    ani.registerState(player[WALK], 2) { shouldWalk }
                    ani.registerState(player[IDLE], 1)
                    position = Vec2f(0f, -40f)
                }

                platform(platformStyle) {
                    x = -12
                    y = 3
                    width = 4
                    height = 4
                }

                platform(platformStyle) {
                    x = -5
                    y = 1
                    width = 8
                    height = 6
                }

                platform(platformStyle) {
                    x = 4
                    y = 2
                    width = 4
                    height = 5
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
