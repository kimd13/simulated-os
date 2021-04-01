package structs

import exceptions.InvalidPhysicalAddressException
import kotlin.math.pow

class PhysicalAddress(value: Int){

    val MAX_SIZE = 2.0.pow(19).toInt()
    var address: Int

    init {
        if (isValidAddress(value)) address = value
        else throw InvalidPhysicalAddressException()
    }

    private fun isValidAddress(address: Int): Boolean =
        address < MAX_SIZE

    override fun toString(): String {
        return address.toString()
    }
}