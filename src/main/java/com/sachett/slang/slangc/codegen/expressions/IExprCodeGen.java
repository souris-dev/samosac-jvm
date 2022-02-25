package com.sachett.slang.slangc.codegen.expressions;

import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
import com.sachett.slang.slangc.symbol.ISymbol;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public interface IExprCodeGen {
    void doCodeGen();

    default void doIdentifierCodegen(
            String idName,
            SymbolTable symbolTable,
            Type type,
            FunctionCodeGen functionCodeGen,
            String qualifiedClassName,
            int loadInstruction
    ) {
        Pair<ISymbol, Integer> lookupInfo = symbolTable.lookupWithNearestScopeValue(idName);
        if (lookupInfo.getFirst() == null) {
            // lookup failed
            return;
        }

        if (lookupInfo.getSecond() == 0) {
            // we're talking about a global variable
            // (a static field of the class during generation)
            functionCodeGen.getMv().visitFieldInsn(
                    Opcodes.GETSTATIC, qualifiedClassName, idName, type.getDescriptor()
            );
        } else {
            Integer localVarIndex = functionCodeGen.getLocalVarIndex(idName);
            functionCodeGen.getMv().visitVarInsn(loadInstruction, localVarIndex);
        }
    }
}
