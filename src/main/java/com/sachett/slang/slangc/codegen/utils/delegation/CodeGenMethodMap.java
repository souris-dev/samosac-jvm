package com.sachett.slang.slangc.codegen.utils.delegation;

import com.sachett.slang.parser.SlangParser;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Map;

import static java.util.Map.entry;

public class CodeGenMethodMap {
    private static final Map<Class<? extends ParseTree>, CodeGenDelegatedMethod> methods = Map.ofEntries(
            entry(SlangParser.NormalDeclAssignStmtContext.class, CodeGenDelegatedMethod.NORMAL_DECLASSIGN),
            entry(SlangParser.BooleanDeclAssignStmtContext.class, CodeGenDelegatedMethod.BOOLEAN_DECLASSIGN),
            entry(SlangParser.DeclStmtContext.class, CodeGenDelegatedMethod.DECL),
            entry(SlangParser.TypeInferredDeclAssignStmtContext.class, CodeGenDelegatedMethod.TYPEINF_DECLASSIGN),
            entry(SlangParser.TypeInferredBooleanDeclAssignStmtContext.class, CodeGenDelegatedMethod.TYPEINF_BOOLEAN_DECLASSIGN),
            entry(SlangParser.WhileStmtContext.class, CodeGenDelegatedMethod.WHILE),
            entry(SlangParser.BreakControlStmtContext.class, CodeGenDelegatedMethod.BREAK),
            entry(SlangParser.ContinueControlStmtContext.class, CodeGenDelegatedMethod.CONTINUE),
            entry(SlangParser.IfStmtContext.class, CodeGenDelegatedMethod.IF),
            entry(SlangParser.ReturnStmtNoExprContext.class, CodeGenDelegatedMethod.RETURN_NOEXPR),
            entry(SlangParser.ReturnStmtWithBooleanExprContext.class, CodeGenDelegatedMethod.RETURN_BOOL),
            entry(SlangParser.ReturnStmtWithExprContext.class, CodeGenDelegatedMethod.RETURN_WITHEXPR),
            entry(SlangParser.ImplicitRetTypeFuncDefContext.class, CodeGenDelegatedMethod.IMPLICIT_RET_FUNCDEF),
            entry(SlangParser.ExplicitRetTypeFuncDefContext.class, CodeGenDelegatedMethod.EXPLICIT_RET_FUNCDEF)
    );

    public static CodeGenDelegatedMethod getMethodFromClass(Class<? extends ParseTree> parseTreeClass) {
        return methods.get(parseTreeClass);
    }
}