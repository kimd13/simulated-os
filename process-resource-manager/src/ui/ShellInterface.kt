package ui

import Exceptions.*
import structs.Manager
import structs.ManagerContract
import java.io.File
import kotlin.system.exitProcess

class ShellInterface {

    private val manager: ManagerContract = Manager.initialize()

    private fun verifyUserInput(input: String?) {
        if (input.isNullOrEmpty()) throw NoInputException()
        else {
            val separatedInput = input.split(" ")
            when (separatedInput[0]) {
                "cr" -> runCommandCr(separatedInput)
                "de" -> runCommandDe(separatedInput)
                "rq" -> runCommandRq(separatedInput)
                "rl" -> runCommandRl(separatedInput)
                "to" -> runCommandTo(separatedInput)
                "in" -> runCommandIn(separatedInput)
                "status" -> runCommandStatus(separatedInput)
                "exit" -> exitProcess(0)
                else -> throw InvalidInputException()
            }
        }
    }

    private fun runCommandCr(args: List<String>) {
        if (args.size != 2) throw IllegalArgumentException()
        val priority = args[1].toInt()
        manager.createProcess(priority)
    }

    private fun runCommandDe(args: List<String>) {
        if (args.size != 2) throw IllegalArgumentException()
        val pid = args[1].toInt()
        manager.deleteProcess(pid)
    }

    private fun runCommandRq(args: List<String>) {
        if (args.size != 3) throw IllegalArgumentException()
        val rid = args[1].toInt()
        val requested = args[2].toInt()
        manager.requestResource(rid, requested)
    }

    private fun runCommandRl(args: List<String>) {
        if (args.size != 3) throw IllegalArgumentException()
        val rid = args[1].toInt()
        val requested = args[2].toInt()
        manager.releaseResource(rid, requested)
    }

    private fun runCommandTo(args: List<String>) {
        if (args.size != 1) throw IllegalArgumentException()
        manager.timeout()
    }

    private fun runCommandIn(args: List<String>) {
        if (args.size != 1) throw IllegalArgumentException()
        manager.restart()
    }

    private fun runCommandStatus(args: List<String>) {
        if (args.size != 2) throw IllegalArgumentException()
        val managerCast = manager as Manager
        when (args[1]) {
            "c" -> println(managerCast.currentProcessStatus())
            "r" -> println(managerCast.resourcesStatus())
            "p" -> println(managerCast.processesStatus())
            "l" -> println(managerCast.readyListStatus())
            else -> throw java.lang.IllegalArgumentException()
        }
    }

    private fun intro() {
        println(
            "Welcome to my process-resource-manager project\n" +
                    "These are the correct commands to run:\n" +
                    "####################################################################################\n" +
                    "cr {priority}         -Create process with priority\n" +
                    "de {pid}              -Delete process pid\n" +
                    "rq {rid} {request #}  -Request amount request # from resource rid\n" +
                    "rl {rid} {release #}  -Release amount release # from resource rid\n" +
                    "to                    -Timeout a process\n" +
                    "in                    -Restores system to initial state\n" +
                    "status {c,r,p,l}        -Displays progress\n" +
                    "                       c[urrent process] | r[esources] | p[rocesses] | l[ready list]\n" +
                    "exit                  -Exits the program\n" +
                    "####################################################################################"
        )
    }


    /**
     * Run function with file name arg will output a text file in the current directory
     */
    fun run(fileName: String) {
        File("output.txt").printWriter().use { out ->
            File(fileName).forEachLine {
                try {
                    verifyUserInput(it)
                    out.print("${manager.current_process.pid} ")
                } catch (e: CriticalUserException) {
                    out.print(-1)
                } catch (e: NoInputException) {
                    out.println()
                }
            }
        }
    }

    /**
     * Run function without file name arg will wait for user input
     */
    fun run() {
        intro()
        while (true) {
            try {
                verifyUserInput(readLine())
            } catch (e: NoInputException) {
                println("No input given, please enter something...")
            } catch (e: InvalidInputException) {
                println("Invalid command, please try again...")
            } catch (e: IllegalArgumentException) {
                println("Invalid command argument, please try again...")
            } catch (e: CriticalUserException) {
                when (e) {
                    is ChildProcessDoesNotExistException -> customCriticalUserExceptionMessage("Cannot delete a non-child process...")
                    is ProcessDoesNotHaveResourceException -> customCriticalUserExceptionMessage("Process does not have the resource...")
                    is ProcessZeroRequestDeniedException -> customCriticalUserExceptionMessage("Process zero has no permissions to request resources...")
                    is ReleaseDeniedException -> customCriticalUserExceptionMessage("Resource release denied...")
                    is RequestDeniedException -> customCriticalUserExceptionMessage("Resource request denied...")
                    is ResourceDoesNotExistException -> customCriticalUserExceptionMessage("Resource does not exist...")
                    is IllegalPriorityException -> customCriticalUserExceptionMessage("Illegal priority entered...")
                }
            }
        }
    }


    private fun customCriticalUserExceptionMessage(message: String) {
        println(message)
        exitProcess(-1)
    }
}
