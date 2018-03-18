package com.github.oshai

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File
import java.io.PrintWriter
import java.util.Date
import java.util.concurrent.TimeUnit

class CommandLineArgs(parser: ArgParser) {
  val verbose by parser.flagging("-v", "--verbose",
      help = "enable verbose mode")

  val file by parser.storing("-f", "--file",
      help = "name of the file to append log into")

  val lines by parser.storing("-l", "--lines",
      help = "max num of lines to append") { toInt() }.default(1_000_000)

  val rotate by parser.storing("-r", "--rotate",
      help = "max num of files to rotate") { toInt() }.default(20)
}

val FLUSH_RATIO = 1
val FLUSH_SECONDS: Long = 1

fun main(args: Array<String>) {
  val parsedArgs = CommandLineArgs(ArgParser(args))
  println("verbose=${parsedArgs.verbose} file=${parsedArgs.file} lines=${parsedArgs.lines} rotate=${parsedArgs.rotate}")
  var linesCounter = 0
  var fileHandle = rotateFile(parsedArgs, null)
  var lastFlush = System.currentTimeMillis()
  while (true) {
    linesCounter++
    if (linesCounter % parsedArgs.lines == 0) {
      fileHandle = rotateFile(parsedArgs, fileHandle)
    }
    val line = readLine() ?: return
    //println(line)
    fileHandle.write(line)
    fileHandle.write("\n")
    if (linesCounter % FLUSH_RATIO == 0 || System.currentTimeMillis() - lastFlush > TimeUnit.SECONDS.toMillis(FLUSH_SECONDS)) {
      fileHandle.flush()
      lastFlush = System.currentTimeMillis()
    }
  }
}

fun rotateFile(parsedArgs: CommandLineArgs, fileHandle: PrintWriter?): PrintWriter {
  fileHandle?.flush()
  fileHandle?.close()
  //rotate
  println("${Date()} - rotating files")
  //first delete last file
  val lastFile = File("${parsedArgs.file}.${parsedArgs.rotate}")
  if (lastFile.exists()) {
    println("${Date()} - delete ${parsedArgs.file}.${parsedArgs.rotate}")
    lastFile.delete()
  }
  for (i in parsedArgs.rotate downTo 1) {
    val currentFile = File("${parsedArgs.file}.$i")
    if (currentFile.exists()) {
      currentFile.renameTo(File("${parsedArgs.file}.${i+1}"))
    }
  }
  val firstFile = File(parsedArgs.file)
  if (firstFile.exists()) {
    firstFile.renameTo(File("${parsedArgs.file}.1"))
  }
  return File(parsedArgs.file).printWriter()
}



