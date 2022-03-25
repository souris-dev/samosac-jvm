package com.sachett.samosa.samosac.codegen;

import com.sachett.samosa.parser.SamosaBaseVisitor;
import com.sachett.samosa.parser.SamosaParser;
import com.sachett.samosa.samosac.codegen.compoundstmt.ControlNodeCodegenType;
import com.sachett.samosa.samosac.codegen.compoundstmt.IControlNodeCodegen;
import com.sachett.samosa.samosac.codegen.compoundstmt.IfStmtCodegen;
import com.sachett.samosa.samosac.codegen.compoundstmt.WhileStmtCodegen;
import com.sachett.samosa.samosac.codegen.expressions.BooleanExprCodegen;
import com.sachett.samosa.samosac.codegen.expressions.IntExprCodegen;
import com.sachett.samosa.samosac.codegen.expressions.StringExprCodegen;
import com.sachett.samosa.samosac.codegen.function.FunctionCallCodegen;
import com.sachett.samosa.samosac.codegen.function.FunctionGenerationContext;
import com.sachett.samosa.samosac.codegen.utils.delegation.CodegenDelegatable;
import com.sachett.samosa.samosac.symbol.ISymbol;
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayDeque;

import static com.sachett.samosa.logging.LoggingUtilsKt.err;

/**
 * This class has methods for common codegen constructs.
 */
public class CodegenCommons extends SamosaBaseVisitor<Void> {
    protected final String className;
    protected final String packageName;
    protected FunctionGenerationContext functionGenerationContext;
    protected final SymbolTable symbolTable;

    /**
     * Stack of WhileStmtCodeGens (for nested while statements).
     */
    protected final ArrayDeque<IControlNodeCodegen> controlNodeCodegens = new ArrayDeque<>();

