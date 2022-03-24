typealias Drawer<K> = (K) -> String

class Document(list: MutableList<Any>) : MutableList<Any> by list {
    val drawer = mutableMapOf<Class<*>, Drawer<*>>()

    private val noDrawerForValue = { x: Any -> "No drawer for value." }

    inline fun <reified T> registerDrawer(noinline fn: Drawer<T>) {
        drawer[T::class.java] = fn
    }

    fun draw(pos: Int) =
        """<document>
 ${
            joinToString("\n") { value ->
                if (value is Document)
                    drawDoc(value, pos + 2).lines().joinToString("\n") { " ".repeat(pos + 2) + it }
                else draw(value, pos + 2).padStart(5)
            }
        }
</document>"""

    private fun drawDoc(document: Document, pos: Int): String = document.draw(pos)

    private fun draw(value: Any, pos: Int) =
        ((drawer[value!!::class.java] ?: noDrawerForValue) as Drawer<Any>)(value).padStart(pos)

    fun copy() = Document(toMutableList()).also { it.drawer.putAll(drawer) }
}

fun main(args: Array<String>) {
    val document = Document(mutableListOf())
    document.registerDrawer<String> { value -> value }
    document.registerDrawer<Int> { value -> "$value" }

    document.add("Test")
    document.add(123)
    val c = document.copy()
    c.add("x")
    c.add("y")
    c.add(c.copy())
    document.add(c)
    document.add(123)

    println(document.draw(0))

}
