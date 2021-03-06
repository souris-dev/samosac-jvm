package com.sachett.samosa.builtins;

import com.sachett.samosa.logging.LoggingUtilsKt;
import com.sachett.samosa.parser.SamosaParser;
import com.sachett.samosa.samosac.codegen.function.FunctionGenerationContext;
import com.sachett.samosa.samosac.staticchecker.ExpressionTypeDetector;
import com.sachett.samosa.samosac.symbol.*;
import com.sachett.samosa.samosac.symbol.symboltable.SymbolTable;
import kotlin.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Provides support for builtin functions and variables.
 */
public class Builtins {
    /**
     * Provides codegen for builtin functions.
     * The info about the public static functions defined in this class are dynamically populated into the
     * symbol table at compilation.
     */
    public static class Functions {
        /**
         * A functional interface. The loadArgumentsToStack() method
         * will be called by builtins codegens to load arguments to stack.
         */
        public interface FunctionArgsLoader {
            void loadArgumentsToStack();
        }

        /**
         * Some codegen functions for builtin functions will need some more parameters to
         * generate the code. This interface serves as a base for them.
         */
        public interface IBuiltinFunctionCodegenParams {}

        public static IBuiltinFunctionCodegenParams currentBuiltinFunctionCodegenParams = null;

        /**
         * TODO: (Refactor) Move this class somewhere else?
         */
        public static class Utils {
            /**
             * Converts a descriptorString to a corresponding FunctionSymbol.
             * NOTE: DOES NOT HANDLE ARRAYS AND OBJECTS OF ANY OTHER EXCEPT STRING FOR NOW.
             * @param descriptorString The descriptor string of the method.
             * @param name             The name of the function symbol to be made.
             * @param method           The java.lang.reflect.Method instance of the method.
             * @return FunctionSymbol corresponding to the given descriptor string.
             */
            public static FunctionSymbol descriptorToFunctionSymbol(String descriptorString, String name, Method method) {
                // TODO: DOES NOT PARSE ARRAYS IN DESCRIPTOR STRING FOR NOW!
                ArrayList<ISymbol> params = new ArrayList<>();
                SymbolType funcRetType = SymbolType.VOID;

                var paramNamesAnnotation = method.getAnnotationsByType(Functions.SamosaBuiltinFuncOverload.class);
                var paramNames = Arrays.stream(paramNamesAnnotation)
                        .filter((annotation) -> annotation.descriptorString().equals(descriptorString))
                        .collect(Collectors.toList()).get(0).paramNames();
                int strPos = 0;
                int paramNamesCount = 0;

                boolean parsingParams = true;

                while (strPos < descriptorString.length()) {
                    char thisChar = descriptorString.charAt(strPos);

                    switch (thisChar) {
                        case '(':
                            strPos++;
                            break;
                        case ')':
                            parsingParams = false;
                            strPos++;
                            break;
                        case 'I':
                            if (!parsingParams) {
                                funcRetType = SymbolType.INT;
                                strPos++;
                                continue;
                            }
                            params.add(new IntSymbol(
                                    paramNames[paramNamesCount],
                                    -1,
                                    false,
                                    (Integer) SymbolType.INT.getDefaultValue(),
                                    false,
                                    false,
                                    new Pair<Integer, Integer>(-1, -1)
                            ));
                            strPos++;
                            break;
                        case 'Z':
                            if (!parsingParams) {
                                funcRetType = SymbolType.BOOL;
                                strPos++;
                                continue;
                            }
                            params.add(new BoolSymbol(
                                    paramNames[paramNamesCount],
                                    -1,
                                    false,
                                    (Boolean) SymbolType.BOOL.getDefaultValue(),
                                    false,
                                    false,
                                    new Pair<Integer, Integer>(-1, -1)
                            ));
                            strPos++;
                            break;
                        case 'L':
                            int classNameEndPos = strPos;
                            while (descriptorString.charAt(classNameEndPos) != ';') {
                                classNameEndPos++;
                            }
                            String className = descriptorString.substring(strPos + 1, classNameEndPos).replace("/", ".");
                            if (className.equals("java.lang.String")) {
                                if (!parsingParams) {
                                    funcRetType = SymbolType.STRING;
                                    strPos = classNameEndPos + 1;
                                    continue;
                                }
                                params.add(new StringSymbol(
                                        paramNames[paramNamesCount],
                                        -1,
                                        false,
                                        (String) SymbolType.STRING.getDefaultValue(),
                                        false,
                                        false,
                                        new Pair<Integer, Integer>(-1, -1)
                                ));
                            } // TODO: Add support for objects of other class types
                            strPos = classNameEndPos + 1;
                            break;
                        case 'V':
                            if (!parsingParams) {
                                funcRetType = SymbolType.VOID;
                                strPos++;
                            }
                            break;
                        default:
                            strPos++;
                    }
                }

                return new FunctionSymbol(
                        name,
                        -1,
                        params,
                        funcRetType,
                        false, true, true,
                        new Pair<Integer, Integer>(-1, -1)
                );
            }

