package com.sachett.slang.slangc.codegen;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.compoundstmt.WhileStmtCodegen;
import com.sachett.slang.slangc.codegen.expressions.BooleanExprCodegen;
import com.sachett.slang.slangc.codegen.expressions.IntExprCodegen;
import com.sachett.slang.slangc.codegen.expressions.StringExprCodegen;
import com.sachett.slang.slangc.codegen.function.FunctionGenerationContext;
import com.sachett.slang.slangc.codegen.utils.delegation.CodegenDelegatable;
import com.sachett.slang.slangc.symbol.ISymbol;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayDeque;
import java.util.ArrayList;

import static com.sachett.slang.logging.LoggingUtilsKt.err;

/**
 * This class has methods for common codegen constructs.
 */
public class CodegenCommons extends SlangBaseVisitor<Void> {
    protected final String className;
    protected final String packageName;
    protected FunctionGenerationContext functionGenerationContext;
    protected final SymbolTable symbolTable;

    /**
     * Stack of WhileStmtCodeGens (for nested while statements).
     */
    protected final ArrayDeque<WhileStmtCodegen> whileStmtCodegens = new ArrayDeque<>();

    protected CodegenDelegatable parentCodegen; // parent codegen class instance
    public CodeGenerator getParentCodegen() {
        return parentCodegen;
    }

    public void setParentCodegen(CodegenDelegatable parentCodegen) {
        this.parentCodegen = parentCodegen;
    }

    public FunctionGenerationContext getFunctionCodegen() {
        return functionGenerationContext;
    }

    public void setFunctionCodegen(FunctionGenerationContext functionGenerationContext) {
        this.functionGenerationContext = functionGenerationContext;
    }

    public CodegenCommons(
            CodegenDelegatable parentCodegen,
            FunctionGenerationContext functionGenerationContext,
            SymbolTable symbolTable,
            String className,
            String packageName
    ) {
        this.className = className;
        this.packageName = packageName;
        this.functionGenerationContext = functionGenerationContext;
        this.symbolTable = symbolTable;
        this.parentCodegen = parentCodegen;
    }

    @Override
    public Void visitBlock(SlangParser.BlockContext ctx) {
        // keep track of scopes in the symbol table
        symbolTable.incrementScopeOverrideScopeCreation(false);
        parentCodegen.visitChildren(ctx);
        symbolTable.decrementScope(false);
        return null;
    }

    @Override
    public Void visitIfStmt(SlangParser.IfStmtContext ctx) {
        SlangParser.BooleanExprContext booleanExprContext = ctx.booleanExpr(0);
        BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                booleanExprContext, symbolTable, functionGenerationContext, className, packageName);

