package com.sachett.samosa;

import com.sachett.samosa.samosac.compiler.CompilerKt;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class has tests for programs that are supposed to compile and run successfully.
 */
public class TestPositiveCompileRun {

    static final File programsDir = new File("src/test/data/positive-compile-run/test-programs");
    static final File programsCompOutputDir = new File("src/test/data/positive-compile-run/test-programs-comp-outputs");
    static final File programsCompErrorDir = new File("src/test/data/positive-compile-run/test-programs-comp-errors");
    static final File programsRunOutputDir = new File("src/test/data/positive-compile-run/test-programs-run-outputs");
    static final File programsRunErrorDir = new File("src/test/data/positive-compile-run/test-programs-run-errors");

    static final File expectedRunOutputDir = new File("src/test/data/positive-compile-run/expected-run-outputs");
    static final File expectedRunErrorDir = new File("src/test/data/positive-compile-run/expected-run-errors");
    static final File classFileOutDir = new File("src/test/data/positive-compile-run/out");

    @BeforeAll
    static void createDirs() {
        programsCompOutputDir.mkdirs();
        programsCompErrorDir.mkdirs();

        programsRunOutputDir.mkdirs();
        programsRunErrorDir.mkdirs();

        classFileOutDir.mkdirs();
    }

    @TestFactory
    Stream<DynamicTest> testRunOutputsOfSourceFiles() {
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

                   compilationOutputFile.createNewFile();
                   compilationErrorFile.createNewFile();

                   PrintStream prevOut = System.out;
                   PrintStream prevErr = System.err;
                   PrintStream redirectedOut = new PrintStream(compilationOutputFile);
                   PrintStream redirectedErr = new PrintStream(compilationErrorFile);
                   System.setOut(redirectedOut);
                   System.setErr(redirectedErr);

                   // Do compilation
                   CompilerKt.main(new String[]{ file.getAbsolutePath(), "-o" + classFileOutDir.getAbsolutePath() });

                   // Restore streams
                   System.setErr(prevErr);
                   System.setOut(prevOut);

                   // Check if there was any compilation error
                   assertEquals("",
                           FileUtils.readFileToString(compilationErrorFile, StandardCharsets.UTF_8).strip(),
                           "Compilation failed for test source file: \n\t" + file.getAbsolutePath()
                                   + "." + "\n" + "See " + compilationErrorFile.getAbsolutePath() + " for details."
                   );
               }

               // Then run the file
               // Store the error and output of the run process too

               if (!System.getProperties().containsKey("norun")) {
                   File runOutputFile = new File(
                           programsRunOutputDir.getPath() + File.separator + file.getName() + ".run.log"
                   );
                   File runErrorFile = new File(
                           programsRunErrorDir.getPath() + File.separator + file.getName() + ".run.err"
                   );
                   File expectedRunOutputFile = new File(
                           expectedRunOutputDir.getPath() + File.separator + file.getName() + ".run.log.should"
                   );
                   File expectedRunErrorFile = new File(
                           expectedRunErrorDir.getPath() + File.separator + file.getName() + ".run.err.should"
                   );

                   runOutputFile.createNewFile();
                   runErrorFile.createNewFile();

                   String classFileName = getClassFileNameFromFileName(file.getName());
                   ProcessBuilder runProcess = new ProcessBuilder("java", classFileName);
                   classFileOutDir.mkdirs();
                   runProcess.directory(classFileOutDir);
                   runProcess.redirectOutput(runOutputFile);
                   runProcess.redirectError(runErrorFile);

                   // Check for execution timeout
                   assertTrue(runProcess.start().waitFor(200, TimeUnit.SECONDS),
                           "Execution timed out for test source file: \n\t" + file.getAbsolutePath()
                   );

                   // Check if there was any run error
                   assertTrue(
                           FileUtils.contentEqualsIgnoreEOL(runErrorFile, expectedRunErrorFile, null),
                           "Execution failed unexpectedly for test source file: \n\t" + file.getAbsolutePath()
                                   + "." + "\n" + "See " + runErrorFile.getAbsolutePath() + " for details."
                   );

                   // Check if there is any difference in output (expected vs. result)
                   assertTrue(
                           FileUtils.contentEqualsIgnoreEOL(runOutputFile, expectedRunOutputFile, null),
                           "Unexpected output for test source file: \n\t" + file.getAbsolutePath()
                           + "." + "\n" + "Expected output is in file: \n\t" + expectedRunOutputFile.getAbsolutePath()
                           + "\n" + "Output received is written to file: \n\t" + runOutputFile.getAbsolutePath()
                   );
               }
           })
        );
    }

    // --------------  Utils  ------------------

    private String getClassFileNameFromFileName(String fileName) {
        String[] fileNameParts = fileName.split("\\.");
        StringBuilder genClassNameBuilder = new StringBuilder();

        for (String fileNamePart : fileNameParts) {
            // Capitalize the first letter of each part of the filename

            if (Objects.equals(fileNamePart, "")) {
                continue;
            }

            if (fileNamePart.contains("/")) {
                String[] dirParts = fileNamePart.split("[/\\\\]");
                fileNamePart = dirParts.length > 0 ? dirParts[dirParts.length - 1] : fileNamePart;
            }

            String modFileNamePart = fileNamePart.substring(0, 1).toUpperCase() + fileNamePart.substring(1);
            genClassNameBuilder.append(modFileNamePart);
        }

        // if the class name has a number, then put it to the end of Samo
        // remove all other than numbers:
        var tempClassNameNumbers = genClassNameBuilder
                .toString().replaceAll("[^\\d]", "");
        var tempClassNameNoNumbers = genClassNameBuilder.toString().replaceAll("[\\d]", "");

        // in the last step, replace any other non-alphanumeric symbols in the name
        return (tempClassNameNoNumbers + tempClassNameNumbers).replaceAll("[^0-9a-zA-Z]", "");
    }
}
