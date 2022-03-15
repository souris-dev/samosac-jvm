package com.sachett.slang.slangc.codegen.utils.delegation;

import com.sachett.slang.parser.SlangParser;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Map;

import static java.util.Map.entry;

public class CodegenMethodMap {
    private static final Map<Class<? extends ParseTree>, CodegenDelegatedMethod> methods = Map.ofEntries(
            entry(SlangParser.NormalDeclAssignStmtContext.class, CodegenDelegatedMethod.NORMAL_DECLASSIGN),
            entry(SlangParser.BooleanDeclAssignStmtContext.class, CodegenDelegatedMethod.BOOLEAN_DECLASSIGN),
            entry(SlangParser.DeclStmtContext.class, CodegenDelegatedMethod.DECL),
            entry(SlangParser.TypeInferredDeclAssignStmtContext.class, CodegenDelegatedMethod.TYPEINF_DECLASSIGN),
            entry(SlangParser.TypeInferredBooleanDeclAssignStmtContext.class, CodegenDelegatedMethod.TYPEINF_BOOLEAN_DECLASSIGN),
            entry(SlangParser.WhileStmtContext.class, CodegenDelegatedMethod.WHILE),
            entry(SlangParser.BreakControlStmtContext.class, CodegenDelegatedMethod.BREAK),
            entry(SlangParser.ContinueControlStmtContext.class, CodegenDelegatedMethod.CONTINUE),
            entry(SlangParser.IfStmtContext.class, CodegenDelegatedMethod.IF),
            entry(SlangParser.ReturnStmtNoExprContext.class, CodegenDelegatedMethod.RETURN_NOEXPR),
            entry(SlangParser.ReturnStmtWithBooleanExprContext.class, CodegenDelegatedMethod.RETURN_BOOL),
            entry(SlangParser.ReturnStmtWithExprContext.class, CodegenDelegatedMethod.RETURN_WITHEXPR),
            entry(SlangParser.ImplicitRetTypeFuncDefContext.class, CodegenDelegatedMethod.IMPLICIT_RET_FUNCDEF),
            entry(SlangParser.ExplicitRetTypeFuncDefContext.class, CodegenDelegatedMethod.EXPLICIT_RET_FUNCDEF),
            entry(SlangParser.ExprAssignContext.class, CodegenDelegatedMethod.EXPR_ASSIGN),
            entry(SlangParser.BooleanExprAssignContext.class, CodegenDelegatedMethod.BOOLEAN_EXPR_ASSIGN),
            entry(SlangParser.BlockContext.class, CodegenDelegatedMethod.BLOCK),
            entry(SlangParser.FunctionCallNoArgsContext.class, CodegenDelegatedMethod.FUNCTIONCALL_NOARGS),
            entry(SlangParser.FunctionCallWithArgsContext.class, CodegenDelegatedMethod.FUNCTIONCALL_WITHARGS)
    );

    public static CodegenDelegatedMethod getMethodFromClass(Class<? extends ParseTree> parseTreeClass) {
        return methods.get(parseTreeClass);
    }
}