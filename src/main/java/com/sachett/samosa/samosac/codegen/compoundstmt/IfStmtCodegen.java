package com.sachett.samosa.samosac.codegen.compoundstmt;

import com.sachett.samosa.parser.SamosaParser;
import com.sachett.samosa.samosac.codegen.expressions.BooleanExprCodegen;
import com.sachett.samosa.samosac.codegen.function.FunctionGenerationContext;
import com.sachett.samosa.samosac.codegen.utils.delegation.CodegenDelegatable;
import com.sachett.samosa.samosac.codegen.utils.delegation.CodegenDelegatedMethod;
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class IfStmtCodegen extends CodegenDelegatable implements IControlNodeCodegen {
    FunctionGenerationContext functionGenerationContext;
    ControlNodeCodegenType controlNodeCodegenType = ControlNodeCodegenType.IF;
    private final CodegenDelegatable delegatedParentCodegen;

    private final String className;
    private final String packageName;
    private final SymbolTable symbolTable;

    @Override
    public ControlNodeCodegenType getControlNodeCodegenType() {
        return controlNodeCodegenType;
    }

    public IfStmtCodegen(
            CodegenDelegatable delegatedParentCodegen,
            FunctionGenerationContext functionGenerationContext,
            SymbolTable symbolTable,
            String className,
            String packageName
    ) {
        super(delegatedParentCodegen.getSharedDelegationManager());

        // Register the stuff that this generator generates with the shared delegation manager.
        HashSet<CodegenDelegatedMethod> delegatedMethodHashSet = new HashSet<>(List.of(
                CodegenDelegatedMethod.BLOCK,
                CodegenDelegatedMethod.IF
        ));
        this.registerDelegatedMethods(delegatedMethodHashSet);

        this.functionGenerationContext = functionGenerationContext;
        this.delegatedParentCodegen = delegatedParentCodegen;
        this.className = className;
        this.packageName = packageName;
        this.symbolTable = symbolTable;
    }

    public void generateIfStmt(SamosaParser.IfStmtContext ctx) {
        SamosaParser.BooleanExprContext booleanExprContext = ctx.booleanExpr(0);
        BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                booleanExprContext, symbolTable, functionGenerationContext, className, packageName);

        booleanExprCodegen.setJumpToFalseLabel(false);
        ArrayList<Pair<Label, SamosaParser.BooleanExprContext>> labels = new ArrayList<>();
        int nElseIfs = ctx.elseifblocks.size();

        // Create labels corresponding to all branches
        // For an if statement with no else-ifs, no. of labels required = 2 (regardless of presence of else block)
        // For an if statement with else-ifs, no. of labels required = 2 + no. of else-if blocks
        // No. of booleanExprContexts = no. of else-ifs + 1 (for if)
        for (int i = 0; i < nElseIfs + 2; i++) {
            if (i != nElseIfs + 1) {
                // for the if and else-if statements' labels
                labels.add(new Pair<>(new Label(), ctx.booleanExpr(i)));
            } else {
                // for the label corresponding to the next statement after if
                // (afterIf label)
                labels.add(new Pair<>(new Label(), null));
            }
        }

        Label afterIf = labels.get(labels.size() - 1).getFirst();
        ArrayList<FunctionGenerationContext.FrameStackMap> frameStackMaps = new ArrayList<>();

        // First generate the boolean expressions and if branch statements
        for (int i = 0; i < labels.size(); i++) {
            var labelCtx = labels.get(i);
            if (labelCtx.getSecond() != null) {
                booleanExprCodegen.setBooleanExprContext(labelCtx.getSecond());
                if (!(booleanExprContext instanceof SamosaParser.BooleanExprRelOpContext)
                        && !(booleanExprContext instanceof SamosaParser.BooleanExprCompOpContext)) {
                    booleanExprCodegen.doCodegen();
                    // In this case, after codegen of the booleanExpr, the stack should contain
                    // a bool value on top, on whose basis we can jump

                    // Note that IFEQ jumps if top of stack == 0 and IFNE jumps if top of stack != 0
                    functionGenerationContext.getMv().visitJumpInsn(Opcodes.IFNE, labelCtx.getFirst());
                } else {
                    booleanExprCodegen.setFalseLabel(labelCtx.getFirst());
                    booleanExprCodegen.setJumpLabelsHaveBlocks(true);
                    booleanExprCodegen.doCodegen();

                    // update the labelCtx with the right jump label
                    labels.set(i, new Pair<>(booleanExprCodegen.getActualJumpLabel(), labelCtx.getSecond()));
                }
            } else {
                // TODO: Generate else block code here
                if (ctx.elseblock.size() > 0) {
                    // else block is present
                    this.startDelegatingTo(delegatedParentCodegen);
                    delegatedParentCodegen.visit(ctx.elseblock.get(0));
                    this.finishDelegating();
                }
                // the label corresponding to the next statement after the if construct
                frameStackMaps.add(functionGenerationContext.getCurrentFrameStackInfo());
                functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, afterIf);
            }
        }

        // Now generate the code for inside the blocks
        for (int i = 0; i < labels.size(); i++) {
            // visit the label and generate code for that block
            Pair<Label, SamosaParser.BooleanExprContext> labelCtx = labels.get(i);
            functionGenerationContext.getMv().visitLabel(labelCtx.getFirst());
            functionGenerationContext.getMv().visitFrame(
                    Opcodes.F_NEW,
                    frameStackMaps.get(0).numLocals, frameStackMaps.get(0).locals,
                    frameStackMaps.get(0).numStack, frameStackMaps.get(0).stack
            );

            // generate codes for the corresponding blocks
            if (i < labels.size() - 1) {
                this.startDelegatingTo(delegatedParentCodegen);
                delegatedParentCodegen.visit(ctx.block(i));
                this.finishDelegating();
                // after execution, skip other labels and go to afterIf
                frameStackMaps.add(functionGenerationContext.getCurrentFrameStackInfo());
                functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, afterIf);
            }
        }
    }
}
