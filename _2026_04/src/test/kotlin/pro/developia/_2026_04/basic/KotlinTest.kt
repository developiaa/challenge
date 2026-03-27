package pro.developia._2026_04.basic

import org.junit.jupiter.api.Test

class KotlinTest {
    @Test
    fun main(){
        val color: Color = Color.Blue(255, 0, 20)
        when (color) {
            is Color.Green -> println("초록")
            is Color.Red -> println("빨강")
            is Color.Blue -> println("파랑")
            is Color.Purple -> println("보라")
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
