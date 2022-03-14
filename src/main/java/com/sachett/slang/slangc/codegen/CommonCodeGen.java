package com.sachett.slang.slangc.codegen;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.compoundstmt.WhileStmtCodeGen;
import com.sachett.slang.slangc.codegen.expressions.BooleanExprCodeGen;
import com.sachett.slang.slangc.codegen.expressions.IntExprCodeGen;
import com.sachett.slang.slangc.codegen.expressions.StringExprCodeGen;
import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
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
public class CommonCodeGen extends SlangBaseVisitor<Void> {
    protected final String className;
    protected final String packageName;
    protected FunctionCodeGen functionCodeGen;
    protected final SymbolTable symbolTable;

    /**
     * Stack of WhileStmtCodeGens (for nested while statements).
     */
    protected final ArrayDeque<WhileStmtCodeGen> whileStmtCodeGens = new ArrayDeque<>();

    protected CodeGenerator parentCodeGen; // parent codegen class instance
    public CodeGenerator getParentCodeGen() {
        return parentCodeGen;
    }

    public void setParentCodeGen(CodeGenerator parentCodeGen) {
        this.parentCodeGen = parentCodeGen;
    }

    public FunctionCodeGen getFunctionCodeGen() {
        return functionCodeGen;
    }

    public void setFunctionCodeGen(FunctionCodeGen functionCodeGen) {
        this.functionCodeGen = functionCodeGen;
    }

    public CommonCodeGen(
            CodeGenerator parentCodeGen,
            FunctionCodeGen functionCodeGen,
            SymbolTable symbolTable,
            String className,
            String packageName
    ) {
        this.className = className;
        this.packageName = packageName;
        this.functionCodeGen = functionCodeGen;
        this.symbolTable = symbolTable;
        this.parentCodeGen = parentCodeGen;
    }

    @Override
    public Void visitBlock(SlangParser.BlockContext ctx) {
        // keep track of scopes in the symbol table
        symbolTable.incrementScopeOverrideScopeCreation(false);
        if (ctx.statements() != null) {
            parentCodeGen.visit(ctx.statements());
        }
        symbolTable.decrementScope(false);
        return null;
    }

