package com.sachett.slang.slangc.codegen.utils.delegation;

import com.sachett.slang.parser.SlangBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

public class CodegenDelegationManager extends SlangBaseVisitor<Void> {
    private CodegenDelegatable currentCodeGenDelegator;
    private CodegenDelegatable currentCodeGenDelegated;

    public CodegenDelegationManager(CodegenDelegatable currentCodeGenDelegator) {
        this.currentCodeGenDelegator = currentCodeGenDelegator;
    }

    public CodegenDelegationManager(
            CodegenDelegatable currentCodeGenDelegator,
            CodegenDelegatable currentCodeGenDelegated
    ) {
        this.currentCodeGenDelegator = currentCodeGenDelegator;
        this.currentCodeGenDelegated = currentCodeGenDelegated;
    }

    public void setCurrentDelegator(CodegenDelegatable parentDelegator) {
        this.currentCodeGenDelegator = parentDelegator;
    }

    public void setCurrentDelegated(CodegenDelegatable childDelegated) {
        this.currentCodeGenDelegated = childDelegated;
    }

    @Override
    public Void visit(ParseTree parseTree) {
        CodegenDelegatedMethod method = CodegenMethodMap.getMethodFromClass(parseTree.getClass());

        if (method == CodegenDelegatedMethod.NORMAL_DECLASSIGN) {
            System.out.println("Delegating NormalDeclAssign"); //debug
        }

        if (method == null) {
            currentCodeGenDelegator.setBeingDelegated(true);
            var voidPlaceholder = currentCodeGenDelegator.visit(parseTree);
            currentCodeGenDelegator.setBeingDelegated(false);
            return voidPlaceholder;
        }

        if (currentCodeGenDelegated == null) {
            if (currentCodeGenDelegator == null) {
                return null;
            }
            else {
                currentCodeGenDelegator.setBeingDelegated(true);
                var voidPlaceholder = currentCodeGenDelegator.visit(parseTree);
                currentCodeGenDelegator.setBeingDelegated(false);
                return voidPlaceholder;
            }
        }

        if (currentCodeGenDelegated.isMethodDelegated(method)) {
            currentCodeGenDelegated.setBeingDelegated(true);
            var voidPlaceholder = currentCodeGenDelegated.visit(parseTree);
            currentCodeGenDelegated.setBeingDelegated(false);
            return voidPlaceholder;
        }
        else {
            if (currentCodeGenDelegator == null) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Void visitChildren(RuleNode node) {
        CodegenDelegatedMethod method = CodegenMethodMap.getMethodFromClass(node.getClass());

        if (method == CodegenDelegatedMethod.NORMAL_DECLASSIGN) {
            System.out.println("Delegating NormalDeclAssign"); //debug
        }

        if (method == null) {
            currentCodeGenDelegator.setBeingDelegated(true);
            var voidPlaceholder = currentCodeGenDelegator.visitChildren(node);
            currentCodeGenDelegator.setBeingDelegated(false);
            return voidPlaceholder;
        }

        if (currentCodeGenDelegated == null) {
            if (currentCodeGenDelegator == null) {
                return null;
            }
            else {
                currentCodeGenDelegator.setBeingDelegated(true);
                var voidPlaceholder = currentCodeGenDelegator.visit(node);
                currentCodeGenDelegator.setBeingDelegated(false);
                return voidPlaceholder;
            }
        }

        if (currentCodeGenDelegated.isMethodDelegated(method)) {
            currentCodeGenDelegated.setBeingDelegated(true);
            var voidPlaceholder = currentCodeGenDelegated.visit(node);
            currentCodeGenDelegated.setBeingDelegated(false);
            return voidPlaceholder;
        }
        else {
            if (currentCodeGenDelegator == null) {
                return null;
            }
        }
        return null;
    }
}
