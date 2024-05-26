package potfur.riskOfKt

import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.graphics.Color

fun main() {
    createLittleKtApp {
        title = "Risk of Kt"
        backgroundColor = Color.DARK_GRAY
        canvasId = "canvas"
    }.start {
        Game(it)
    }
}
