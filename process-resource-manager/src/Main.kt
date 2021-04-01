import ui.ShellInterface

/**
 * Main user the user input shell if no file argument is given
 */
fun main(args: Array<String>) {
    assert(args.size == 1 || args.isEmpty())
    val shell = ShellInterface()
    when (args.size) {
        0 -> shell.run()
        1 -> shell.run(args[0])
    }
}