    protected CodegenDelegatable parentCodegen; // parent codegen class instance
    public CodegenDelegatable getParentCodegen() {
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
    public Void visitBlock(SamosaParser.BlockContext ctx) {
        // keep track of scopes in the symbol table
        Pair<Integer, Integer> blockStart = new Pair<>(ctx.start.getLine(), ctx.start.getCharPositionInLine());
        symbolTable.goToBlock(blockStart);
        parentCodegen.visitChildren(ctx);
        symbolTable.restoreLastCoordinates();
        return null;
    }

    public Void visitExprAssign(SamosaParser.ExprAssignContext ctx) {
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
        return null;
    }

    @Override
    public Void visitIfStmt(SamosaParser.IfStmtContext ctx) {
        IfStmtCodegen ifStmtCodegen = new IfStmtCodegen(
                parentCodegen,
                functionGenerationContext,
                symbolTable,
                className,
                packageName
        );

        controlNodeCodegens.push(ifStmtCodegen);
        parentCodegen.startDelegatingTo(ifStmtCodegen);
        ifStmtCodegen.generateIfStmt(ctx);
        parentCodegen.finishDelegating();
        controlNodeCodegens.pop();
        return null;
    }

    @Override
    public Void visitWhileStmt(SamosaParser.WhileStmtContext ctx) {
        WhileStmtCodegen whileStmtCodegen = new WhileStmtCodegen(
                parentCodegen,
                functionGenerationContext,
                symbolTable,
                className,
                packageName
        );

        controlNodeCodegens.push(whileStmtCodegen);
        parentCodegen.startDelegatingTo(whileStmtCodegen);
        whileStmtCodegen.generateWhileStmt(ctx);
        parentCodegen.finishDelegating();
        controlNodeCodegens.pop();
        return null;
    }

    /**
     * Checks if there's a WhileStmtCodegen in the stack (and return one if there's one).
     * Returns null if the stack does not contain a WhileStmtCodegen.
     */
    private WhileStmtCodegen getMostRecentWhileStmtCodegen() {
        // The loop below iterates of things getting popped off from the stack represented by the deque
        // (but does not actually pop anything)
        for (var elem : controlNodeCodegens) {
            if (elem.getControlNodeCodegenType() == ControlNodeCodegenType.WHILE) {
                return (WhileStmtCodegen) elem;
            }
        }
        return null;
    }

    @Override
    public Void visitBreakControlStmt(SamosaParser.BreakControlStmtContext ctx) {
        WhileStmtCodegen firstWhileStmtCodegenOnStack = getMostRecentWhileStmtCodegen();

        if (firstWhileStmtCodegenOnStack != null) {
            firstWhileStmtCodegenOnStack.visitBreakControlStmt(ctx);
        }
        return null;
    }

    @Override
    public Void visitContinueControlStmt(SamosaParser.ContinueControlStmtContext ctx) {
        WhileStmtCodegen firstWhileStmtCodegenOnStack = getMostRecentWhileStmtCodegen();

        if (firstWhileStmtCodegenOnStack != null) {
            firstWhileStmtCodegenOnStack.visitContinueControlStmt(ctx);
        }
        return null;
    }

    @Override
    public Void visitFunctionCallNoArgs(SamosaParser.FunctionCallNoArgsContext ctx) {
        // This should be called in the case of a function call statement (and not expression)
        FunctionCallCodegen functionCallCodegen = new FunctionCallCodegen(
                symbolTable, className, functionGenerationContext, className, packageName
        );
        functionCallCodegen.doNoArgFunctionCallCodegen(ctx, true); // discard result in case of a statement
        return null;
    }

    @Override
    public Void visitFunctionCallWithArgs(SamosaParser.FunctionCallWithArgsContext ctx) {
        // This should be called in the case of a function call statement (and not expression)
        FunctionCallCodegen functionCallCodegen = new FunctionCallCodegen(
                symbolTable, className, functionGenerationContext, className, packageName
        );
        functionCallCodegen.doWithArgFunctionCallCodegen(ctx, true); // discard result in case of a statement
        return null;
    }

    private void generateRandomNumber() {
        functionGenerationContext.getMv().visitTypeInsn(Opcodes.NEW, "java/util/Random");
        functionGenerationContext.getMv().visitInsn(Opcodes.DUP);
        functionGenerationContext.getMv().visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Random", "<init>", "()V", false);
        functionGenerationContext.getMv().visitIntInsn(Opcodes.BIPUSH, 101);
        functionGenerationContext.getMv().visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Random", "nextInt", "(I)I", false);
    }

    @Override
    public Void visitUncertainStatementSingle(SamosaParser.UncertainStatementSingleContext ctx) {
        generateRandomNumber();
        IntExprCodegen intExprCodegen = new IntExprCodegen(
                ctx.expr(),
                symbolTable,
                functionGenerationContext,
                className,
                packageName
        );
        intExprCodegen.doCodegen();

        // if the generated number is lower than or equal to the probability value given,
        // we execute the statement, else we skip it (comparison happens at runtime)
        Label endUncertaintyLabel = new Label();
        functionGenerationContext.getMv().visitJumpInsn(Opcodes.IF_ICMPGT, endUncertaintyLabel);
        parentCodegen.visit(ctx.statement());
        functionGenerationContext.getMv().visitLabel(endUncertaintyLabel);

        return null;
    }

    @Override
    public Void visitUncertainStatementMultiple(SamosaParser.UncertainStatementMultipleContext ctx) {
        generateRandomNumber();
        IntExprCodegen intExprCodegen = new IntExprCodegen(
                ctx.expr(),
                symbolTable,
                functionGenerationContext,
                className,
                packageName
        );
        intExprCodegen.doCodegen();

        // if the generated number is lower than or equal to the probability value given,
        // we execute the statement, else we skip it (comparison happens at runtime)
        Label secondStmt = new Label();
        functionGenerationContext.getMv().visitJumpInsn(Opcodes.IF_ICMPGT, secondStmt);
        parentCodegen.visit(ctx.statement(0));
        Label endUncertaintyLabel = new Label();
        functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, endUncertaintyLabel);
        functionGenerationContext.getMv().visitLabel(secondStmt);
        parentCodegen.visit(ctx.statement(1));
        functionGenerationContext.getMv().visitJumpInsn(Opcodes.GOTO, endUncertaintyLabel);
        functionGenerationContext.getMv().visitLabel(endUncertaintyLabel);

        return null;
    }

    @Override
    public Void visitUncertainCompoundStmtSingle(SamosaParser.UncertainCompoundStmtSingleContext ctx) {
        return super.visitUncertainCompoundStmtSingle(ctx);
    }

    @Override
    public Void visitUncertainCompoundStmtMultiple(SamosaParser.UncertainCompoundStmtMultipleContext ctx) {
        return super.visitUncertainCompoundStmtMultiple(ctx);
    }
}
