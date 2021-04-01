package structs

import Exceptions.InvalidProcessStateException
import Exceptions.NoInstantiationException
import structs.ProcessPriority.*
import structs.ProcessState.PROCESS_ZERO
import structs.ProcessState.READY
import java.util.*

interface ProcessReadyListContract {
    fun add(pcb: ProcessControlBlockContract)
    fun remove(pcb: ProcessControlBlockContract)
    fun clear()
}

class ProcessReadyList private constructor() : ProcessReadyListContract {

    private var high_priority_list: LinkedList<ProcessControlBlockContract> = LinkedList()
    private var mid_priority_list: LinkedList<ProcessControlBlockContract> = LinkedList()
    private var low_priority_list: LinkedList<ProcessControlBlockContract> = LinkedList()

    override fun add(pcb: ProcessControlBlockContract) {
        checkState(pcb)
        when (pcb.priority) {
            HIGH -> high_priority_list.add(pcb)
            MID -> mid_priority_list.add(pcb)
            LOW -> low_priority_list.add(pcb)
        }
    }

    private fun checkState(pcb: ProcessControlBlockContract) {
        if (pcb.state == PROCESS_ZERO) {
            return
        }
        if (pcb.state != READY) throw InvalidProcessStateException()
    }

    override fun remove(pcb: ProcessControlBlockContract) {
        if (!high_priority_list.remove(pcb))
            if (!mid_priority_list.remove(pcb))
                low_priority_list.remove(pcb)
    }

    override fun clear() {
        high_priority_list.clear()
        mid_priority_list.clear()
        low_priority_list.clear()
    }

    fun getHighestPriorityProcess(): ProcessControlBlockContract =
        when {
            high_priority_list.isNotEmpty() -> high_priority_list.removeFirst()
            mid_priority_list.isNotEmpty() -> mid_priority_list.removeFirst()
            else -> low_priority_list.removeFirst()
        }

    fun peekHighestPriorityProcess(): ProcessControlBlockContract =
        when {
            high_priority_list.isNotEmpty() -> high_priority_list.peekFirst()
            mid_priority_list.isNotEmpty() -> mid_priority_list.peekFirst()
            else -> low_priority_list.peekFirst()
        }

    private fun prettyString(list: LinkedList<ProcessControlBlockContract>): String {
        return if (list.isNotEmpty()) {
            var prettyString = ""
            for (i in list) {
                prettyString += "${i.pid}->"
            }
            prettyString.substring(0, prettyString.length - 2)
        } else ""
    }

    override fun toString(): String {
        return "HIGH: " + prettyString(high_priority_list) + "\n" +
                "MID: " + prettyString(mid_priority_list) + "\n" +
                "LOW: " + prettyString(low_priority_list)
    }

    companion object {

        @Volatile
        var INSTANCE: ProcessReadyList? = null

        @JvmStatic
        fun get(): ProcessReadyList =
            INSTANCE ?: throw NoInstantiationException(this)

        @JvmStatic
        fun initialize(): ProcessReadyList {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = ProcessReadyList()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}