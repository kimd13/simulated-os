package structs

import exceptions.NoInstantiationException
import structs.FrameValue.Companion.FRAME_SIZE

class Disk private constructor(size: Int) {

    //disk blocks sizes must match frame sizes
    val BLOCK_SIZE = FRAME_SIZE

    var blocks: Array<IntArray> = Array(size) { IntArray(BLOCK_SIZE) }

    fun read_block(index: Int): IntArray =
        blocks[index]

    fun write_block(index: Int, value: IntArray) {
        blocks[index] = value
    }

    fun read_block_word(blockIndex: Int, wordIndex: Int): Int =
        blocks[blockIndex][wordIndex]

    fun write_block_word(blockIndex: Int, wordIndex: Int, value: Int){
        blocks[blockIndex][wordIndex] = value
    }

    companion object {
        @Volatile
        var INSTANCE: Disk? = null

        @JvmStatic
        fun get(): Disk = INSTANCE ?: throw NoInstantiationException(this)

        @JvmStatic
        fun initialize(size: Int): Disk {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = Disk(size)
                    }
                }
            }
            return INSTANCE!!
        }
    }

}