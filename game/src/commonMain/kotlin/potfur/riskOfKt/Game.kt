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
import potfur.riskOfKt.textures.Animations
import potfur.riskOfKt.textures.Box
import potfur.riskOfKt.textures.Direction.RIGHT
import potfur.riskOfKt.textures.TileSet
import potfur.riskOfKt.textures.Tiles
import kotlin.time.Duration.Companion.milliseconds

enum class DirtGrass : TileSet { LEFT_CORNER, RIGHT_CORNER, LEFT_WALL, RIGHT_WALL, FLOOR_A, FLOOR_B, FLOOR_C, FILL }

enum class King : AnimationSet { IDLE, WALK, JUMP }

class Game(context: Context) : ContextListener(context) {

    init {
        Logger.setLevels(Logger.Level.DEBUG)
    }

    override suspend fun Context.start() {
        val viewport = ExtendViewport(200, 200)

        val tiles = Tiles.fromVFS(resourcesVfs) {
            "tiles.png" {
                DirtGrass.FLOOR_A of TextureSlice(it, 112, 0, 16, 15)
                DirtGrass.FLOOR_B of TextureSlice(it, 128, 0, 16, 15)
                DirtGrass.FLOOR_C of TextureSlice(it, 144, 0, 16, 15)
                DirtGrass.LEFT_CORNER of TextureSlice(it, 128, 16, 16, 15)
                DirtGrass.RIGHT_CORNER of TextureSlice(it, 144, 16, 16, 15)
                DirtGrass.LEFT_WALL of TextureSlice(it, 160, 16, 16, 15)
                DirtGrass.RIGHT_WALL of TextureSlice(it, 192, 16, 16, 15)
                DirtGrass.FILL of TextureSlice(it, 176, 16, 16, 15)
            }
        }

        val dirtGrass = PlatformStyle(
            tiles[DirtGrass.LEFT_CORNER],
            tiles[DirtGrass.RIGHT_CORNER],
            tiles[DirtGrass.LEFT_WALL],
            tiles[DirtGrass.RIGHT_WALL],
            tiles[DirtGrass.FLOOR_B],
            tiles[DirtGrass.FILL],
        )

        val animations = Animations.fromVFS(resourcesVfs) {
            "characters.png" {
                King.IDLE of listOf(
                    TextureSlice(it, 0, 40, 24, 24) facing RIGHT center Vec2f(16f, 12f) box Box(16f, 22f)
                ) duration 150.milliseconds

                King.WALK of (0..4).map { i ->
                    TextureSlice(it, i * 32, 40, 24, 24) facing RIGHT center Vec2f(16f, 12f) box Box(16f, 22f)
                } duration 150.milliseconds

                King.JUMP of listOf(
                    TextureSlice(it, 6 * 32, 40, 24, 24) facing RIGHT center Vec2f(16f, 12f) box Box(16f, 22f),
                ) duration 150.milliseconds
            }
        }

        val scene = sceneGraph(this, viewport) {
            requestShowDebugInfo = false

            world {
                player(AnimationPlayer(), input) {
                    ani.registerState(animations[King.JUMP], 4) { shouldJump }
                    ani.registerState(animations[King.WALK], 2) { shouldWalk }
                    ani.registerState(animations[King.IDLE], 1)
                    position = Vec2f(0f, -40f)
                }

                platform(dirtGrass) {
                    position = Vec2f(0f, 0f)
                    width = 10f * style.width
                    height = 3f * style.height
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