    @Override
    public Void visitIfStmt(SlangParser.IfStmtContext ctx) {
        SlangParser.BooleanExprContext booleanExprContext = ctx.booleanExpr(0);
        BooleanExprCodeGen booleanExprCodeGen = new BooleanExprCodeGen(
                booleanExprContext, symbolTable, functionCodeGen, className, packageName);

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
        ArrayList<FunctionCodeGen.FrameStackMap> frameStackMaps = new ArrayList<>();

        // First generate the boolean expressions and if branch statements
        for (Pair<Label, SlangParser.BooleanExprContext> labelCtx : labels) {
            if (labelCtx.getSecond() != null) {
                booleanExprCodeGen.setBooleanExprContext(labelCtx.getSecond());
                if (!(booleanExprContext instanceof SlangParser.BooleanExprRelOpContext)
                        && !(booleanExprContext instanceof SlangParser.BooleanExprCompOpContext)) {
                    booleanExprCodeGen.doCodeGen();
                    // In this case, after codegen of the booleanExpr, the stack should contain
                    // a bool value on top, on whose basis we can jump

                    // Note that IFEQ jumps if top of stack == 0 and IFNE jumps if top of stack != 0
                    functionCodeGen.getMv().visitJumpInsn(Opcodes.IFNE, labelCtx.getFirst());
                } else {
                    booleanExprCodeGen.setFalseLabel(labelCtx.getFirst());
                    booleanExprCodeGen.setJumpLabelsHaveBlocks(true);
                    booleanExprCodeGen.doCodeGen();
                }
            } else {
                // TODO: Generate else block code here
                if (ctx.elseblock.size() > 0) {
                    // else block is present
                    parentCodeGen.visit(ctx.elseblock.get(0));
                }
                // the label corresponding to the next statement after the if construct
                frameStackMaps.add(functionCodeGen.getCurrentFrameStackInfo());
                functionCodeGen.getMv().visitJumpInsn(Opcodes.GOTO, afterIf);
            }
        }

        // Now generate the code for inside the blocks
        for (int i = 0; i < labels.size(); i++) {
            // visit the label and generate code for that block
            Pair<Label, SlangParser.BooleanExprContext> labelCtx = labels.get(i);
            functionCodeGen.getMv().visitLabel(labelCtx.getFirst());
            functionCodeGen.getMv().visitFrame(
                    Opcodes.F_NEW,
                    frameStackMaps.get(0).numLocals, frameStackMaps.get(0).locals,
                    frameStackMaps.get(0).numStack, frameStackMaps.get(0).stack
            );

            // generate codes for the corresponding blocks
            if (i < labels.size() - 1) {
                parentCodeGen.visit(ctx.block(i));
                // after execution, skip other labels and go to afterIf
                frameStackMaps.add(functionCodeGen.getCurrentFrameStackInfo());
                functionCodeGen.getMv().visitJumpInsn(Opcodes.GOTO, afterIf);
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
                IntExprCodeGen intCodeGen = new IntExprCodeGen(
                        ctx.expr(), symbolTable, functionCodeGen, className, packageName);
                intCodeGen.doCodeGen();
                break;

            case BOOL:
                // This is the case when either of these occurs:
                // aBoolVar = () -> aFunctionReturningBool.
                // or,
                // aBoolVar = anotherBoolVar.
                type = Type.BOOLEAN_TYPE;
                storeInstruction = Opcodes.ISTORE;
                BooleanExprCodeGen boolCodeGen = new BooleanExprCodeGen(
                        null, symbolTable, functionCodeGen, className, packageName);
                boolCodeGen.doSpecialCodeGen(ctx.expr());
                break;

            case STRING:
                type = Type.getType(String.class);
                StringExprCodeGen stringExprCodeGen = new StringExprCodeGen(
                        ctx.expr(), symbolTable, functionCodeGen, className, packageName);
                stringExprCodeGen.doCodeGen();
                break;

            default:
                err("[Error] Wrong assignment (bad type on LHS).");
        }

        // Store the value generated into the variable
        if (lookupInfo.getSecond() == 0) {
            // we're talking about a global variable
            // (a static field of the class during generation)
            assert type != null;
            functionCodeGen.getMv().visitFieldInsn(
                    Opcodes.PUTSTATIC, className, idName, type.getDescriptor()
            );
        } else {
            Integer localVarIndex = functionCodeGen.getLocalVarIndex(lookupInfo.getFirst().getAugmentedName());
            functionCodeGen.getMv().visitVarInsn(storeInstruction, localVarIndex);
        }
        return super.visitExprAssign(ctx);
    }

    @Override
    public Void visitWhileStmt(SlangParser.WhileStmtContext ctx) {
        WhileStmtCodeGen whileStmtCodeGen = new WhileStmtCodeGen(
                parentCodeGen,
                functionCodeGen,
                symbolTable,
                className,
                packageName
        );

        whileStmtCodeGens.push(whileStmtCodeGen);
        whileStmtCodeGen.generateWhileStmt(ctx);
        whileStmtCodeGens.pop();
        return null;
    }

    @Override
    public Void visitBreakControlStmt(SlangParser.BreakControlStmtContext ctx) {
        if (whileStmtCodeGens.size() > 0) {
            whileStmtCodeGens.peek().visitBreakControlStmt(ctx);
        }
        return null;
    }

    @Override
    public Void visitContinueControlStmt(SlangParser.ContinueControlStmtContext ctx) {
        if (whileStmtCodeGens.size() > 0) {
            whileStmtCodeGens.peek().visitContinueControlStmt(ctx);
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
