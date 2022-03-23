package com.sachett.slang.builtins;

import com.sachett.slang.slangc.codegen.function.FunctionGenerationContext;
import com.sachett.slang.slangc.symbol.*;
import kotlin.Pair;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

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

                var paramNamesAnnotation = method.getAnnotationsByType(Functions.SlangBuiltinFuncOverload.class);
                var paramNames = Arrays.stream(paramNamesAnnotation)
                        .filter((annotation) -> annotation.descriptorString().equals(descriptorString)).toList().get(0).paramNames();
                int strPos = 0;
                int paramNamesCount = 0;

                boolean parsingParams = true;

                while (strPos < descriptorString.length()) {
                    char thisChar = descriptorString.charAt(strPos);

                    switch (thisChar) {
                        case '(' -> {
                            strPos++;
                        }
                        case ')' -> {
                            parsingParams = false;
                            strPos++;
                        }
                        case 'I' -> {
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
                        }
                        case 'Z' -> {
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
                        }
                        case 'L' -> {
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
                        }
                        case 'V' -> {
                            if (!parsingParams) {
                                funcRetType = SymbolType.VOID;
                                strPos++;
                            }
                        }
                        default -> strPos++;
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
        }

        @Retention(RetentionPolicy.RUNTIME)
        public @interface SlangBuiltinFuncOverloads {
            SlangBuiltinFuncOverload[] value();
        }

        @Repeatable(SlangBuiltinFuncOverloads.class)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface SlangBuiltinFuncOverload {
            String descriptorString() default "()V";
            String[] paramNames() default {};
        }

        @Retention(RetentionPolicy.RUNTIME)
        public @interface SlangBuiltinFuncName {
            String name();
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
        @SlangBuiltinFuncName(name = "putout")
        @SlangBuiltinFuncOverload(descriptorString = "(I)V", paramNames = {"intToPrint"})
        @SlangBuiltinFuncOverload(descriptorString = "(Z)V", paramNames = {"boolieToPrint"})
        @SlangBuiltinFuncOverload(descriptorString = "(Ljava/lang/String;)V", paramNames = {"stringToPrint"})
        public static void putout(
                FunctionArgsLoader argsLoader,
                FunctionGenerationContext functionGenerationCtx,
                SymbolType symbolTypeToPrint,
                boolean err
        ) {
            functionGenerationCtx.getMv().visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", err ? "err" : "out",
                    "Ljava/io/PrintStream;");
            argsLoader.loadArgumentsToStack();

            String printlnDescriptor = "(Ljava/lang/String;)V";
            switch (symbolTypeToPrint) {
                case BOOL -> printlnDescriptor = "(Z)V";
                case INT -> printlnDescriptor = "(I)V";
                case STRING -> printlnDescriptor = "(Ljava/lang/String;)V";
            }

            functionGenerationCtx.getMv().visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                    "println", printlnDescriptor, false);
        }

        /**
         * Inputs a value from stdin. The value taken as input is present on the stack on top after this function finishes.
         *
         * @param functionGenerationCtx The function generation context in which to place the function call to this.
         * @param symbolTypeToInput     The type of value to be taken as input. Can be INT, BOOL, or STRING.
         */
        @SlangBuiltinFuncName(name = "putin")
        @SlangBuiltinFuncOverload(descriptorString = "()I")
        @SlangBuiltinFuncOverload(descriptorString = "()Z")
        @SlangBuiltinFuncOverload(descriptorString = "()Ljava/lang/String;")
        public static void putin(
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
                case INT -> functionGenerationCtx.getMv().visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false
                );
                case BOOL -> functionGenerationCtx.getMv().visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextBoolean", "()Z", false
                );
                case STRING -> functionGenerationCtx.getMv().visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String;", false
                );
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
        @SlangBuiltinFuncName(name = "exit")
        @SlangBuiltinFuncOverload(descriptorString = "(I)V", paramNames = {"exitCode"})
        public static void exit(FunctionArgsLoader argsLoader, FunctionGenerationContext functionGenerationContext) {
            argsLoader.loadArgumentsToStack();

            functionGenerationContext.getMv().visitMethodInsn(
                    Opcodes.INVOKESTATIC, "java/lang/System", "exit", "(I)V", false
            );
        }
    }
}
