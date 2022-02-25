package com.sachett.slang.slangc.codegen.expressions;

import com.sachett.slang.parser.SlangBaseVisitor;
import com.sachett.slang.parser.SlangParser;
import com.sachett.slang.slangc.codegen.function.FunctionCodeGen;
import com.sachett.slang.slangc.staticchecker.ExpressionTypeDetector;
import com.sachett.slang.slangc.symbol.SymbolType;
import com.sachett.slang.slangc.symbol.symboltable.SymbolTable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static com.sachett.slang.logging.LoggingUtilsKt.err;

public class BooleanExprCodeGen extends SlangBaseVisitor<Void> implements IExprCodeGen {
    private final SlangParser.BooleanExprContext exprContext;
    private final FunctionCodeGen functionCodeGen;
    private final SymbolTable symbolTable;
    private final String className;
    private final String packageName;
    private final String qualifiedClassName;

    private boolean insertLabelJumps = false;

    // jump instructions are inverted to jump to false labels
    // for example if it is var1 > var2, codegen generates jump to falseLabel if var1 <= var 2
    // and if jumpToFalseLabel is set to false:
    // for var1 > var2, codegen generates jump to trueLabel if var2 > var2
    private boolean jumpToFalseLabel = true;

    private Label trueLabel = new Label();
    private Label falseLabel = new Label();

    public BooleanExprCodeGen(
            SlangParser.BooleanExprContext exprContext,
            SymbolTable symbolTable,
            FunctionCodeGen functionCodeGen,
            String className,
            String packageName
    ) {
        this.exprContext = exprContext;
        this.functionCodeGen = functionCodeGen;
        this.symbolTable = symbolTable;
        this.packageName = packageName;
        this.className = className;
        this.qualifiedClassName = packageName.replace(".", "/") + className;
    }

    @Override
    public void doCodeGen() {
        visit(this.exprContext);
    }

    /**
     * Performs codeGen in cases such as:
     * aBoolVariable = () -> aFuncThatReturnsABool.
     * or,
     * aBoolVar = anotherBoolVar.
     * @param specialExprContext    The ExprContext corresponding to the RHS.
     */
    public void doSpecialCodeGen(SlangParser.ExprContext specialExprContext) {
        visit(specialExprContext);
    }

    public void setInsertLabelJumps(boolean insertLabelJumps) {
        this.insertLabelJumps = insertLabelJumps;
    }

    public void setTrueLabel(Label label) {
        this.trueLabel = label;
    }

    public void setFalseLabel(Label label) {
        this.falseLabel = label;
    }

    public void setJumpToFalseLabel(boolean jumpToFalseLabel) {
        this.jumpToFalseLabel = jumpToFalseLabel;
    }

    @Override
    public Void visitBooleanExprRelOp(SlangParser.BooleanExprRelOpContext ctx) {
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

        // Since we only have int expressions that can be compared using relops right now
        if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
            // evaluate the left and right sides of the relOp expression
            IntExprCodeGen intExprCodeGen = new IntExprCodeGen(ctx.expr(0), symbolTable, functionCodeGen, className, packageName);
            intExprCodeGen.doCodeGen();

            if (!this.insertLabelJumps) {
                functionCodeGen.getMv().visitInsn(Opcodes.I2L);
            }

            intExprCodeGen.setExprContext(ctx.expr(1));
            intExprCodeGen.doCodeGen();

            if (!this.insertLabelJumps) {
                functionCodeGen.getMv().visitInsn(Opcodes.I2L);
            }
        }

