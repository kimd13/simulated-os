package structs

import Exceptions.ReleaseDeniedException
import Exceptions.RequestDeniedException
import java.util.*
import kotlin.math.abs

interface ResourceControlBlockContract {
    var process_waitlist: LinkedList<RequestForm>
    val number_of_resources: Int
    var number_of_resources_available: Int
    val rid: Int
    fun request(pcb: ProcessControlBlockContract, requestAmount: Int): Boolean
    fun release(releaseAmount: Int): LinkedList<RequestForm>
    fun removeFromWaitList(pcb: ProcessControlBlockContract)
}

class ResourceControlBlock(
    override val number_of_resources: Int
) : ResourceControlBlockContract {

    override var number_of_resources_available: Int = number_of_resources
    override var process_waitlist: LinkedList<RequestForm> = LinkedList()
    override val rid: Int = createRid()

    private fun createRid(): Int {
        RESOURCE_COUNT++
        return RESOURCE_COUNT
    }

    override fun request(pcb: ProcessControlBlockContract, requestAmount: Int): Boolean {
        if (requestAmount > number_of_resources) throw RequestDeniedException()
        if (number_of_resources_available < requestAmount) {
            pcb.block(rid)
            process_waitlist.add(RequestForm(pcb, requestAmount))
            return false
        }
        number_of_resources_available -= requestAmount
        return true
    }

    override fun release(releaseAmount: Int): LinkedList<RequestForm> {
        if (releaseAmount + number_of_resources_available > number_of_resources) throw ReleaseDeniedException()
        number_of_resources_available += releaseAmount
        var released: LinkedList<RequestForm> = LinkedList()
        while (true) {
            try {
                val toBeReleased = process_waitlist.first
                val remainder = evaluateRequestForm(toBeReleased)
                when {
                    remainder == 0 -> {
                        number_of_resources_available = 0
                        unblockAndRemove(released, toBeReleased)
                        return released
                    }
                    remainder < 0 -> {
                        number_of_resources_available = 0
                        toBeReleased.requested = abs(remainder)
                        return released
                    }
                    else -> {
                        number_of_resources_available = remainder
                        unblockAndRemove(released, toBeReleased)
                    }
                }
            } catch (e: NoSuchElementException) {
                return released
            }
        }
    }

    override fun removeFromWaitList(pcb: ProcessControlBlockContract) {
        process_waitlist.forEach {
            if (it.pcb == pcb) {
                process_waitlist.remove(it)
            }
        }
    }

    private fun unblockAndRemove(released: LinkedList<RequestForm>, requestForm: RequestForm) {
        released.add(
            requestForm.apply {
                pcb.free()
            }
        )
        process_waitlist.removeFirst()
    }


    private fun evaluateRequestForm(form: RequestForm): Int =
        number_of_resources_available - form.requested

    override fun toString(): String =
        "Resource $rid | available: $number_of_resources_available | inventory: $number_of_resources"

    companion object {
        @JvmStatic
        var RESOURCE_COUNT: Int = -1
    }

}