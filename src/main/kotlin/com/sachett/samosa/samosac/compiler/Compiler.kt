package com.sachett.samosa.samosac.compiler

import com.sachett.samosa.logging.err
import com.sachett.samosa.parser.SamosaLexer
import com.sachett.samosa.parser.SamosaParser
import com.sachett.samosa.samosac.codegen.ClassFileGenerator
import com.sachett.samosa.samosac.staticchecker.StaticTypesChecker
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

import kotlinx.coroutines.*

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        err("samosac: No input files!")
    }

    val inputStream = CharStreams.fromFileName(args[0])
    val samosaGrammarLexer = SamosaLexer(inputStream)
    val commonTokenStream = CommonTokenStream(samosaGrammarLexer)
    val samosaParser = SamosaParser(commonTokenStream)

    val programContext = samosaParser.program()

    val symbolTable = SymbolTable()

    println("Visiting declarations...")
    val staticTypesChecker = StaticTypesChecker(symbolTable)
    staticTypesChecker.visit(programContext)

    println("Beginning class file generation")
    // parallel generation of class files
    runBlocking {
        repeat (args.size) {
            launch {
                val classFileGenerator = ClassFileGenerator(programContext, args[0], symbolTable)
                classFileGenerator.generateClass()
                classFileGenerator.writeClass()
            }
        }
    }
}