        if (theRelOp.GT() != null) {
            if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
                if (this.insertLabelJumps) {
                    int opcode = this.jumpToFalseLabel ? Opcodes.IF_ICMPLE : Opcodes.IF_ICMPGT;
                    Label labelToJump = this.jumpToFalseLabel ? this.falseLabel : this.trueLabel;
                    functionCodeGen.getMv().visitJumpInsn(opcode, labelToJump);
                } else {
                    functionCodeGen.getMv().visitInsn(Opcodes.LCMP);
                }
            }
        } else if (theRelOp.GTEQ() != null) {
            if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
                if (this.insertLabelJumps) {
                    int opcode = this.jumpToFalseLabel ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPGE;
                    Label labelToJump = this.jumpToFalseLabel ? this.falseLabel : this.trueLabel;
                    functionCodeGen.getMv().visitJumpInsn(opcode, labelToJump);
                } else {
                    functionCodeGen.getMv().visitInsn(Opcodes.LCMP);
                }
            }
        } else if (theRelOp.LT() != null) {
            if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
                if (this.insertLabelJumps) {
                    int opcode = this.jumpToFalseLabel ? Opcodes.IF_ICMPGE : Opcodes.IF_ICMPLT;
                    Label labelToJump = this.jumpToFalseLabel ? this.falseLabel : this.trueLabel;
                    functionCodeGen.getMv().visitJumpInsn(opcode, labelToJump);
                } else {
                    functionCodeGen.getMv().visitInsn(Opcodes.LCMP);
                }
            }
        } else if (theRelOp.LTEQ() != null) {
            if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
                if (this.insertLabelJumps) {
                    int opcode = this.jumpToFalseLabel ? Opcodes.IF_ICMPGT : Opcodes.IF_ICMPLE;
                    Label labelToJump = this.jumpToFalseLabel ? this.falseLabel : this.trueLabel;
                    functionCodeGen.getMv().visitJumpInsn(opcode, labelToJump);
                } else {
                    functionCodeGen.getMv().visitInsn(Opcodes.LCMP);
                }
            }
        } else {
            err("[Error] Unknown relational operator.");
        }

        return null;
    }

    @Override
    public Void visitBooleanExprOr(SlangParser.BooleanExprOrContext ctx) {
        // TODO: implement short circuiting
        visit(ctx.booleanExpr(0));
        visit(ctx.booleanExpr(1));
        functionCodeGen.getMv().visitInsn(Opcodes.IOR);
        return null;
    }

    @Override
    public Void visitBooleanExprNot(SlangParser.BooleanExprNotContext ctx) {
        // I couldn't find a JVM "not" instruction.
        // So, here's a little trick: xoring anything with true gives its complement
        visit(ctx.booleanExpr());
        functionCodeGen.getMv().visitLdcInsn(true);
        functionCodeGen.getMv().visitInsn(Opcodes.IXOR);
        return null;
    }

    @Override
    public Void visitBooleanExprCompOp(SlangParser.BooleanExprCompOpContext ctx) {
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

        var theRelOp = ctx.compOp();

        // Since we only have int expressions that can be compared using relops right now
        if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
            // evaluate the left and right sides of the relOp expression
            IntExprCodeGen intExprCodeGen = new IntExprCodeGen(ctx.expr(0), symbolTable, functionCodeGen, className, packageName);
            intExprCodeGen.doCodeGen();
            intExprCodeGen.setExprContext(ctx.expr(1));
            intExprCodeGen.doCodeGen();
        }

        if (theRelOp.COMP() != null) {
            if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
                if (this.insertLabelJumps) {
                    int opcode = this.jumpToFalseLabel ? Opcodes.IF_ICMPNE : Opcodes.IF_ICMPEQ;
                    Label labelToJump = this.jumpToFalseLabel ? this.falseLabel : this.trueLabel;
                    functionCodeGen.getMv().visitJumpInsn(opcode, labelToJump);
                } else {
                    functionCodeGen.getMv().visitInsn(Opcodes.LCMP);
                }
            }
        } else if (theRelOp.COMPNOTEQ() != null) {
            if (lhsType.getSecond() == SymbolType.INT && rhsType.getSecond() == SymbolType.INT) {
                if (this.insertLabelJumps) {
                    int opcode = this.jumpToFalseLabel ? Opcodes.IF_ICMPEQ : Opcodes.IF_ICMPNE;
                    Label labelToJump = this.jumpToFalseLabel ? this.falseLabel : this.trueLabel;
                    functionCodeGen.getMv().visitJumpInsn(opcode, labelToJump);
                } else {
                    functionCodeGen.getMv().visitInsn(Opcodes.LCMP);
                }
            }
        } else {
            err("[Error] Unknown comparison operator.");
        }

        return null;
    }

    @Override
    public Void visitBooleanExprParen(SlangParser.BooleanExprParenContext ctx) {
        visit(ctx.booleanExpr());
        return null;
    }

    @Override
    public Void visitBooleanExprIdentifier(SlangParser.BooleanExprIdentifierContext ctx) {
        String idName = ctx.IDENTIFIER().getText();
        doIdentifierCodegen(idName, symbolTable, Type.BOOLEAN_TYPE, functionCodeGen, qualifiedClassName, Opcodes.ILOAD);
        return null;
    }

    /**
     * Used by doSpecialCodeGen().
     * @param ctx   Appropriate context.
     */
    @Override
    public Void visitExprIdentifier(SlangParser.ExprIdentifierContext ctx) {
        String idName = ctx.IDENTIFIER().getText();
        // Let's trust the static type checker here and assume that this identifier is of boolean type
        doIdentifierCodegen(idName, symbolTable, Type.BOOLEAN_TYPE, functionCodeGen, qualifiedClassName, Opcodes.ILOAD);
        return null;
    }

    @Override
    public Void visitBooleanTrue(SlangParser.BooleanTrueContext ctx) {
        functionCodeGen.getMv().visitLdcInsn(true);
        return null;
    }

    @Override
    public Void visitBooleanFalse(SlangParser.BooleanFalseContext ctx) {
        functionCodeGen.getMv().visitLdcInsn(false);
        return null;
    }

    @Override
    public Void visitBooleanExprXor(SlangParser.BooleanExprXorContext ctx) {
        visit(ctx.booleanExpr(0));
        visit(ctx.booleanExpr(1));
        functionCodeGen.getMv().visitInsn(Opcodes.IXOR); // TODO: TEST IF THIS WORKS!
        return null;
    }

    @Override
    public Void visitBooleanExprAnd(SlangParser.BooleanExprAndContext ctx) {
        // TODO: implement short circuiting
        visit(ctx.booleanExpr(0));
        visit(ctx.booleanExpr(1));
        functionCodeGen.getMv().visitInsn(Opcodes.IAND);
        return null;
    }

    @Override
    public Void visitFunctionCallWithArgs(SlangParser.FunctionCallWithArgsContext ctx) {
        // TODO: This is a DUMMY, to be implemented
        functionCodeGen.getMv().visitLdcInsn(SymbolType.BOOL.getDefaultValue());
        return null;
    }

    @Override
    public Void visitFunctionCallNoArgs(SlangParser.FunctionCallNoArgsContext ctx) {
        // TODO: This is a DUMMY, to be implemented
        functionCodeGen.getMv().visitLdcInsn(SymbolType.BOOL.getDefaultValue());
        return null;
    }
}
