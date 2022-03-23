package com.sachett.slang.builtins;

import com.sachett.slang.slangc.codegen.function.FunctionGenerationContext;
import com.sachett.slang.slangc.symbol.SymbolType;
import org.objectweb.asm.Opcodes;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

        @Retention(RetentionPolicy.RUNTIME)
        public @interface SlangBuiltinFuncOverloads {
            SlangBuiltinFuncOverload[] value();
        }

        @Repeatable(SlangBuiltinFuncOverloads.class)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface SlangBuiltinFuncOverload {
            String descriptorString() default "()V";
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
        @SlangBuiltinFuncOverload(descriptorString = "(I)V")
        @SlangBuiltinFuncOverload(descriptorString = "(Z)V")
        @SlangBuiltinFuncOverload(descriptorString = "(Ljava/lang/String;)V")
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
        @SlangBuiltinFuncOverload(descriptorString = "(I)V")
        public static void exit(FunctionArgsLoader argsLoader, FunctionGenerationContext functionGenerationContext) {
            argsLoader.loadArgumentsToStack();

            functionGenerationContext.getMv().visitMethodInsn(
                    Opcodes.INVOKESTATIC, "java/lang/System", "exit", "(I)V", false
            );
        }
    }
}
