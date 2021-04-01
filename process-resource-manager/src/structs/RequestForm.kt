package structs

data class RequestForm(val pcb: ProcessControlBlockContract, var requested: Int) {
    override fun toString(): String {
        return "[Process: ${pcb.pid}, Requested: $requested]"
    }
}