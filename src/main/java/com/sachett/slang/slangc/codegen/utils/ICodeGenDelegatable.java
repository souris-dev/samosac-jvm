package com.sachett.slang.slangc.codegen.utils;

import com.sachett.slang.slangc.codegen.CodeGenerator;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.HashSet;

abstract class ICodeGenDelegatable extends CodeGenerator {
    HashSet<CodeGenDelegatedMethod> delegatedMethods = new HashSet<>();
    CodeGenDelegationManager codeGenDelegationManager;

    public ICodeGenDelegatable(
            HashSet<CodeGenDelegatedMethod> delegatedMethods,
            CodeGenDelegationManager codeGenDelegationManager
    ) {
        this.codeGenDelegationManager = codeGenDelegationManager;
        this.delegatedMethods = delegatedMethods;
    }

    public ICodeGenDelegatable(
            CodeGenDelegationManager codeGenDelegationManager
    ) {
        this.codeGenDelegationManager = codeGenDelegationManager;
    }

    protected void registerDelegatedMethod(CodeGenDelegatedMethod method) {
        this.delegatedMethods.add(method);
    }

    public boolean isMethodDelegated(CodeGenDelegatedMethod method) {
        return delegatedMethods.contains(method);
    }

    @Override
    public Void visit(ParseTree parseTree) {
        return codeGenDelegationManager.visit(parseTree);
    }

    @Override
    public Void visitChildren(RuleNode node) {
        return codeGenDelegationManager.visitChildren(node);
    }
}