            /**
             * Convert parser context to (partial) descriptor. This descriptor only contains argument types.
             * @param ctx
             * @param symbolTable
             * @return (Partial) descriptor for the required function.
             */
            public static String ctxToDescriptor(SamosaParser.FunctionCallWithArgsContext ctx, SymbolTable symbolTable) {
                var ctxCallParamsChildren = ctx.callArgList().children;
                var normalParams = ctx.callArgList().callParams;
                var booleanParams = ctx.callArgList().booleanCallParams;
                StringBuilder descriptor = new StringBuilder("(");

                var normalParamsCounter = 0;
                var booleanParamsCounter = 0;
                var paramsCounter = 0;

                ExpressionTypeDetector typeDetector = new ExpressionTypeDetector(symbolTable);
                for (ParseTree pt : ctxCallParamsChildren) {
                    var ptClassName = pt.getClass().getName();
                    // Kind of a hack, but well it does the job
                    if (ptClassName.contains("Expr") && !ptClassName.contains("Boolean")) {
                        if (ptClassName.contains("ExprFunctionCall")) {

                        }
                        // normal expression
                        var exprCtx = normalParams.get(normalParamsCounter);
                        Pair<Boolean, SymbolType> typeInfo = typeDetector.getType(exprCtx);

                        if (!typeInfo.getFirst()) {
                            LoggingUtilsKt.fmtfatalerr("Bad expression passed as argument (incompatible types).", ctx.start.getLine());
                        }

                        switch (typeInfo.getSecond()) {
                            case INT:
                                descriptor.append("I");
                                break;
                            case BOOL:
                                descriptor.append("Z");
                                break;
                            case STRING:
                                descriptor.append("Ljava/lang/String;");
                                break;
                        }

                        normalParamsCounter++;
                        paramsCounter++;
                    }
                    else if (ptClassName.contains("Boolean")) {
                        descriptor.append("Z");
                        booleanParamsCounter++;
                        paramsCounter++;
                    }
                }

                descriptor.append(")");
                return descriptor.toString();
            }

            public static void setBuiltinFunctionCodegenParams(IBuiltinFunctionCodegenParams params) {
                currentBuiltinFunctionCodegenParams = params;
            }
        }

        @Retention(RetentionPolicy.RUNTIME)
        public @interface SamosaBuiltinFuncOverloads {
            SamosaBuiltinFuncOverload[] value();
        }

        @Repeatable(SamosaBuiltinFuncOverloads.class)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface SamosaBuiltinFuncOverload {
            String descriptorString() default "()V";
            String[] paramNames() default {};
        }

        @Retention(RetentionPolicy.RUNTIME)
        public @interface SamosaBuiltinFuncName {
            String name();
        }

        @Retention(RetentionPolicy.RUNTIME)
        public @interface RequiresCodegenParams {}

        // ---------------- BUILTINS --------------------------

        @SamosaBuiltinFuncName(name = "putout")
        @SamosaBuiltinFuncOverload(descriptorString = "(I)V", paramNames = {"intToPrint"})
        public static void putoutInt(
                FunctionArgsLoader functionArgsLoader,
                FunctionGenerationContext functionGenerationCtx
        ) {
            doPutout(functionArgsLoader, functionGenerationCtx, SymbolType.INT);
        }

