package potfur.riskOfKt

import com.lehaine.littlekt.createLittleKtApp
import com.lehaine.littlekt.graphics.Color

fun main() {
    createLittleKtApp {
        backgroundColor = Color.DARK_GRAY
        title = "Risk of Kt"
    }.start {
        Game(it)
    }
}
