package pro.developia._2026_04.basic

import org.junit.jupiter.api.Test

class KotlinTest {
    @Test
    fun main() {
        val color: Color = Color.Blue(255, 0, 20)
        when (color) {
            is Color.Green -> println("초록")
            is Color.Red -> println("빨강")
            is Color.Blue -> println("파랑")
            is Color.Purple -> println("보라")
        }

        sayHello()
        println()
        sayHello("ace")
        println()

        println()

//        var obj = "" //String
        var obj = 123 //Int

        typeCheck(obj)

        for(i in 4..5) {
            println("Hey $i!")
        }


        val range1 = 1..10
        val range2 = 0 until 10
        println(range1)// [1]
        println(range2)// [2]

        showRange(1..5)
        showRange(0 until 5)
        showRange(5 downTo 1)// [1]
        showRange(0..9 step 2)// [2]
        showRange(0 until 10 step 3)// [3]
        showRange(9 downTo 2 step 3)
    }

    fun showRange(r: IntProgression) {
        for (i in r) {
            print("$i ")
        }
        println()
    }

    fun sayHello(name: String = "kj") {
        print("Hello $name")
    }

    fun typeCheck(obj: Any) {
        when (obj) {
            is Int -> println("Int")
            is String -> println("String")
            is Double -> println("Double")
            is Boolean -> println("Boolean")
            else -> println("else")
        }
    }
}


sealed class Color {
    data class Red(val r: Int, val g: Int, val b: Int) : Color()
    data class Green(val r: Int, val g: Int, val b: Int) : Color()
    data class Blue(val r: Int, val g: Int, val b: Int) : Color()
    data class Purple(val r: Int, val g: Int, val b: Int) : Color()
}

//data class Red(val r: Int, val g: Int, val b: Int) : Color()
//data class Green(val r: Int, val g: Int, val b: Int) : Color()
//data class Blue(val r: Int, val g: Int, val b: Int) : Color()
//data class Purple(val r: Int, val g: Int, val b: Int) : Color()
