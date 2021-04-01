package structs

import exceptions.NoInstantiationException

class PageManager(memorySize: Int){

    var free_frames = Array<Boolean>(memorySize){true}

    fun alertFrameTaken(memoryIndex: Int) {
        free_frames[memoryIndex] = false
        //println("frame: ${memoryIndex} taken")
    }

    fun handlePageFault(memoryLocation: Int, diskIndex: Int, isPageTable: Boolean){
        val frameIndex = findFreeFrame()
        if (frameIndex != null){
            val disk_value = Disk.INSTANCE!!.read_block(diskIndex)
            diskToFreeFrame(disk_value, frameIndex, isPageTable, memoryLocation)
        } else replacePage()

    }

    private fun diskToFreeFrame(value: IntArray, frameIndex: Int, isPageTable: Boolean, memoryLocation: Int) =
        PhysicalMemory.INSTANCE!!.apply {
            if (isPageTable) setPageTable(frameIndex)
            else setPage(frameIndex)
            getFrame(frameIndex).words = value
            set(memoryLocation, frameIndex)
        }

    private fun findFreeFrame(): Int?{
        var index: Int = 0
        free_frames.forEach {
            if (it) return index
            else index++
        }
        return null
    }

    private fun replacePage() {}

    companion object {
        @Volatile
        var INSTANCE: PageManager? = null

        @JvmStatic
        fun get(): PageManager = INSTANCE ?: throw NoInstantiationException(this)

        @JvmStatic
        fun initialize(): PageManager {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = PageManager(PhysicalMemory.SIZE!!)
                    }
                }
            }
            return INSTANCE!!
        }
    }

}