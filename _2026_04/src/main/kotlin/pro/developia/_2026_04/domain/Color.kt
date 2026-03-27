package pro.developia._2026_04.domain

enum class Color(val r: Int, var g: Int, var b: Int) {
    RED(255, 0, 0),
    ORANGE(255, 165, 0),
    YELLOW(255, 255, 0),
    GREEN(0, 255, 0),
    BLUE(0, 0, 255),
    PURPLE(0, 0, 255);


}

fun getColorName(color: Color): String = when (color) {
    Color.RED -> "R"
    Color.ORANGE -> "G"
    Color.YELLOW -> "B"
    Color.GREEN -> "G"
    Color.BLUE -> "B"
    Color.PURPLE -> "P"
}
