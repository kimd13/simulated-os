package structs

import exceptions.InvalidVirtualAddressException
import structs.FrameValue.Companion.FRAME_SIZE
import kotlin.math.pow
import kotlin.math.sqrt

class VirtualAddress(address: Int){

    val MAX_SIZE = 2.0.pow(27).toInt()
    //Offset have 9 bit values, so we need a value of 111 111 111 to perform and ops
    //111 111 111 = 511
    private val AND_VALUE = 511

    val address = address
    val segmentTableOffset: Offset
    val pageTableOffset: Offset
    val pageOffset: Offset
    val segmentBoundary: Int

    init {
        if (!isValidAddress(address)) throw InvalidVirtualAddressException()
        address.apply {
            segmentTableOffset = getSegmentTableOffset(this)
            pageTableOffset = getPageTableOffset(this)
            pageOffset = getPageOffset(this)
            segmentBoundary = getSegmentBoundary(this)
        }
    }

    private fun isValidAddress(address: Int): Boolean =
        address < MAX_SIZE

    private fun getSegmentTableOffset(address: Int): Offset =
        //right shift by 18 bits
        Offset(address.shr(18))

    private fun getPageTableOffset(address: Int): Offset{
        //right shift by 9 bits to get rid of page offset
        val ridPageOffset = address.shr(9)
        //and with 111 111 111
        return Offset(ridPageOffset.and(AND_VALUE))
    }

    private fun getPageOffset(address: Int): Offset =
        Offset(address.and(AND_VALUE))

    private fun getSegmentBoundary(address: Int): Int{
        //111 111 111 111 111 1111
        val and_concat = ((AND_VALUE + 1) * (AND_VALUE + 1)) -1
        return address.and(and_concat)
    }

    override fun toString(): String {
        return "address: ${address}, segment table offset: ${segmentTableOffset.offset}, page table offset: ${pageTableOffset.offset}" +
                ", page offset: ${pageOffset.offset}, segment boundary: ${segmentBoundary}"
    }
}
