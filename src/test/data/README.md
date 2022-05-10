## Adding tests

The directories are divided into the following:

1. *positive-compile-run*: Programs that are supposed to compile successfully and run either successfully or with an error.
2. *negative-compile*: Programs that are not supposed to compile successfully.

### Adding a positive compile and run test case

1. Write the test program. Put it into *positive-compile-run/test-programs* (ensure that the filename ends with `.samo`). Let us call the filename for this program `<filename>` (note that `<filename>` ends with *.samo*).
2. Make a file named as `<filename>.run.log.should` in *positive-compile-run/test-programs/expected-run-outputs*. In this file, put the expected output of the program.
3. Make a file named as `<filename>.run.err.should` in *positive-compile-run/test-programs/expected-run-errors*. In this file, put the expected error output of the program.

In steps 2 and 3, if there is no expected output and/or error, the files can be left blank. (The files still do need to be created.)

### Adding a negative compile test case

1. Write the test program. Put it into *negative-compile/test-programs* (ensure that the filename ends with `.samo`). Let us call the filename for this program `<filename>` (note that `<filename>` ends with *.samo*).
2. Make a file named as `<filename>.compile.err.should` in *negative-compile/test-programs/expected-compile-errors*. In this file, put the expected compilation output of the program.

In step 2, if there is no expected output and/or error, the files can be left blank. (The files still do need to be created.)
