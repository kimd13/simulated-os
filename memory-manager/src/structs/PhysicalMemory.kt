package structs

import exceptions.NoInstantiationException
import exceptions.PhysicalMemoryOutOfBoundsException
import structs.FrameValue.Companion.FRAME_SIZE

/**
 * Physical memory is instantiated given the number of frames it should contain
 */
class PhysicalMemory private constructor(size: Int) {

    private var frames: Array<FrameValue> = Array(size) { Page() }

    fun setSegmentTable(index: Int) {
        if (!indexWithinBounds(index)) throw PhysicalMemoryOutOfBoundsException()
        frames[index] = SegmentTable()
        PageManager.INSTANCE!!.alertFrameTaken(index)
    }

    fun setPageTable(index: Int) {
        if (!indexWithinBounds(index)) throw PhysicalMemoryOutOfBoundsException()
        frames[index] = PageTable()
        PageManager.INSTANCE!!.alertFrameTaken(index)
    }

    fun setPage(index: Int){
        if (!indexWithinBounds(index)) throw PhysicalMemoryOutOfBoundsException()
        frames[index] = Page()
        PageManager.INSTANCE!!.alertFrameTaken(index)
    }

    fun getFrame(index: Int): FrameValue = frames[index]

    fun get(index: Int): Int {
        if (!indexWithinBounds(index)) throw PhysicalMemoryOutOfBoundsException()
        return frames[findFrameIndex(index)].words[findWordIndex(index)]
    }

    fun set(index: Int, value: Int) {
        if (!indexWithinBounds(index)) throw PhysicalMemoryOutOfBoundsException()
        frames[findFrameIndex(index)].words[findWordIndex(index)] = value
    }

    fun findFrameIndex(index: Int): Int = Math.floorDiv(index, FRAME_SIZE)

    fun findWordIndex(index: Int): Int = index.rem(FRAME_SIZE)

    private fun indexWithinBounds(index: Int): Boolean {
        //index must be within the bounds decided by the number of frames assigned to physical memory
        val bounds = SIZE!! * FRAME_SIZE
        return index < bounds
    }

    companion object {
        @Volatile
        var INSTANCE: PhysicalMemory? = null

        @JvmStatic
        var SIZE: Int? = null

        @JvmStatic
        fun get(): PhysicalMemory = INSTANCE ?: throw NoInstantiationException(this)

        @JvmStatic
        fun initialize(size: Int): PhysicalMemory {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = PhysicalMemory(size)
                        SIZE = size
                    }
                }
            }
            return INSTANCE!!
        }
    }
}