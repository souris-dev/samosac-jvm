package com.sachett.samosa.samosac.codegen.expressions;

import com.sachett.samosa.parser.SamosaBaseVisitor;
import com.sachett.samosa.parser.SamosaParser;
import com.sachett.samosa.samosac.codegen.function.FunctionCallCodegen;
import com.sachett.samosa.samosac.codegen.function.FunctionGenerationContext;
import com.sachett.samosa.samosac.staticchecker.ExpressionTypeDetector;
import com.sachett.samosa.samosac.symbol.SymbolType;
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static com.sachett.samosa.logging.LoggingUtilsKt.err;
import static com.sachett.samosa.logging.LoggingUtilsKt.fmtfatalerr;

public class BooleanExprCodegen extends SamosaBaseVisitor<Void> implements IExprCodegen {
    private SamosaParser.BooleanExprContext exprContext;
    private final FunctionGenerationContext functionGenerationContext;
    private final SymbolTable symbolTable;
    private final String className;
    private final String packageName;
    private final String qualifiedClassName;

    /**
     * Indicates whether the labels generated will have more code inserted in them.
     * This should be true in the case of if, while etc.
     * but false for cases where the boolean result of the evaluation should be stored
     * on top of the stack after doCodegen(); for example, in case of assignment statements.
     */
    private boolean jumpLabelsHaveBlocks = false;

    /**
     * jump instructions are inverted to jump to false labels
     * for example if it is var1 > var2, codegen generates jump to falseLabel if var1 <= var 2
     * and if jumpToFalseLabel is set to false:
     * for var1 > var2, codegen generates jump to trueLabel if var2 > var2
     */
    private boolean jumpToFalseLabel = true;

    private Label trueLabel = new Label();  // to jump to when condition is true
    private Label falseLabel = new Label(); // to jump to when condition is false
    private Label nextLabel = new Label();  // to jump to after the boolean expression is evaluated

    /* Stores the actual label to which the jump is performed on condition true/false (trueLabel or falseLabel) */
    private Label conditionJumpLabel = new Label();

    public Label getActualJumpLabel() {
        return conditionJumpLabel;
    }

    public Label getNextLabel() {
        return nextLabel;
    }

    public BooleanExprCodegen(
            SamosaParser.BooleanExprContext exprContext,
            SymbolTable symbolTable,
            FunctionGenerationContext functionGenerationContext,
            String className,
            String packageName
    ) {
        this.exprContext = exprContext;
        this.functionGenerationContext = functionGenerationContext;
        this.symbolTable = symbolTable;
        this.packageName = packageName;
        this.className = className;
        this.qualifiedClassName = packageName.replace(".", "/") + className;
    }

    @Override
    public void doCodegen() {
        visit(this.exprContext);
    }

    public void setBooleanExprContext(SamosaParser.BooleanExprContext booleanExprContext) {
        this.exprContext = booleanExprContext;
    }

    /**
     * Performs codeGen in cases such as:
     * aBoolVariable = () -> aFuncThatReturnsABool.
     * or,
     * aBoolVar = anotherBoolVar.
     * @param specialExprContext    The ExprContext corresponding to the RHS.
     */
    public void doSpecialCodegen(SamosaParser.ExprContext specialExprContext) {
        visit(specialExprContext);
    }

    public void setJumpLabelsHaveBlocks(boolean jumpLabelsHaveBlocks) {
        this.jumpLabelsHaveBlocks = jumpLabelsHaveBlocks;
    }

    public void setTrueLabel(Label label) {
        this.trueLabel = label;
    }

    public void setFalseLabel(Label label) {
        this.falseLabel = label;
    }

    public void setNextLabel(Label label) {
        this.nextLabel = label;
    }

    public void setJumpToFalseLabel(boolean jumpToFalseLabel) {
        this.jumpToFalseLabel = jumpToFalseLabel;
    }

