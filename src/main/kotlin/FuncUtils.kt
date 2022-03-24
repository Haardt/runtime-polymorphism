fun <A, B, C> partial1(a: A, f: (A, B) -> C): (B) -> C {
    return { b -> f(a, b) }
}

fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C {
    return { a -> { b -> f(a, b) } }
}

fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C {
    return { a, b -> f(a)(b) }
}

fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C {
    return { a -> f(g(a)) }
}

sealed class FList<out A> {
    companion object {
        fun <A> of(vararg elements: A): FList<A> {
            val tail = elements.sliceArray(1 until elements.size)
            return if (elements.isEmpty()) Nil else Con(elements[0], of(*tail))
        }

        fun <A> empty(): FList<A> = Nil
    }

    object Nil : FList<Nothing>()
    data class Con<out A>(val head: A, val tail: FList<A>) : FList<A>()
}

fun <A, B> FList<A>.map(f: (A) -> B): FList<B> {
    fun loop(vararg n: FList<A>): FList<B> {
        return when (val e = n[0]) {
            is FList.Nil -> FList.Nil
            is FList.Con<A> -> FList.Con(f(e.head), loop(e.tail))
        }
    }
    return loop(this)
}

fun <A> FList<A>.tail(): FList<A> = when (val e = this) {
    is FList.Nil -> FList.Nil
    is FList.Con<A> -> when (val t = e.tail) {
        is FList.Nil -> FList.Nil
        is FList.Con<A> -> FList.Con(t.head, t.tail)
    }
}

fun <A> FList<A>.drop(i: Int): FList<A> = when (this) {
    is FList.Nil -> FList.Nil
    is FList.Con<A> -> (1..i).map { this.tail() }.last()
}

fun <A> FList<A>.dropLast(): FList<A> {
    fun loop(vararg n: FList<A>): FList<A> {
        return when (val e = n[0]) {
            is FList.Nil -> FList.Nil
            is FList.Con<A> -> if (e.tail is FList.Nil) {
                FList.Nil
            } else
                FList.Con(e.head, loop(e.tail))
        }
    }
    return loop(this)
}

fun <A> FList<A>.dropIf(f: (A) -> Boolean): FList<A> {
    fun loop(vararg n: FList<A>): FList<A> {
        return when (val e = n[0]) {
            is FList.Nil -> FList.Nil
            is FList.Con<A> -> if (f(e.head)) {
                loop(e.tail)
            } else {
                FList.Con(e.head, loop(e.tail))
            }
        }
    }
    return loop(this)
}

fun <A> FList<A>.setHead(e: A): FList<A> = this.tail().let { tail ->
    FList.Con(e, tail)
}

fun <A> FList<A>.append(otherList: FList<A>): FList<A> = when (val e = this) {
    is FList.Nil -> otherList
    is FList.Con<A> -> FList.Con(e.head, e.tail.append(otherList))
}

fun <A, B> FList<A>.foldRight(z: B, f: (A, B) -> B): B {
    return when (val xs = this) {
        is FList.Nil -> z
        is FList.Con<A> -> f(xs.head, xs.tail.foldRight(z, f))
    }
}

fun <A> FList<A>.length(): Int {
    return this.foldRight(0) { _, b ->
        b + 1
    }
}


fun main(args: Array<String>) {
    val fn1 = { a: String, b: String -> a + b }
    val fn2 = { a: String -> a.toInt() }

    val p = partial1("Hallo", fn1)

    val c = curry(fn1)
    val dc = uncurry(c)

    val com = compose(fn2, c("1"))

    println(com("2"))

    val list = FList.of("1", "2", "3")
    val other = FList.of("Super", "duper")
    list.dropIf { e -> e == "2" }
        .append(other)
        .dropLast()
        .map { e ->
            println("JO: $e")
            e
        }.length().apply { println("Result $this") }
}
