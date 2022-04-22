package com.sachett.samosa.logging

import kotlin.system.exitProcess

enum class Severity {
    WARNING, ERROR, FATAL
}

/**
 * Fatal error.
 */
fun err(message: String): Nothing {
    System.err.println(message)
    exitProcess(-1)
}

fun fmtfatalerr(message: String, lineNumber: Int): Nothing {
    System.err.println("[Error, Line $lineNumber] $message")
    exitProcess(-1)
}

fun fmterror(message: String, lineNumber: Int, severity: Severity = Severity.FATAL) {
    when (severity) {
        Severity.WARNING -> {
            println("[Warning, Line ${lineNumber}] $message")
        }
        Severity.ERROR -> {
            System.err.println("[Error, Line ${lineNumber}] $message")
        }
        Severity.FATAL -> {
            System.err.println("[Error, Line ${lineNumber}] $message")
            exitProcess(-1)
        }
    }
}