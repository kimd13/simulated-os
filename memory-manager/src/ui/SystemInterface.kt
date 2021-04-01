package ui

import exceptions.CriticalUserException
import exceptions.InvalidInputException
import exceptions.NoInputException
import exceptions.SegmentationFaultException
import structs.*
import structs.FrameValue.Companion.FRAME_SIZE
import java.io.File
import kotlin.math.abs
import kotlin.system.exitProcess

class SystemInterface {

    val FRAMES_IN_MEMORY = 1024
    val BLOCKS_IN_DISK = 1024

    init {
        Disk.initialize(BLOCKS_IN_DISK)
        PhysicalMemory.initialize(FRAMES_IN_MEMORY)
        PageManager.initialize()
        MemoryManagementUnit.initialize()
        createDefaultSegmentTable()
    }

    fun createDefaultSegmentTable() {
        PhysicalMemory.INSTANCE!!.setSegmentTable(0)
        PhysicalMemory.INSTANCE!!.setSegmentTable(1)
    }

    fun populateWithFile(fileName: String) {
        val fileLines = File(fileName).readLines()
        assert(fileLines.size == 2)
        populateSegmentTable(fileLines[0].split(" "))
        populatePageTable(fileLines[1].split(" "))
    }

    fun translateWithFile(fileName: String) {
        File("output-dp.txt").printWriter().use { out ->
            val fileLines = File(fileName).readLines()
            assert(fileLines.size == 1)
            fileLines[0].split(" ").forEach {
                try {
                    val pa = MemoryManagementUnit.INSTANCE!!.translate(VirtualAddress(it.toInt()))
                    out.print(pa)
                    out.print(" ")
                } catch (e: SegmentationFaultException) {
                    out.print(-1)
                    out.print(" ")
                } catch (e: NoInputException) {
                    out.println()
                }
            }
        }
    }

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
                }
            }
        }
    }

    private fun verifyUserInput(input: String?) {
        if (input.isNullOrEmpty()) throw NoInputException()
        else {
            val separatedInput = input.split(" ")
            when (separatedInput[0]) {
                "add" -> runCommandAdd(separatedInput)
                "check" -> runCommandCheck(separatedInput)
                "type" -> runCommandType(separatedInput)
                "trans" -> runCommandTrans(separatedInput)
                "exit" -> exitProcess(0)
                else -> throw InvalidInputException()
            }
        }
    }

    private fun runCommandAdd(args: List<String>) {
        //if (args.subList(2, args.size).size % 3 == 0) throw IllegalArgumentException()
        when (args[1]) {
            "-s" -> populateSegmentTable(args.subList(2, args.size))
            "-p" -> populatePageTable(args.subList(2, args.size))
            else -> throw InvalidInputException()
        }
    }

    private fun runCommandCheck(args: List<String>) {
        if (args.size != 2) throw IllegalArgumentException()
        val index = args[1].toInt()
        println(
            "value : ${PhysicalMemory.INSTANCE!!.get(index)} | frame #: ${
                PhysicalMemory.INSTANCE!!.findFrameIndex(
                    index
                )
            } | word #: ${PhysicalMemory.INSTANCE!!.findWordIndex(index)}"
        )
    }

    private fun runCommandType(args: List<String>) {
        if (args.size != 2) throw IllegalArgumentException()
        println(PhysicalMemory.INSTANCE!!.getFrame(args[1].toInt()))
    }

    private fun runCommandTrans(args: List<String>) {
        if (args.size != 2) throw IllegalArgumentException()
        val va = VirtualAddress(args[1].toInt())
        val pa = MemoryManagementUnit.INSTANCE!!.translate(VirtualAddress(args[1].toInt()))
        println("pa: ${pa} | va: ${va}")
    }

    private fun intro() {
        println(
            "Welcome to my memory-manager project\n" +
                    "These are the correct commands to run:\n" +
                    "####################################################################################\n" +
                    "add                   -Adds to physical memory\n" +
                    "    -s {s z f ...}     -s adds to segment table\n" +
                    "                       'page table of segment s resides in frame f and the length of segment s is z'\n" +
                    "    -p {s p f ...}     -p adds to page tables\n" +
                    "                       'page p of segment s resides in frame f'\n" +
                    "trans {va}            -Translates virtual address to physical address\n" +
                    "check {index}         -Checks for value in index of memory\n" +
                    "type {frame #}        -Prints the type of frame, either a PAGE, PAGE TABLE, SEGMENT TABLE\n" +
                    "exit                  -Exits the program\n" +
                    "####################################################################################"
        )
    }

    private fun populateSegmentTable(populateList: List<String>) {
        //[0]s(segment number) [1]z(size) [2]f(Page Table in frame f)...
        val chunks = populateList.chunked(3)
        chunks.forEach {
            val initialIndex = 2 * it[0].toInt()
            PhysicalMemory.INSTANCE!!.apply {
                set(initialIndex, it[1].toInt())
                it[2].toInt().apply {
                    set(initialIndex + 1, this)
                    if (this >= 0) setPageTable(this)
                }
            }
        }
    }

    private fun populatePageTable(populateList: List<String>) {
        //[0]s(segment number) [1]p(page number) [2]f(page resides in frame f)...
        val chunks = populateList.chunked(3)
        chunks.forEach {
            val pageTableFrame = PhysicalMemory.INSTANCE!!.get(2 * it[0].toInt() + 1)
            if (pageTableFrame >= 0) {
                val pageTableIndex = pageTableFrame * FRAME_SIZE + it[1].toInt()
                it[2].toInt().apply {
                    PhysicalMemory.INSTANCE!!.set(pageTableIndex, it[2].toInt())
                    if (this >= 0) PhysicalMemory.INSTANCE!!.setPage(this)
                    //else page is in disk
                }
            } else {
                Disk.INSTANCE!!.write_block_word(abs(pageTableFrame), it[1].toInt(), it[2].toInt())
                it[2].toInt().apply {
                    if (this >= 0) PhysicalMemory.INSTANCE!!.setPage(this)
                    //else page is in disk
                }
            }
        }
    }
}