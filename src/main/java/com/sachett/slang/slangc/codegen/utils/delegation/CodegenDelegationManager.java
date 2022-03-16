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

    private Void delegateVisitTo(CodegenDelegatable delegatable, ParseTree tree) {
        delegatable.setBeingDelegated(true);
        var voidPlaceholder = delegatable.visit(tree);
        delegatable.setBeingDelegated(false);
        return voidPlaceholder;
    }

    private Void delegateChildrenVisitTo(CodegenDelegatable delegatable, RuleNode node) {
        delegatable.setBeingDelegated(true);
        var voidPlaceholder = delegatable.visitChildren(node);
        delegatable.setBeingDelegated(false);
        return voidPlaceholder;
    }

    @Override
    public Void visit(ParseTree parseTree) {
        CodegenDelegatedMethod method = CodegenMethodMap.getMethodFromClass(parseTree.getClass());

        if (method == CodegenDelegatedMethod.NORMAL_DECLASSIGN) {
            System.out.println("Delegating NormalDeclAssign"); //debug
        }

        if (method == null) {
            return delegateVisitTo(currentCodeGenDelegator, parseTree);
        }

        if (currentCodeGenDelegated == null) {
            if (currentCodeGenDelegator == null) {
                return null;
            }
            else {
                return delegateVisitTo(currentCodeGenDelegator, parseTree);
            }
        }

        if (currentCodeGenDelegated.isMethodDelegated(method)) {
            return delegateVisitTo(currentCodeGenDelegated, parseTree);
        }
        else {
            if (currentCodeGenDelegator == null) {
                return null;
            }
            else {
                return delegateVisitTo(currentCodeGenDelegator, parseTree);
            }
        }
    }

    @Override
    public Void visitChildren(RuleNode node) {
        CodegenDelegatedMethod method = CodegenMethodMap.getMethodFromClass(node.getClass());

        if (method == CodegenDelegatedMethod.NORMAL_DECLASSIGN) {
            System.out.println("Delegating NormalDeclAssign"); //debug
        }

        if (method == null) {
            return delegateChildrenVisitTo(currentCodeGenDelegator, node);
        }

        if (currentCodeGenDelegated == null) {
            if (currentCodeGenDelegator == null) {
                return null;
            }
            else {
                return delegateChildrenVisitTo(currentCodeGenDelegator, node);
            }
        }

        if (currentCodeGenDelegated.isMethodDelegated(method)) {
            return delegateChildrenVisitTo(currentCodeGenDelegated, node);
        }
        else {
            if (currentCodeGenDelegator == null) {
                return null;
            } else {
                return delegateChildrenVisitTo(currentCodeGenDelegator, node);
            }
        }
    }
}
