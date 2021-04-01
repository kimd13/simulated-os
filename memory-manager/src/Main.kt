import ui.SystemInterface

fun main(args: Array<String>) {
    assert(args.size == 2 || args.isEmpty())
    val systemInterface = SystemInterface()
    when (args.size) {
        0 -> systemInterface.run()
        2 -> {
            systemInterface.populateWithFile(args[0])
            systemInterface.translateWithFile(args[1])
        }
    }
}