        @SamosaBuiltinFuncName(name = "putout")
        @SamosaBuiltinFuncOverload(descriptorString = "(Z)V", paramNames = {"boolieToPrint"})
        public static void putoutBoolie(
                FunctionArgsLoader functionArgsLoader,
                FunctionGenerationContext functionGenerationCtx
        ) {
            doPutout(functionArgsLoader, functionGenerationCtx, SymbolType.BOOL);
        }

        @SamosaBuiltinFuncName(name = "putout")
        @SamosaBuiltinFuncOverload(descriptorString = "(Ljava/lang/String;)V", paramNames = {"stringToPrint"})
        public static void putoutString(
                FunctionArgsLoader functionArgsLoader,
                FunctionGenerationContext functionGenerationCtx
        ) {
            doPutout(functionArgsLoader, functionGenerationCtx, SymbolType.STRING);
        }

        @SamosaBuiltinFuncName(name = "putinInt")
        @SamosaBuiltinFuncOverload(descriptorString = "()I")
        public static void putinInt(
                FunctionArgsLoader functionArgsLoader,
                FunctionGenerationContext functionGenerationCtx
        ) {
            doPutin(functionGenerationCtx, SymbolType.INT);
        }

        @SamosaBuiltinFuncName(name = "putinBoolie")
        @SamosaBuiltinFuncOverload(descriptorString = "()Z")
        public static void putinBoolie(
                FunctionArgsLoader functionArgsLoader,
                FunctionGenerationContext functionGenerationCtx
        ) {
            doPutin(functionGenerationCtx, SymbolType.BOOL);
        }

        @SamosaBuiltinFuncName(name = "putinString")
        @SamosaBuiltinFuncOverload(descriptorString = "()Ljava/lang/String;")
        public static void putinString(
                FunctionArgsLoader functionArgsLoader,
                FunctionGenerationContext functionGenerationCtx
        ) {
            doPutin(functionGenerationCtx, SymbolType.STRING);
        }

        @SamosaBuiltinFuncName(name = "stoi")
        @SamosaBuiltinFuncOverload(descriptorString = "(Ljava/lang/String;)I", paramNames = {"stringToConvert"})
        public static void stringToInt(
                FunctionArgsLoader functionArgsLoader,
                FunctionGenerationContext functionGenerationContext
        ) {
            functionArgsLoader.loadArgumentsToStack();

            functionGenerationContext.getMv().visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer",
                    "parseInt", "(Ljava/lang/String;)I", false);
        }

