package structs

import Exceptions.*
import structs.ProcessPriority.*
import structs.ProcessState.*

interface ManagerContract {
    var current_process: ProcessControlBlockContract
    fun createProcess(priority: Int)
    fun deleteProcess(pid: Int)
    fun releaseResource(rid: Int, requested: Int)
    fun requestResource(rid: Int, requested: Int)
    fun timeout()
    fun restart()
}

class Manager private constructor(
    private var processes: HashMap<Int, ProcessControlBlockContract> = HashMap(NUMBER_OF_PROCESSES),
    private var resources: HashMap<Int, ResourceControlBlockContract> = HashMap(NUMBER_OF_RESOURCES),
    private var ready_list: ProcessReadyList = ProcessReadyList.initialize(),
    override var current_process: ProcessControlBlockContract = ProcessControlBlock(LOW)
) : ManagerContract, Scheduler {

    init {
        initialize()
    }

    private fun initialize() {
        current_process.state = PROCESS_ZERO
        processes[current_process.pid] = current_process
        ready_list.add(current_process)
        initializeResources()
    }

    private fun initializeResources() {
        val howMany = 4
        for (r in 0 until howMany) {
            val resource: ResourceControlBlockContract
            resource = if (r == 0) {
                ResourceControlBlock(1)
            } else {
                ResourceControlBlock(r)
            }
            resources[r] = resource
        }
    }

    @Throws(CriticalUserException::class)
    override fun createProcess(priority: Int) {
        val priorityState = intToProcessPriority(priority)
        if (priorityState == LOW) throw IllegalPriorityException()
        val process = current_process.create(priorityState)
        processes[process.pid] = process
        ready_list.add(process)
        contextSwitch()
    }

    @Throws(CriticalUserException::class)
    override fun deleteProcess(pid: Int) {
        if (!isChildOf(pid)) throw ChildProcessDoesNotExistException()
        val process = processes[pid]!!
        deleteFromParentChildren(process)
        for (p in process.children) {
            deleteHelper(p.pid)
        }
        deleteProcessTrace(process)
        contextSwitch()
    }

    private fun deleteHelper(pid: Int) {
        val process = processes[pid]
        for (p in processes[pid]!!.children) {
            deleteHelper(p.pid)
        }
        deleteProcessTrace(process!!)
    }

    private fun deleteProcessTrace(pcb: ProcessControlBlockContract) {
        if (pcb.state == BLOCKED) {
            deleteFromWaitList(pcb)
        } else deleteFromReadyList(pcb)
        freeResourcesOnDelete(pcb)
        deleteFromProcesses(pcb)
    }

    private fun deleteFromParentChildren(pcb: ProcessControlBlockContract) =
        processes[pcb.parent]!!.children.remove(pcb)

    private fun deleteFromWaitList(pcb: ProcessControlBlockContract) {
        if (pcb.blockerRid != null) {
            val blocker = pcb.blockerRid
            resources[blocker]!!.removeFromWaitList(pcb)
        }
    }

    private fun deleteFromReadyList(pcb: ProcessControlBlockContract) =
        ready_list.remove(pcb)

    private fun freeResourcesOnDelete(pcb: ProcessControlBlockContract) {
        pcb.resources.forEach { (k, v) ->
            releaseAndAdd(k, v)
        }
    }

    private fun deleteFromProcesses(pcb: ProcessControlBlockContract) =
        processes.remove(pcb.pid)

    private fun isChildOf(pid: Int): Boolean =
        current_process.children.contains(processes[pid])

    @Throws(CriticalUserException::class)
    override fun releaseResource(rid: Int, requested: Int) {
        if (!resources.containsKey(rid)) throw ResourceDoesNotExistException()
        if (!current_process.resources.contains(rid)) throw ProcessDoesNotHaveResourceException()
        if (current_process.resources[rid]!! < requested) throw ReleaseDeniedException()
        else {
            current_process.resources.remove(rid)
            releaseAndAdd(rid, requested)
        }
        contextSwitch()
    }

    private fun releaseAndAdd(rid: Int, requested: Int) {
        val released = resources[rid]!!.release(requested)
        released.forEach {
            ready_list.add(it.pcb.apply {
                getResource(rid, it.requested)
            })
        }
    }

    @Throws(CriticalUserException::class)
    override fun requestResource(rid: Int, requested: Int) {
        if (isInitialProcess()) throw ProcessZeroRequestDeniedException()
        if (!resources.containsKey(rid)) throw ResourceDoesNotExistException()
        else {
            if (resources[rid]!!.request(current_process, requested)) {
                current_process.getResource(rid, requested)
            } else {
                contextSwitch()
            }
        }
    }

    override fun timeout() {
        if (!isInitialProcess()) {
            current_process.state = READY
            ready_list.add(current_process)
            current_process = ready_list.getHighestPriorityProcess()
        }
        contextSwitch()
    }

    override fun restart() {
        clear()
        initialize()
    }

    override fun contextSwitch() {
        val readyListTopProcess = ready_list.peekHighestPriorityProcess()
        when {
            current_process.state == BLOCKED -> currentProcessBlocked()
            readyListTopProcess.pid == current_process.pid -> topProcessIsCurrentProcess()
            readyListTopProcess.priority > current_process.priority -> topProcessIsGreater()
            else -> currentProcessUnchanged()
        }
    }

    private fun currentProcessBlocked() {
        current_process = ready_list.getHighestPriorityProcess()
        if (isInitialProcess()) {
            current_process.apply {
                state = PROCESS_ZERO
                ready_list.add(current_process)
            }
        } else ready_list.remove(current_process.apply { state = RUNNING })
    }

    private fun topProcessIsCurrentProcess() {
        if (!isInitialProcess()) {
            ready_list.remove(current_process)
            current_process.state = RUNNING
        }
    }

    private fun topProcessIsGreater() {
        if (!isInitialProcess()) {
            ready_list.add(current_process.apply {
                state = READY
            })
        }
        current_process = ready_list.getHighestPriorityProcess().apply {
            state = RUNNING
        }
    }

    private fun currentProcessUnchanged() =
        ready_list.remove(current_process).also { current_process.state = RUNNING }

    fun currentProcessStatus(): String =
        current_process.toString()

    fun readyListStatus(): String =
        ready_list.toString()

    fun processesStatus(): String {
        var processesString = ""
        processes.forEach { k, v ->
            processesString += "$k: ${v.state} | holds: ${v.resources}\n"
        }
        return processesString.trim()
    }

    fun resourcesStatus(): String {
        var resourcesString = ""
        resources.forEach { k, v ->
            resourcesString += "$k: inventory: ${v.number_of_resources} | available: ${v.number_of_resources_available} " +
                    "| waitlist: ${v.process_waitlist}\n"
        }
        return resourcesString.trim()
    }

    private fun intToProcessPriority(priority: Int): ProcessPriority =
        when (priority) {
            0 -> LOW
            1 -> MID
            2 -> HIGH
            else -> throw IllegalPriorityException()
        }

    private fun isInitialProcess() =
        current_process.pid == 0

    private fun clear() {
        processes.clear()
        resources.clear()
        ready_list.clear()
        ProcessControlBlock.PROCESS_COUNT = -1
        ResourceControlBlock.RESOURCE_COUNT = -1
        current_process = ProcessControlBlock(LOW)
    }

    companion object {

        @Volatile
        var INSTANCE: Manager? = null

        @JvmStatic
        val NUMBER_OF_PROCESSES = 15
        @JvmStatic
        val NUMBER_OF_RESOURCES = 4

        @JvmStatic
        fun get(): Manager =
            INSTANCE ?: throw NoInstantiationException(this)

        @JvmStatic
        fun initialize(): Manager {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = Manager()
                    }
                }
            }
            return INSTANCE!!
        }
    }

}