package structs

import exceptions.NoInstantiationException
import exceptions.SegmentationFaultException
import structs.FrameValue.Companion.FRAME_SIZE
import kotlin.math.abs


class MemoryManagementUnit() {

    val tlb: TranslationLookAsideBuffer = TranslationLookAsideBuffer()

    fun translate(va: VirtualAddress): PhysicalAddress? {
        if (notWithinSegmentBoundary(va)) throw SegmentationFaultException()
        else {
            val pageTableFrame = getPageTableFrame(va)
            val pageFrame = getPageFrame(va, pageTableFrame)
            val pageOffset = getPageOffset(va, pageFrame)
            return PhysicalAddress(pageOffset)
        }
    }

    private fun getPageTableFrame(va: VirtualAddress): Int{
        var pageTableFrameLocation = 2 * va.segmentTableOffset.offset + 1
        var pageTableFrame = PhysicalMemory.INSTANCE!!.get(pageTableFrameLocation)
        if (pageTableFrame < 0){
            PageManager.INSTANCE!!.handlePageFault(pageTableFrameLocation, abs(pageTableFrame), true)
            pageTableFrame = PhysicalMemory.INSTANCE!!.get(pageTableFrameLocation)
        }
        return pageTableFrame
    }

    private fun getPageFrame(va: VirtualAddress, pageTableFrame: Int): Int{
        val pageFrameLocation = pageTableFrame * FRAME_SIZE + va.pageTableOffset.offset
        var pageFrame = PhysicalMemory.INSTANCE!!.get(pageFrameLocation)
        if(pageFrame < 0){
            PageManager.INSTANCE!!.handlePageFault(pageFrameLocation, abs(pageFrame), false)
            pageFrame = PhysicalMemory.INSTANCE!!.get(pageFrameLocation)
        }
        return pageFrame
    }

    private fun getPageOffset(va: VirtualAddress, pageFrame: Int) : Int =
        pageFrame * FRAME_SIZE + va.pageOffset.offset

    private fun notWithinSegmentBoundary(va: VirtualAddress) =
        va.segmentBoundary >= PhysicalMemory.INSTANCE!!.get(2 * va.segmentTableOffset.offset!!)

    companion object {
        @Volatile
        var INSTANCE: MemoryManagementUnit? = null

        @JvmStatic
        fun get(): MemoryManagementUnit = INSTANCE ?: throw NoInstantiationException(this)

        @JvmStatic
        fun initialize(): MemoryManagementUnit {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = MemoryManagementUnit()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}