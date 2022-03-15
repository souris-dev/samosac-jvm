package com.sachett.slang.slangc.codegen.expressions;

import com.sachett.slang.slangc.codegen.function.FunctionCodegen;
import com.sachett.slang.slangc.symbol.ISymbol;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public interface IExprCodegen {
    void doCodegen();

    default void doIdentifierCodegen(
            String idName,
            SymbolTable symbolTable,
            Type type,
            FunctionCodegen functionCodegen,
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
            functionCodegen.getMv().visitFieldInsn(
                    Opcodes.GETSTATIC, qualifiedClassName, idName, type.getDescriptor()
            );
        } else {
            Integer localVarIndex = functionCodegen.getLocalVarIndex(lookupInfo.getFirst().getAugmentedName());
            functionCodegen.getMv().visitVarInsn(loadInstruction, localVarIndex);
        }
    }
}