    @Override
    public Void visitBooleanExprRelOp(SamosaParser.BooleanExprRelOpContext ctx) {
        var lhs = ctx.expr(0);
        var rhs = ctx.expr(1);

        var typeDetector = new ExpressionTypeDetector(symbolTable);
        var lhsType = typeDetector.getType(lhs);
        var rhsType = typeDetector.getType(rhs);

        // check for incompatible types
        if ((!lhsType.getFirst() || !rhsType.getFirst())
                || (lhsType.getSecond() != rhsType.getSecond())
                || !lhsType.getSecond().getCanBeUsedWithCompOp()
        ) {
            return null;
        }

        var theRelOp = ctx.relOp();
        var exprType = lhsType.getSecond();

        // Since we only have int expressions that can be compared using relops right now
        if (exprType == SymbolType.INT) {
            // evaluate the left and right sides of the relOp expression
            IntExprCodegen intExprCodegen = new IntExprCodegen(ctx.expr(0), symbolTable, functionGenerationContext, className, packageName);
            intExprCodegen.doCodegen();

            intExprCodegen.setExprContext(ctx.expr(1));
            intExprCodegen.doCodegen();
        }

        Label labelToJump = new Label();
        conditionJumpLabel = labelToJump;
        nextLabel = new Label();
        // save current stack map
        var currentFrameStack = functionGenerationContext.getCurrentFrameStackInfo();

        if (theRelOp.GT() != null) {
            if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
                int opcode = this.jumpToFalseLabel ? Opcodes.IF_ICMPLE : Opcodes.IF_ICMPGT;
                functionGenerationContext.getMv().visitJumpInsn(opcode, labelToJump);
            }
        } else if (theRelOp.GTEQ() != null) {
            if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
                int opcode = this.jumpToFalseLabel ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPGE;
                functionGenerationContext.getMv().visitJumpInsn(opcode, labelToJump);
            }
        } else if (theRelOp.LT() != null) {
            if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
                int opcode = this.jumpToFalseLabel ? Opcodes.IF_ICMPGE : Opcodes.IF_ICMPLT;
                functionGenerationContext.getMv().visitJumpInsn(opcode, labelToJump);
            }
        } else if (theRelOp.LTEQ() != null) {
            if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
                int opcode = this.jumpToFalseLabel ? Opcodes.IF_ICMPGE : Opcodes.IF_ICMPLT;
                functionGenerationContext.getMv().visitJumpInsn(opcode, labelToJump);
            }
        } else {
            err("[Error] Unknown relational operator.");
        }

        if (!this.jumpLabelsHaveBlocks) {
            // the argument passed in the next line
            // is the simplification of: this.jumpToFalseLabel ? true : false
            functionGenerationContext.getMv().visitLdcInsn(this.jumpToFalseLabel ? 1 : 0);

            functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, nextLabel);
            functionGenerationContext.getMv().visitLabel(labelToJump);
            functionGenerationContext.getMv().visitFrame(
                    Opcodes.F_NEW,
                    currentFrameStack.numLocals, currentFrameStack.locals,
                    currentFrameStack.numStack, currentFrameStack.stack
            );

            // the argument passed in the next line
            // is the simplification of: this.jumpToFalseLabel ? false : true
            functionGenerationContext.getMv().visitLdcInsn(this.jumpToFalseLabel ? 0 : 1);
            currentFrameStack = functionGenerationContext.getCurrentFrameStackInfo();
            functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, nextLabel);

            functionGenerationContext.getMv().visitLabel(nextLabel);
            // see the Javadoc about visitFrame to know what is Opcodes.F_SAME1
            functionGenerationContext.getMv().visitFrame(
                    Opcodes.F_NEW,
                    currentFrameStack.numLocals, currentFrameStack.locals,
                    currentFrameStack.numStack, currentFrameStack.stack
            );
        }

        return null;
    }

    @Override
    public Void visitBooleanExprOr(SamosaParser.BooleanExprOrContext ctx) {
        // TODO: implement short circuiting
        visit(ctx.booleanExpr(0));
        visit(ctx.booleanExpr(1));
        functionGenerationContext.getMv().visitInsn(Opcodes.IOR);
        return null;
    }

    @Override
    public Void visitBooleanExprNot(SamosaParser.BooleanExprNotContext ctx) {
        // I couldn't find a JVM "not" instruction.
        // So, here's a little trick: xoring anything with true gives its complement
        visit(ctx.booleanExpr());
        functionGenerationContext.getMv().visitLdcInsn(1);
        functionGenerationContext.getMv().visitInsn(Opcodes.IXOR);
        return null;
    }

    @Override
    public Void visitBooleanExprCompOp(SamosaParser.BooleanExprCompOpContext ctx) {
        var lhs = ctx.expr(0);
        var rhs = ctx.expr(1);

        var typeDetector = new ExpressionTypeDetector(symbolTable);
        var lhsType = typeDetector.getType(lhs);
        var rhsType = typeDetector.getType(rhs);

        // check for incompatible types
        if ((!lhsType.getFirst() || !rhsType.getFirst())
                || (lhsType.getSecond() != rhsType.getSecond())
                || !lhsType.getSecond().getCanBeUsedWithCompOp()
        ) {
            return null;
        }

        var theCompOp = ctx.compOp();
        var exprType = lhsType.getSecond();

        // These are the types for which we support the comp ops for now
        switch(exprType) {
            case INT:
                IntExprCodegen intExprCodegen = new IntExprCodegen(ctx.expr(0), symbolTable, functionGenerationContext, className, packageName);
                intExprCodegen.doCodegen();
                intExprCodegen.setExprContext(ctx.expr(1));
                intExprCodegen.doCodegen();
                break;
            case STRING:
                StringExprCodegen stringExprCodegen = new StringExprCodegen(ctx.expr(0), symbolTable, functionGenerationContext, className, packageName);
                stringExprCodegen.doCodegen();
                stringExprCodegen.setExprContext(ctx.expr(1));
                stringExprCodegen.doCodegen();
                break;
            case BOOL:
                BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(null, symbolTable, functionGenerationContext, className, packageName);
                booleanExprCodegen.doSpecialCodegen(ctx.expr(0));
                booleanExprCodegen.doSpecialCodegen(ctx.expr(1));
                break;
        }

        Label labelToJump = new Label();
        conditionJumpLabel = labelToJump;
        nextLabel = new Label();
        // save current stack map
        var currentFrameStack = functionGenerationContext.getCurrentFrameStackInfo();

        if (!exprType.getCanBeUsedWithCompOp()) {
            fmtfatalerr("Cannot compare given types.", ctx.start.getLine());
            return null;
        }

        if (theCompOp.COMP() != null) {
            switch (exprType) {
                case INT:
                case BOOL:
                    int opcode = Opcodes.IF_ICMPEQ;//this.jumpToFalseLabel ? Opcodes.IF_ICMPNE : Opcodes.IF_ICMPEQ;
                    functionGenerationContext.getMv().visitJumpInsn(opcode, labelToJump);
                    break;
                case STRING:
                    opcode = Opcodes.IF_ACMPEQ;//this.jumpToFalseLabel ? Opcodes.IF_ICMPNE : Opcodes.IF_ICMPEQ;
                    functionGenerationContext.getMv().visitJumpInsn(opcode, labelToJump);
                    break;
            }
        } else if (theCompOp.COMPNOTEQ() != null) {
            switch (exprType) {
                case INT:
                case BOOL:
                    int opcode = Opcodes.IF_ICMPNE;//this.jumpToFalseLabel ? Opcodes.IF_ICMPNE : Opcodes.IF_ICMPEQ;
                    functionGenerationContext.getMv().visitJumpInsn(opcode, labelToJump);
                    break;
                case STRING:
                    opcode = Opcodes.IF_ACMPNE;//this.jumpToFalseLabel ? Opcodes.IF_ICMPNE : Opcodes.IF_ICMPEQ;
                    functionGenerationContext.getMv().visitJumpInsn(opcode, labelToJump);
                    break;
            }
        } else {
            err("[Error] Unknown relational operator.");
        }

        if (!this.jumpLabelsHaveBlocks) {
            // the argument passed in the next line
            // is the simplification of: this.jumpToFalseLabel ? true : false
            functionGenerationContext.getMv().visitLdcInsn(this.jumpToFalseLabel ? 1 : 0);

            functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, nextLabel);
            functionGenerationContext.getMv().visitLabel(labelToJump);
            functionGenerationContext.getMv().visitFrame(
                    Opcodes.F_NEW,
                    currentFrameStack.numLocals, currentFrameStack.locals,
                    currentFrameStack.numStack, currentFrameStack.stack
            );

            // the argument passed in the next line
            // is the simplification of: this.jumpToFalseLabel ? false : true
            functionGenerationContext.getMv().visitLdcInsn(this.jumpToFalseLabel ? 0 : 1);
            currentFrameStack = functionGenerationContext.getCurrentFrameStackInfo();
            functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, nextLabel);

            functionGenerationContext.getMv().visitLabel(nextLabel);
            // see the Javadoc about visitFrame to know what is Opcodes.F_SAME1
            functionGenerationContext.getMv().visitFrame(
                    Opcodes.F_NEW,
                    currentFrameStack.numLocals, currentFrameStack.locals,
                    currentFrameStack.numStack, currentFrameStack.stack
            );
        }

        return null;
    }

    @Override
    public Void visitBooleanExprParen(SamosaParser.BooleanExprParenContext ctx) {
        visit(ctx.booleanExpr());
        return null;
    }

    @Override
    public Void visitBooleanExprIdentifier(SamosaParser.BooleanExprIdentifierContext ctx) {
        String idName = ctx.IDENTIFIER().getText();
        doIdentifierCodegen(idName, symbolTable, Type.BOOLEAN_TYPE, functionGenerationContext, qualifiedClassName, Opcodes.ILOAD);
        return null;
    }

    /**
     * Used by doSpecialCodegen().
     * @param ctx   Appropriate context.
     */
    @Override
    public Void visitExprIdentifier(SamosaParser.ExprIdentifierContext ctx) {
        String idName = ctx.IDENTIFIER().getText();
        // Let's trust the static type checker here and assume that this identifier is of boolean type
        doIdentifierCodegen(idName, symbolTable, Type.BOOLEAN_TYPE, functionGenerationContext, qualifiedClassName, Opcodes.ILOAD);
        return null;
    }

    @Override
    public Void visitBooleanTrue(SamosaParser.BooleanTrueContext ctx) {
        functionGenerationContext.getMv().visitLdcInsn(1);
        return null;
    }

    @Override
    public Void visitBooleanFalse(SamosaParser.BooleanFalseContext ctx) {
        functionGenerationContext.getMv().visitLdcInsn(0);
        return null;
    }

    @Override
    public Void visitBooleanExprXor(SamosaParser.BooleanExprXorContext ctx) {
        visit(ctx.booleanExpr(0));
        visit(ctx.booleanExpr(1));
        functionGenerationContext.getMv().visitInsn(Opcodes.IXOR); // TODO: TEST IF THIS WORKS!
        return null;
    }

    @Override
    public Void visitBooleanExprAnd(SamosaParser.BooleanExprAndContext ctx) {
        // TODO: implement short circuiting
        visit(ctx.booleanExpr(0));
        visit(ctx.booleanExpr(1));
        functionGenerationContext.getMv().visitInsn(Opcodes.IAND);
        return null;
    }

    @Override
    public Void visitFunctionCallWithArgs(SamosaParser.FunctionCallWithArgsContext ctx) {
        FunctionCallCodegen functionCallCodegen = new FunctionCallCodegen(
                symbolTable, className, functionGenerationContext, className, packageName
        );
        functionCallCodegen.doWithArgFunctionCallCodegen(ctx, false); // do not discard result
        return null;
    }

    @Override
    public Void visitFunctionCallNoArgs(SamosaParser.FunctionCallNoArgsContext ctx) {
        FunctionCallCodegen functionCallCodegen = new FunctionCallCodegen(
                symbolTable, className, functionGenerationContext, className, packageName
        );
        functionCallCodegen.doNoArgFunctionCallCodegen(ctx, false); // do not discard result
        return null;
    }
}
