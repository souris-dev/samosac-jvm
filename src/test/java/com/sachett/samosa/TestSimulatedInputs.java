package com.sachett.samosa;

import com.sachett.samosa.samosac.compiler.CompilerKt;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSimulatedInputs {
    static final File programsDir = new File("src/test/data/simulated-input-run/test-programs");
    static final File programsCompOutputDir = new File("src/test/data/simulated-input-run/test-programs-comp-outputs");
    static final File programsCompErrorDir = new File("src/test/data/simulated-input-run/test-programs-comp-errors");
    static final File programsRunOutputDir = new File("src/test/data/simulated-input-run/test-programs-run-outputs");
    static final File programsRunErrorDir = new File("src/test/data/simulated-input-run/test-programs-run-errors");

    static final File expectedRunOutputDir = new File("src/test/data/simulated-input-run/expected-run-outputs");
    static final File expectedRunErrorDir = new File("src/test/data/simulated-input-run/expected-run-errors");
    static final File classFileOutDir = new File("src/test/data/simulated-input-run/out");
    static final File inputsDir = new File("src/test/data/simulated-input-run/inputs");

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
                        File inputFile = new File(
                                inputsDir.getPath() + File.separator + file.getName() + ".inputs"
                        );

                        runOutputFile.createNewFile();
                        runErrorFile.createNewFile();

                        String classFileName = TestPositiveCompileRun.getClassFileNameFromFileName(file.getName());

                        ProcessBuilder runProcessBuilder = new ProcessBuilder("java", classFileName);
                        classFileOutDir.mkdirs();
                        runProcessBuilder.directory(classFileOutDir);
                        runProcessBuilder.redirectError(runErrorFile);

                        FileWriter runOutputFileStream = new FileWriter(runOutputFile);
                        BufferedReader inputsFileStream = new BufferedReader(new FileReader(inputFile));

                        Process runProcess = runProcessBuilder.start();

                        InputStream runProcessStdout = runProcess.getInputStream();
                        OutputStream runProcessStdin = runProcess.getOutputStream();

                        BufferedReader outReader = new BufferedReader(new InputStreamReader(runProcessStdout));
                        BufferedWriter inputWriter = new BufferedWriter(new OutputStreamWriter(runProcessStdin));

                        Thread resultOutStreamPoll = new Thread(() -> {
                            try {
                                int outChar;
                                StringBuilder stringBuilder = new StringBuilder();

                                while (!Thread.interrupted() && (outChar = outReader.read()) != -1 && runProcess.isAlive()) {
                                    char outputChar = (char) outChar;
                                    runOutputFileStream.write(outputChar);
                                    stringBuilder.append(outputChar);

                                    String lastWord = stringBuilder.toString();

                                    if (lastWord.contains("<inp>")) {
                                        // Take the next input line from the file and feed it into the process
                                        String inputLine = inputsFileStream.readLine();
                                        inputWriter.write(inputLine);
                                        inputWriter.newLine();
                                        inputWriter.flush();

                                        // clear buffer
                                        stringBuilder.setLength(0);
                                    }

                                    if (lastWord.endsWith(" ")
                                            || lastWord.endsWith("\n")
                                            || lastWord.endsWith("\t")
                                    ) {
                                        // clear buffer if we're onto a new word
                                        stringBuilder.setLength(0);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        resultOutStreamPoll.start();
                        resultOutStreamPoll.join(4000);
                        runOutputFileStream.close();

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
}
