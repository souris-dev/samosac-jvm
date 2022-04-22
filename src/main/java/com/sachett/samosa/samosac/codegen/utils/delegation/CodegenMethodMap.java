package com.sachett.samosa.samosac.codegen.utils.delegation;

import com.sachett.samosa.parser.SamosaParser;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Map;

import static java.util.Map.entry;

public class CodegenMethodMap {
    private static final Map<Class<? extends ParseTree>, CodegenDelegatedMethod> methods = Map.ofEntries(
            entry(SamosaParser.NormalDeclAssignStmtContext.class, CodegenDelegatedMethod.NORMAL_DECLASSIGN),
            entry(SamosaParser.BooleanDeclAssignStmtContext.class, CodegenDelegatedMethod.BOOLEAN_DECLASSIGN),
            entry(SamosaParser.DeclStmtContext.class, CodegenDelegatedMethod.DECL),
            entry(SamosaParser.TypeInferredDeclAssignStmtContext.class, CodegenDelegatedMethod.TYPEINF_DECLASSIGN),
            entry(SamosaParser.TypeInferredBooleanDeclAssignStmtContext.class, CodegenDelegatedMethod.TYPEINF_BOOLEAN_DECLASSIGN),
            entry(SamosaParser.WhileStmtContext.class, CodegenDelegatedMethod.WHILE),
            entry(SamosaParser.BreakControlStmtContext.class, CodegenDelegatedMethod.BREAK),
            entry(SamosaParser.ContinueControlStmtContext.class, CodegenDelegatedMethod.CONTINUE),
            entry(SamosaParser.IfStmtContext.class, CodegenDelegatedMethod.IF),
            entry(SamosaParser.ReturnStmtNoExprContext.class, CodegenDelegatedMethod.RETURN_NOEXPR),
            entry(SamosaParser.ReturnStmtWithBooleanExprContext.class, CodegenDelegatedMethod.RETURN_BOOL),
            entry(SamosaParser.ReturnStmtWithExprContext.class, CodegenDelegatedMethod.RETURN_WITHEXPR),
            entry(SamosaParser.ImplicitRetTypeFuncDefContext.class, CodegenDelegatedMethod.IMPLICIT_RET_FUNCDEF),
            entry(SamosaParser.ExplicitRetTypeFuncDefContext.class, CodegenDelegatedMethod.EXPLICIT_RET_FUNCDEF),
            entry(SamosaParser.ExprAssignContext.class, CodegenDelegatedMethod.EXPR_ASSIGN),
            entry(SamosaParser.BooleanExprAssignContext.class, CodegenDelegatedMethod.BOOLEAN_EXPR_ASSIGN),
            entry(SamosaParser.BlockContext.class, CodegenDelegatedMethod.BLOCK),
            entry(SamosaParser.FunctionCallNoArgsContext.class, CodegenDelegatedMethod.FUNCTIONCALL_NOARGS),
            entry(SamosaParser.FunctionCallWithArgsContext.class, CodegenDelegatedMethod.FUNCTIONCALL_WITHARGS)
    );

    public static CodegenDelegatedMethod getMethodFromClass(Class<? extends ParseTree> parseTreeClass) {
        return methods.get(parseTreeClass);
    }
}