        @SamosaBuiltinFuncName(name = "itos")
        @SamosaBuiltinFuncOverload(descriptorString = "(I)Ljava/lang/String;", paramNames = {"stringToConvert"})
        public static void intToString(
                FunctionArgsLoader functionArgsLoader,
                FunctionGenerationContext functionGenerationContext
        ) {
            functionArgsLoader.loadArgumentsToStack();

            functionGenerationContext.getMv().visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer",
                    "toString", "(I)Ljava/lang/String;", false);
        }

        public static class GotoBeginningParams implements IBuiltinFunctionCodegenParams {
            String className;

            public GotoBeginningParams(String className) {
                this.className = className;
            }
        }

        @SamosaBuiltinFuncName(name = "main")
        @SamosaBuiltinFuncOverload(descriptorString = "()V", paramNames = {})
        @RequiresCodegenParams()
        // TODO: Not yet implemented fully
        public static void gotoBeginning(
                FunctionArgsLoader functionArgsLoader,
                FunctionGenerationContext functionGenerationContext
        ) {
            LoggingUtilsKt.err("[Error] The function call: () -> main to start from the beginning" +
                    " is currently not supported. ");

            if (!(currentBuiltinFunctionCodegenParams instanceof GotoBeginningParams)) {
                return;
            }

            String className = ((GotoBeginningParams) currentBuiltinFunctionCodegenParams).className;

            // TODO: we somehow need to put the cmdline args received back on stack before the next line
            functionGenerationContext.getMv().visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    className,
                    "main",
                    "([Ljava/lang/String;)V",
                    false
            );

            // clear this
            currentBuiltinFunctionCodegenParams = null;
        }

        /**
         * Displays text to stdout (or stderr). Also adds an end-line at the end.
         * Function call example: ("hello") -> println.
         * Will load arguments on stack using argsLoader; required arguments: one single value of type symbolTypeToPrint
         * that will be output to stdout or stderr.
         *
         * @param argsLoader            An implementation of the functional interface FunctionArgsLoader (a lambda) whose
         *                              loadArgumentsToStack() method will be called internally to load arguments to stack.
         *                              In case of putout, this should load a single value of type symbolTypeToPrint onto the stack.
         * @param functionGenerationCtx The function generation context in which to place the function call.
         * @param symbolTypeToPrint     The symbol type to print to screen.
         *                              Can take SymbolType.INT, BOOL or STRING for now.
         */
        private static void doPutout(
                FunctionArgsLoader argsLoader,
                FunctionGenerationContext functionGenerationCtx,
                SymbolType symbolTypeToPrint
        ) {
            functionGenerationCtx.getMv().visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
                    "Ljava/io/PrintStream;");
            argsLoader.loadArgumentsToStack();

            String printlnDescriptor = "(Ljava/lang/String;)V";
            switch (symbolTypeToPrint) {
                case BOOL:
                    printlnDescriptor = "(Z)V";
                    break;
                case INT:
                    printlnDescriptor = "(I)V";
                    break;
                case STRING:
                    printlnDescriptor = "(Ljava/lang/String;)V";
                    break;
            }

            functionGenerationCtx.getMv().visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                    "println", printlnDescriptor, false);
        }

        /**
         * Inputs a value from stdin using Scanner. The value taken as input is present on the stack on top after this function finishes.
         *
         * @param functionGenerationCtx The function generation context in which to place the function call to this.
         * @param symbolTypeToInput     The type of value to be taken as input. Can be INT, BOOL, or STRING.
         */
        private static void doPutin(
                FunctionGenerationContext functionGenerationCtx,
                SymbolType symbolTypeToInput
        ) {
            // Check if it is a compatible symbol type
            if ((symbolTypeToInput != SymbolType.INT)
                    && (symbolTypeToInput != SymbolType.BOOL)
                    && (symbolTypeToInput != SymbolType.STRING)) {
                return;
            }
            // Make a scanner
            functionGenerationCtx.getMv().visitTypeInsn(Opcodes.NEW, "java/util/Scanner");
            functionGenerationCtx.getMv().visitInsn(Opcodes.DUP);
            functionGenerationCtx.getMv().visitFieldInsn(
                    Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;"
            );
            functionGenerationCtx.getMv().visitMethodInsn(
                    Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false
            );

            // scan next required thing
            switch (symbolTypeToInput) {
                case INT:
                    functionGenerationCtx.getMv().visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false
                    );
                    break;
                case BOOL:
                    functionGenerationCtx.getMv().visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextBoolean", "()Z", false
                    );
                    break;
                case STRING:
                    functionGenerationCtx.getMv().visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String;", false
                    );
                    break;
            }
        }

        /**
         * Uses System.exit to exit.
         * Expects arguments to be loaded by argsLoader; expected arguments: one int value (exit status code).
         *
         * @param argsLoader                An implementation of the functional interface FunctionArgsLoader (a lambda) whose
         *                                  loadArgumentsToStack() method will be called internally to load expected arguments to stack.
         * @param functionGenerationContext The function generation context in which to place the function call to this.
         */
        @SamosaBuiltinFuncName(name = "exit")
        @SamosaBuiltinFuncOverload(descriptorString = "(I)V", paramNames = {"exitCode"})
        public static void exit(FunctionArgsLoader argsLoader, FunctionGenerationContext functionGenerationContext) {
            argsLoader.loadArgumentsToStack();

            functionGenerationContext.getMv().visitMethodInsn(
                    Opcodes.INVOKESTATIC, "java/lang/System", "exit", "(I)V", false
            );
        }
    }
}
