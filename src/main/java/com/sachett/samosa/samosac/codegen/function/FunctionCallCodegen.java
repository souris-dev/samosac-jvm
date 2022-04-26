package com.sachett.samosa.samosac.codegen.function;

import com.sachett.samosa.builtins.Builtins;
import com.sachett.samosa.logging.LoggingUtilsKt;
import com.sachett.samosa.parser.SamosaParser;
import com.sachett.samosa.samosac.codegen.expressions.BooleanExprCodegen;
import com.sachett.samosa.samosac.codegen.expressions.IntExprCodegen;
import com.sachett.samosa.samosac.codegen.expressions.StringExprCodegen;
import com.sachett.samosa.samosac.codegen.utils.delegation.CodegenDelegatable;
import com.sachett.samosa.samosac.staticchecker.ExpressionTypeDetector;
import com.sachett.samosa.samosac.symbol.FunctionSymbol;
import com.sachett.samosa.samosac.symbol.ISymbol;
import com.sachett.samosa.samosac.symbol.SymbolType;
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class FunctionCallCodegen extends CodegenDelegatable {
    private final SymbolTable symbolTable;
    private final String functionOwner;
    private final FunctionGenerationContext functionGenerationContext;

    private final String className;
    private final String packageName;

    /**
     * Initialize a function call codegen.
     * @param symbolTable   The shared symbol table.
     * @param owner         The owner of the function getting called.
     */
    public FunctionCallCodegen(
            SymbolTable symbolTable, String owner, FunctionGenerationContext functionGenerationContext,
            String className, String packageName
    ) {
        this.symbolTable = symbolTable;
        this.functionOwner = owner;
        this.functionGenerationContext = functionGenerationContext;
        this.className = className;
        this.packageName = packageName;
    }

    public void doNoArgFunctionCallCodegen(SamosaParser.FunctionCallNoArgsContext ctx, boolean discardResult) {
        // first try to find the function within this class
        String funcName = ctx.IDENTIFIER().getText();
        ISymbol functionSymbol = symbolTable.lookupInCoordinates(funcName, new Pair<>(0, 0));

        if (functionSymbol == null) {
            // TODO: find in the imported packages

            // builtins:
            var expectedDescriptor = "()";
            var theFunc = symbolTable.lookupBuiltinFunctionMatchingOverload(
                    funcName, expectedDescriptor
            );

            if (theFunc == null) {
                return;
            }

            Method theBuiltin = theFunc.getSecond();
            Builtins.Functions.FunctionArgsLoader argsLoader = () -> {};
            try {
                setBuiltinCodegenParams(theBuiltin);
                theBuiltin.invoke(null, argsLoader, functionGenerationContext);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        else if (functionSymbol instanceof FunctionSymbol) {
            // the function exists in this class
            // currently all generated methods are static in this class
            functionGenerationContext.getMv().visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    functionOwner,
                    funcName,
                    FunctionGenerationContext.generateDescriptor((FunctionSymbol) functionSymbol),
                    false
            );
        }
    }

    private void pushArgumentsToStack(FunctionSymbol funcSymbol, SamosaParser.FunctionCallWithArgsContext ctx) {
        int normalParamCounter = 0;
        int booleanParamCounter = 0;
        int argsPushed = 0;
        int totalArgs = funcSymbol.getParamList().size();
        ArrayList<ISymbol> argsList = funcSymbol.getParamList();
        var booleanPassedParams = ctx.callArgList().booleanCallParams;
        var normalPassedParams = ctx.callArgList().callParams;

        while (argsPushed < totalArgs) {
            switch (argsList.get(argsPushed).getSymbolType()) {
                case BOOL:
                    // We need to check if the normalPassedParams contains a boolean expr disguised as a normal one
                    // This can happen if it's just a single function call or an identifier with no boolean ops

                    if (normalPassedParams.size() > 0) {
                        ExpressionTypeDetector typeDetector = new ExpressionTypeDetector(symbolTable);
                        var typeDetectionResult = typeDetector.getType(
                                normalPassedParams.get(normalParamCounter)
                        );
                        if (typeDetectionResult.getSecond() == SymbolType.BOOL) {
                            // we've got a boolean expression disguised as a normal expression here
                            BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                                    null, symbolTable, functionGenerationContext, className, packageName
                            );
                            booleanExprCodegen.doSpecialCodegen(normalPassedParams.get(normalParamCounter));
                            normalParamCounter++;
                            break;
                        }
                    }

                    SamosaParser.BooleanExprContext boolExpr = booleanPassedParams.get(booleanParamCounter);
                    BooleanExprCodegen booleanExprCodegen = new BooleanExprCodegen(
                            boolExpr, symbolTable, functionGenerationContext, className, packageName
                    );
                    booleanExprCodegen.doCodegen();
                    booleanParamCounter++;
                    break;

                case INT:
                    SamosaParser.ExprContext intExpr = normalPassedParams.get(normalParamCounter);
                    IntExprCodegen intExprCodegen = new IntExprCodegen(
                            intExpr, symbolTable, functionGenerationContext, className, packageName
                    );
                    intExprCodegen.doCodegen();
                    normalParamCounter++;
                    break;

                case STRING:
                    SamosaParser.ExprContext stringExpr = normalPassedParams.get(normalParamCounter);
                    StringExprCodegen stringExprCodegen = new StringExprCodegen(
                            stringExpr, symbolTable, functionGenerationContext, className, packageName
                    );
                    stringExprCodegen.doCodegen();
                    normalParamCounter++;
                    break;
            }

            argsPushed++;
        }
    }

    private void setBuiltinCodegenParams(Method theBuiltin) {
        if (theBuiltin.isAnnotationPresent(Builtins.Functions.RequiresCodegenParams.class)) {
            if (!theBuiltin.isAnnotationPresent(Builtins.Functions.SamosaBuiltinFuncName.class)) {
                LoggingUtilsKt.err("Internal error: Builtin function " + theBuiltin.getName()
                        + " does not have an associated samosa builtin name.");
                return;
            }
            String samosaFuncName = theBuiltin.getAnnotationsByType(
                    Builtins.Functions.SamosaBuiltinFuncName.class
            )[0].name();
            // Extend this as required.
            switch (samosaFuncName) {
                case "main":
                    Builtins.Functions.Utils.setBuiltinFunctionCodegenParams(
                            new Builtins.Functions.GotoBeginningParams(className)
                    );
            }
        }
    }

    public void doWithArgFunctionCallCodegen(SamosaParser.FunctionCallWithArgsContext ctx, boolean discardResult) {
        // first try to find the function within this class
        String funcName = ctx.IDENTIFIER().getText();
        ISymbol functionSymbol = symbolTable.lookupInCoordinates(funcName, new Pair<>(0, 0));

        var stackSize = functionGenerationContext.getAnalyzerAdapter().stack != null ?
                functionGenerationContext.getAnalyzerAdapter().stack.size() : 0;

        if (functionSymbol == null) {
            // TODO: find in the imported packages

            // builtins:
            var expectedDescriptor = Builtins.Functions.Utils.ctxToDescriptor(ctx, symbolTable);
            var theFunc = symbolTable.lookupBuiltinFunctionMatchingOverload(
                    funcName, expectedDescriptor
            );

            if (theFunc == null) {
                return;
            }

            Method theBuiltin = theFunc.getSecond();
            Builtins.Functions.FunctionArgsLoader argsLoader = () -> {
                pushArgumentsToStack(theFunc.getFirst(), ctx);
            };
            try {
                setBuiltinCodegenParams(theBuiltin);
                theBuiltin.invoke(null, argsLoader, functionGenerationContext);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        else if (functionSymbol instanceof FunctionSymbol) {
            // the function exists in this class
            // currently all generated methods are static in this class

            var funcSymbol = (FunctionSymbol) functionSymbol;
            // first push its arguments to the stack
            pushArgumentsToStack(funcSymbol, ctx);

            functionGenerationContext.getMv().visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    functionOwner,
                    funcName,
                    FunctionGenerationContext.generateDescriptor(funcSymbol),
                    false
            );
        }

        if (discardResult) {
            // pop the stack once if it has a result
            var currentStackSize = functionGenerationContext.getAnalyzerAdapter().stack != null ?
                    functionGenerationContext.getAnalyzerAdapter().stack.size() : 0;
            if (currentStackSize - stackSize == 1) {
                // one result was pushed to stack
                // need to pop it since we are discarding the result
                functionGenerationContext.getMv().visitInsn(Opcodes.POP);
            }
        }
    }
}
