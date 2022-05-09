package com.sachett.samosa.samosac.codegen.expressions;

import com.sachett.samosa.samosac.codegen.ClassFileGenerator;
import com.sachett.samosa.samosac.codegen.function.FunctionGenerationContext;
import com.sachett.samosa.samosac.symbol.ISymbol;
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public interface IExprCodegen {
    void doCodegen();

    default void doIdentifierCodegen(
            String idName,
            SymbolTable symbolTable,
            Type type,
            FunctionGenerationContext functionGenerationContext,
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
            // that should be looked up in the symbol table without the augmented name
            // (a static field of the class during generation)
            functionGenerationContext.getMv().visitFieldInsn(
                    Opcodes.GETSTATIC, qualifiedClassName, idName, type.getDescriptor()
            );
        }
        else if (lookupInfo.getSecond() != 0 && functionGenerationContext
                                                .getParentClassGenerator()
                                                .getStaticVarsAugmentedNames()
                                                .containsKey(
                                                        lookupInfo
                                                                .getFirst()
                                                                .getAugmentedName()))
        {
            // static variable but stored in symbol table with augmented name
            functionGenerationContext.getMv().visitFieldInsn(
                    Opcodes.GETSTATIC, qualifiedClassName, lookupInfo.getFirst().getAugmentedName(), type.getDescriptor()
            );
        }
        else {
            Integer localVarIndex = functionGenerationContext.getLocalVarIndex(lookupInfo.getFirst().getAugmentedName());
            functionGenerationContext.getMv().visitVarInsn(loadInstruction, localVarIndex);
        }
    }
}
