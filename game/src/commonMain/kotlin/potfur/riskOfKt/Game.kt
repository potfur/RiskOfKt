package potfur.riskOfKt

import com.lehaine.littlekt.Context
import com.lehaine.littlekt.ContextListener
import com.lehaine.littlekt.graph.sceneGraph
import com.lehaine.littlekt.graphics.Color
import com.lehaine.littlekt.graphics.gl.ClearBufferMask
import com.lehaine.littlekt.log.Logger
import com.lehaine.littlekt.util.viewport.ExtendViewport

class Game(context: Context) : ContextListener(context) {

    init {
        Logger.setLevels(Logger.Level.DEBUG)
    }

    override suspend fun Context.start() {
        val viewport = ExtendViewport(960, 540)

        val scene = sceneGraph(this, viewport) {
            requestShowDebugInfo = true
        }.apply {
            initialize()
        }

        onResize { width, height ->
            viewport.update(width, height, context)
        }

        onRender { dt ->
            gl.clearColor(Color.DARK_GRAY)
            gl.clear(ClearBufferMask.COLOR_BUFFER_BIT)

            scene.update(dt)
            scene.render()
        }
    }
}
