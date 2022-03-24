typealias Drawer<K> = (K) -> String

class Document(list: MutableList<Any>) : MutableList<Any> by list {
    val drawer = mutableMapOf<Class<*>, Drawer<*>>()

    private val noDrawerForValue = { x: Any -> "No drawer for value." }

    inline fun <reified T> registerDrawer(noinline fn: Drawer<T>) = drawer.put(T::class.java, fn)

    fun draw(pos: Int): String {
        fun content() = joinToString("\n") { value ->
            when (value) {
                is Document -> value.draw(pos)
                else -> draw(value, pos + 2).padStart(5)
            }
        }

        return """
<document>
${content()}
</document>""".trimIndent().lines().joinToString("\n" + " ".repeat(pos + 2))
    }

    private fun draw(value: Any, pos: Int) =
        (drawer.getOrDefault(value.javaClass, noDrawerForValue) as Drawer<Any>).let { drawIt ->
            " ".repeat(pos) + drawIt(value)
        }

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
