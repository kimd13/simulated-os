package structs

import exceptions.InvalidOffsetException
import kotlin.math.pow

class Offset(value: Int){

    val MAX_SIZE = 2.0.pow(9).toInt()
    var offset: Int

    init {
        if (isSizeOk(value)) offset = value
        else throw InvalidOffsetException()
    }

    private fun isSizeOk(value: Int): Boolean = value < MAX_SIZE
}