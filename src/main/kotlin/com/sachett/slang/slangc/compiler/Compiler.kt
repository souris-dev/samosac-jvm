package com.sachett.slang.slangc.compiler

import com.sachett.slang.logging.err
import com.sachett.slang.parser.SlangLexer
import com.sachett.slang.parser.SlangParser
import com.sachett.slang.slangc.codegen.ClassFileGenerator
import com.sachett.slang.slangc.staticchecker.StaticTypesChecker
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

import kotlinx.coroutines.*

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        err("slangc: No input files!")
    }

    val inputStream = CharStreams.fromFileName(args[0])
    val slangGrammarLexer = SlangLexer(inputStream)
    val commonTokenStream = CommonTokenStream(slangGrammarLexer)
    val slangParser = SlangParser(commonTokenStream)

    val programContext = slangParser.program()

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