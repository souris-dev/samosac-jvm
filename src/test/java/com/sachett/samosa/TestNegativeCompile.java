package com.sachett.samosa;

import com.sachett.samosa.samosac.compiler.CompilerKt;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestNegativeCompile {
    static final File programsDir = new File("src/test/data/negative-compile/test-programs");
    static final File programsCompOutputDir = new File("src/test/data/negative-compile/test-programs-comp-outputs");
    static final File programsCompErrorDir = new File("src/test/data/negative-compile/test-programs-comp-errors");
    static final File expectedCompErrorDir = new File("src/test/data/negative-compile/expected-compile-errors");
    static final File classFileOutDir = new File("src/test/data/negative-compile/out");

    @BeforeAll
    static void createDirs() {
        programsCompOutputDir.mkdirs();
        programsCompErrorDir.mkdirs();

        classFileOutDir.mkdirs();
    }

    @TestFactory
    Stream<DynamicTest> testCompileErrors() {
        // Compile the test cases first
        File[] sourceFiles = programsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".samo"));

        assertNotNull(sourceFiles, "No test source files found!");
        assertTrue(sourceFiles.length > 0, "No test source files found!");

        return Arrays.stream(sourceFiles).parallel().map((file) ->
                DynamicTest.dynamicTest("Test Source File: " + file.getName(), () -> {
                    // First compile the file
                    // Store the error and output of the compilation process into designated directories

                    if (!System.getProperties().containsKey("nocompile")) {
                        File compilationOutputFile = new File(
                                programsCompOutputDir.getPath() + File.separator + file.getName() + ".compile.log"
                        );
                        File compilationErrorFile = new File(
                                programsCompErrorDir.getPath() + File.separator + file.getName() + ".compile.err"
                        );
                        File expectedCompErrorFile = new File(
                                expectedCompErrorDir.getPath() + File.separator + file.getName() + ".compile.err.should"
                        );

                        compilationOutputFile.createNewFile();
                        compilationErrorFile.createNewFile();

                        PrintStream prevOut = System.out;
                        PrintStream prevErr = System.err;
                        PrintStream redirectedOut = new PrintStream(compilationOutputFile);
                        PrintStream redirectedErr = new PrintStream(compilationErrorFile);
                        System.setOut(redirectedOut);
                        System.setErr(redirectedErr);

                        // Do compilation
                        int exitStatus = catchSystemExit(() ->
                            CompilerKt.main(new String[]{ file.getAbsolutePath(), "-o" + classFileOutDir.getAbsolutePath() })
                        );

                        // Restore streams
                        System.setErr(prevErr);
                        System.setOut(prevOut);
                        assertNotEquals(exitStatus, 0, "Compilation exited with status 0. " +
                                "\nCompilation was expected to have failed with status code other than 0.");

                        // Check if the compilation error is as specified
                        assertTrue(
                                FileUtils.contentEqualsIgnoreEOL(compilationErrorFile, expectedCompErrorFile, null),
                                "Compilation failed with unexpected error for test source file: \n\t" + file.getAbsolutePath()
                                        + "." + "\nExpected compilation error can be found in: " + expectedCompErrorFile.getAbsolutePath()
                                + "\nActual error received during compilation is written in: " + compilationErrorFile.getAbsolutePath()
                        );
                    }
                })
        );
    }
}