        ArrayList<Pair<Label, SlangParser.BooleanExprContext>> labels = new ArrayList<>();
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
        for (Pair<Label, SlangParser.BooleanExprContext> labelCtx : labels) {
            if (labelCtx.getSecond() != null) {
                booleanExprCodegen.setBooleanExprContext(labelCtx.getSecond());
                if (!(booleanExprContext instanceof SlangParser.BooleanExprRelOpContext)
                        && !(booleanExprContext instanceof SlangParser.BooleanExprCompOpContext)) {
                    booleanExprCodegen.doCodegen();
                    // In this case, after codegen of the booleanExpr, the stack should contain
                    // a bool value on top, on whose basis we can jump

                    // Note that IFEQ jumps if top of stack == 0 and IFNE jumps if top of stack != 0
                    functionGenerationContext.getMv().visitJumpInsn(Opcodes.IFNE, labelCtx.getFirst());
                } else {
                    booleanExprCodegen.setFalseLabel(labelCtx.getFirst());
                    booleanExprCodegen.setJumpLabelsHaveBlocks(true);
                    booleanExprCodegen.doCodegen();
                }
            } else {
                // TODO: Generate else block code here
                if (ctx.elseblock.size() > 0) {
                    // else block is present
                    parentCodegen.visit(ctx.elseblock.get(0));
                }
                // the label corresponding to the next statement after the if construct
                frameStackMaps.add(functionGenerationContext.getCurrentFrameStackInfo());
                functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, afterIf);
            }
        }

        // Now generate the code for inside the blocks
        for (int i = 0; i < labels.size(); i++) {
            // visit the label and generate code for that block
            Pair<Label, SlangParser.BooleanExprContext> labelCtx = labels.get(i);
            functionGenerationContext.getMv().visitLabel(labelCtx.getFirst());
            functionGenerationContext.getMv().visitFrame(
                    Opcodes.F_NEW,
                    frameStackMaps.get(0).numLocals, frameStackMaps.get(0).locals,
                    frameStackMaps.get(0).numStack, frameStackMaps.get(0).stack
            );

            // generate codes for the corresponding blocks
            if (i < labels.size() - 1) {
                parentCodegen.visit(ctx.block(i));
                // after execution, skip other labels and go to afterIf
                frameStackMaps.add(functionGenerationContext.getCurrentFrameStackInfo());
                functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, afterIf);
            }
        }

        return null;
    }

    public Void visitExprAssign(SlangParser.ExprAssignContext ctx) {
        // look up the identifier on the left to determine the type of the expression
        // because static type check has already ensured compatibility on both sides
        String idName = ctx.IDENTIFIER().getText();

        Pair<ISymbol, Integer> lookupInfo = symbolTable.lookupWithNearestScopeValue(idName);
        if (lookupInfo.getFirst() == null) {
            // lookup failed
            return null;
        }

        Type type = null;
        int storeInstruction = Opcodes.ASTORE;

        // Do codegen of RHS
        switch (lookupInfo.getFirst().getSymbolType()) {
            case INT:
                type = Type.INT_TYPE;
                storeInstruction = Opcodes.ISTORE;
                IntExprCodegen intCodegen = new IntExprCodegen(
                        ctx.expr(), symbolTable, functionGenerationContext, className, packageName);
                intCodegen.doCodegen();
                break;

            case BOOL:
                // This is the case when either of these occurs:
                // aBoolVar = () -> aFunctionReturningBool.
                // or,
                // aBoolVar = anotherBoolVar.
                type = Type.BOOLEAN_TYPE;
                storeInstruction = Opcodes.ISTORE;
                BooleanExprCodegen boolCodegen = new BooleanExprCodegen(
                        null, symbolTable, functionGenerationContext, className, packageName);
                boolCodegen.doSpecialCodegen(ctx.expr());
                break;

            case STRING:
                type = Type.getType(String.class);
                StringExprCodegen stringExprCodegen = new StringExprCodegen(
                        ctx.expr(), symbolTable, functionGenerationContext, className, packageName);
                stringExprCodegen.doCodegen();
                break;

            default:
                err("[Error] Wrong assignment (bad type on LHS).");
        }

        // Store the value generated into the variable
        if (lookupInfo.getSecond() == 0) {
            // we're talking about a global variable
            // (a static field of the class during generation)
            assert type != null;
            functionGenerationContext.getMv().visitFieldInsn(
                    Opcodes.PUTSTATIC, className, idName, type.getDescriptor()
            );
        } else {
            Integer localVarIndex = functionGenerationContext.getLocalVarIndex(lookupInfo.getFirst().getAugmentedName());
            functionGenerationContext.getMv().visitVarInsn(storeInstruction, localVarIndex);
        }
        return super.visitExprAssign(ctx);
    }

    @Override
    public Void visitWhileStmt(SlangParser.WhileStmtContext ctx) {
        WhileStmtCodegen whileStmtCodegen = new WhileStmtCodegen(
                parentCodegen,
                functionGenerationContext,
                symbolTable,
                className,
                packageName
        );

        whileStmtCodegens.push(whileStmtCodegen);
        parentCodegen.startDelegatingTo(whileStmtCodegen);
        whileStmtCodegen.generateWhileStmt(ctx);
        parentCodegen.finishDelegating();
        whileStmtCodegens.pop();
        return null;
    }

    @Override
    public Void visitBreakControlStmt(SlangParser.BreakControlStmtContext ctx) {
        if (whileStmtCodegens.size() > 0) {
            whileStmtCodegens.peek().visitBreakControlStmt(ctx);
        }
        return null;
    }

    @Override
    public Void visitContinueControlStmt(SlangParser.ContinueControlStmtContext ctx) {
        if (whileStmtCodegens.size() > 0) {
            whileStmtCodegens.peek().visitContinueControlStmt(ctx);
        }
        return null;
    }

    @Override
    public Void visitFunctionCallNoArgs(SlangParser.FunctionCallNoArgsContext ctx) {
        return null;
    }

    @Override
    public Void visitFunctionCallWithArgs(SlangParser.FunctionCallWithArgsContext ctx) {
        return null;
    }
}
