package structs

import structs.ProcessState.BLOCKED
import structs.ProcessState.READY
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

interface ProcessControlBlockContract {
    var priority: ProcessPriority
    var pid: Int
    var state: ProcessState
    var parent: Int?
    var children: LinkedList<ProcessControlBlockContract>
    var resources: HashMap<Int, Int>
    var blockerRid: Int?
    fun create(priority: ProcessPriority): ProcessControlBlock
    fun getResource(rid: Int, howMany: Int)
    fun releaseResource(rid: Int, howMany: Int)
    fun block(rid: Int)
    fun free()
}

class ProcessControlBlock(override var priority: ProcessPriority) : ProcessControlBlockContract {

    override var pid = createPid()
    override var state = READY
    override var parent: Int? = null
    override var children: LinkedList<ProcessControlBlockContract> = LinkedList()
    override var resources: HashMap<Int, Int> = HashMap()
    override var blockerRid: Int? = null

    override fun create(priority: ProcessPriority): ProcessControlBlock =
        ProcessControlBlock(priority).also {
            it.parent = this.pid
            this.children.add(it)
        }

    override fun getResource(rid: Int, howMany: Int) {
        if (!resources.contains(rid)){
            resources[rid] = howMany
        } else {
            val sum = resources[rid]!! + howMany
            resources[rid] = sum
        }
    }

    override fun releaseResource(rid: Int, howMany: Int) {
        resources[rid] = resources[rid]!! - howMany
    }

    override fun block(rid: Int) {
        blockerRid = rid
        state = BLOCKED
    }

    override fun free() {
        blockerRid = null
        state = READY
    }

    private fun createPid(): Int {
        PROCESS_COUNT++
        return PROCESS_COUNT
    }

    override fun toString(): String =
        "Current process id: $pid\n" +
                "Priority level: $priority\n" +
                "Child ids: ${getChildrenPidsAsList()}\n" +
                "Parent process id: $parent\n" +
                "Resource ids: $resources"

    private fun getChildrenPidsAsList(): ArrayList<Int> {
        val childrenList: ArrayList<Int> = arrayListOf()
        children.forEach {
            childrenList.add(it.pid)
        }
        return childrenList
    }

    companion object {
        @JvmStatic
        var PROCESS_COUNT: Int = -1
    }
}