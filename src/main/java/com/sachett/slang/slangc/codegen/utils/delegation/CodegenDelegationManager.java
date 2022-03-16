package com.sachett.slang.slangc.codegen.utils.delegation;

import com.sachett.slang.parser.SlangBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

public class CodegenDelegationManager extends SlangBaseVisitor<Void> {
    private CodegenDelegatable currentCodeGenDelegator;
    private CodegenDelegatable currentCodeGenDelegated;
    private boolean beingDelegatedStore = false;

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

    private Void delegateVisitTo(CodegenDelegatable delegated, ParseTree tree) {
        delegated.setBeingDelegated(true);
        var voidPlaceholder = delegated.visit(tree);
        delegated.setBeingDelegated(false);
        return voidPlaceholder;
    }

    private Void delegateChildrenVisitTo(CodegenDelegatable delegated, RuleNode node) {
        delegated.setBeingDelegated(true);
        var voidPlaceholder = delegated.visitChildren(node);
        delegated.setBeingDelegated(false);
        return voidPlaceholder;
    }

    private void undelegate(CodegenDelegatable delegatable) {
        beingDelegatedStore = delegatable.isBeingDelegated();
        delegatable.setBeingDelegated(false);
    }

    private void restoreDelegate(CodegenDelegatable delegatable) {
        delegatable.setBeingDelegated(beingDelegatedStore);
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
            undelegate(currentCodeGenDelegated);
            var _void = delegateVisitTo(currentCodeGenDelegated, parseTree);
            restoreDelegate(currentCodeGenDelegated);
            return _void;
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
            undelegate(currentCodeGenDelegator);
            var _void = delegateChildrenVisitTo(currentCodeGenDelegated, node);
            restoreDelegate(currentCodeGenDelegator);
            return _void;
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
