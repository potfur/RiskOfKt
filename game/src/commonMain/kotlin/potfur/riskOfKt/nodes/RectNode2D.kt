package potfur.riskOfKt.nodes

import com.lehaine.littlekt.graph.node.node2d.Node2D
import com.lehaine.littlekt.math.Rect


abstract class RectNode2D: Node2D() {
    abstract val rect: Rect
}
