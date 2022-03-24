<h1 align="center">Samosa</h1>
<p align="left">
  <center><i>Welcome, samosa lovers!</i><br><br></center>
  <b>Samosa is a programming language written in Java and Kotlin, that runs on the JVM.</b>
<br><br><i>Note: This programming language, "samosa", is named after an Indian snack called "samosa", and is pronounced as "some-o-saah" (the part "saah" is pronounced like the word "sour", but without the "r").</i>
<br>

<h2 align="center">Installation</h2>
There are some alternatives for installing samosa. <i>This section will be updated soon with the other alternatives.</i>
<h3>Building from source</h3>
<b>Note: Ensure that you have > JDK 12 installed (the project was developed on JDK 17).</b> 
<br><br>For now, you can download the source and build it using maven.
Easier installation methods will be provided soon.

<h2 align="center">Usage</h2>
<b>Note: Ensure that you have the JRE installed before starting this section.</b><br>

<h3>Compilation</h3>
Type your samosa program in a file, and name it something (for example samosa.samo).
Then use the .jar file of the compiler to compile it <b>(ensure that you have java in you PATH)</b>:

```
java -jar samosac-jvm.jar samosa.samo
```

(Replace `samosac-jvm.jar` with the name of the compiler jar file, and `samosa.samo` with the name of the file you wrote your program in.)

<br><i>This section will be updated.</i>

<h3>Running the program</h3>

As samosa compiles to JVM bytecode, a `.class` is generated, named as per your filename.
So for the above example, a file named `SamosaSamo.class` would be created in the `./out` directory.
<br><br>To run it, do this <b>(ensure that you have java in your PATH)</b>:

```
cd out
java SamosaSamo
```

<h2 align="center">Syntax</h2>

As samosa is still in its first release, it has limited features. More features will be added soon in upcoming releases.
<br>

<h3>Starting a program</h3>
Any samosa program must start with `<samosa>` and end with `</samosa>`.
<br>Note that statements in samosa end with a period (`.`).
For example:

```
<samosa>
("Hello World!") -> putout.
</samosa>
```
(Note: in the example above, we are making a function call to `putout`, a built-in function. That line is equivalent to `System.out.println("Hello World!")` in Java.)

<h3>Comments</h3>
Comments in samosa can span multiple lines. They start with `/*` and end with `*/`.
Example:

```
<samosa>
/* This line is a comment and will not be executed. */
("This line is executed.") -> putout.
</samosa>
```

<h3>Variables</h3>
Variables are declared with the keyword `bro, ` (yes, the comma is necessary :-)).
Currently, variables can be only of three types: `int` (for integers), `string` (for strings), or `boolie` (for boolean values).

Some examples of declaration and initialization:

```
<samosa>
bro, i: int = 0.
bro, str: string = "hello!".
bro, aBoolVal: boolie = true.

/* Variables can also just be declared. */
bro, j: int.
bro, str2: string.
bro, boolieVal: boolie.
</samosa>
```

If a variable is only declared, the variable is assigned the default value for that type:
1. for `int`, the default value is 57005 (in hex, `0xDEAD`).
2. for `string`, the default value is `lawl`.
3. for `boolie`, the default value is `true`.

If you're initializing a variable at the same time when you are declaring it, you can skip writing its type:

```
<samosa>
/* Types will be inferred for these: */
bro, i = 0.
bro, str = "string".
bro, aBoolVal = true.
</samosa>
```

<h3>Expressions</h3>

Expressions in samosa work in pretty much the same way as in Java/C++ or many other languages.
<p>Integer literals allow digits from 0 to 9. Hex or octal number systems are not yet supported.
If your result is a floating point number, it will be converted to an int
by taking off the part after the decimal point.</p>

<p>String literals start and end with double quotes. You can use the <code>+</code> operator for string concatenation.</p>
  Some example expressions:

```
<samosa>
bro, str = "string" + "literal".

bro, anIntVal = 3 + 4 / 4.

/* Boolean expressions: */
bro, aBoolVal = true or false.
bro, anotherBoolVal = anIntVal > 10.
bro, someBoolVal = anIntVal == 10 and anotherBoolVal.
bro, boolval = anIntVal != 100.
</samosa>
```

For boolean expressions, any of `true`/`True`/`yes`/`TRUE` can be used for a truthy value.
<br>For a falsy value, any of `false`/`False`/`nope`/`FALSE` can be used. 

In boolean expressions:
<ul>
<li><code>||</code> or the keyword <code>or</code> stands for a <b>logical OR (not short-circuited)</b></li>
<li><code>&&</code> or the keyword<code>and</code> stands for a <b>logical AND (not short-circuited)</b></li>
<li><code>||!</code> or the keyword<code>strictor</code> stands for a <b>logical XOR</b></li>
<li><code>!!</code> or the keyword<code>not</code> stands for a <b>logical NOT</b></li>
</ul>

<h3>Conditional statements</h3>
Samosa supports `if` statements (and if-else if-else ladders). The syntax for `if` statements in samosa is similar to that found in many other languages:
An example:

```
<samosa>
bro, i = 9.

if (i == 9) {
    ("i is 9") -> putout.
}
else if (i == 10) {
    ("i is 10") -> putout.
}
else if (i == 11) {
    ("i is 11") -> putout.
}
else {
    ("I dunno, I just like samosa.") -> putout.
}
</samosa>
```

<b>Disclaimer: The example above is just for demonstration purposes. Please do not use such lame conditional statements. Thanks.</b>

<h3>Loops</h3>

Samosa currently supports only one kind of loops: `while` loops. It works in a similar way as in other languages:
<br>The following example prints the numbers 3, 2, 1 sequentially on three lines. 
```
<samosa>

bro, i: int = 3.

while (i > 0) {
    (i) -> putout.
    i = i - 1.
}

</samosa>
```

Other kinds of loops will also be added in subsequent releases.

<h3>Functions</h3>

Yep, samosa also supports functions!
<br>(<i>Samosa does not yet support first-class functions though, but support for the same is planned.</i>)
<br><br>

<h4>Defining a function</h4>
A function in samosa is defined using the keyword `let`.
A function may declare some formal parameters, and can either return no value or return a value of a supported type (varargs are not yet supported).

Some examples:

```
<samosa>

let function1(var1: int, var2: string): void {
    /* do something here */
}

let function2(var1: int): int {
    return var1 + 3.
}

/* If your function returns nothing, you need not specify a return type. */
let function3(var1: int) {
    /* do something */
}

</samosa>
```

<h4>Calling a function</h4>

A function can be called as a standalone statement or within an expression, like in many languages.
The syntax for the same is: `(<arguments>) -> <function name>`, where `<function name>` is the name of the function to be called
and `<arguments>` is the list of passed arguments to the function, separated by commas.<br>
<br><b>Note: This syntax is will probably be changed as it sometimes causes readability issues.</b>
<br>
<br>An extended example of the program above would demonstrate this:

```
<samosa>

let function1(var1: int, var2: string): void {
    /* do something here */
}

let function2(var1: int): int {
    return var1 + 3.
}

let function3(var1: int) {
    /* do something */
}

(10) -> function3.

bro, m = 7.
bro, i: int = 3 + (5 + m) -> function2.

</samosa>
```

<b>Note: To call a function, it must be defined before the point where it is being called. So, the following program will not work:</b>

```

let function1(var1: int, var2: string): void {
    /* do something here */
}

let function2(var1: int): int {
    return var1 + 3.
}

bro, m = 7.

/* The next line works as function2 is defined earlier. */
bro, i: int = 3 + (5 + m) -> function2.

/* The next line does not work as function3 is defined later. */
(10) -> function3.

let function3(var1: int) {
    /* do something */
}

```

Recursion is supported, but the compiler does not currently perform tail-call optimization (support is planned for later releases).
Function overloading is not currently supported for user defined functions (but is supported for builtin functions).

<h4>Builtin functions</h4>

Samosa has a few builtin functions (more will be added soon, in addition to a small standard library).
Some builtin functions have overloads.

1. `putout`

    This function takes a single argument and prints it to stdout, and prints a newline after it. It returns nothing.
    <br>The argument can be a `string`, `int` or a `boolie` (three overloads).
    <br>Example:
    
    ```
    <samosa>
    
    bro, i: int = 0.
    bro, str: string = "hello ".
    bro, boolVal: boolie = "boolieVal".
    
    (i) -> putout.
    (str) -> putout.
    (boolVal) -> putout.
    
    </samosa>
    ```

2. `putinInt`

    Takes in an `int` as user input (from stdin). Example: 
    
    ```
    <samosa>
    bro, i = () -> putinInt.
    </samosa>
    ```

3. `putinBoolie`

    Similar to `putinInt` but inputs a boolean value.<br><br>

4. `putinString`

    Similar to `putinInt` but inputs a string value.<br><br>

5. `stoi`
    
    Converts a `string` to an `int`. Takes a `string` as argument. Will throw an exception if the number is of the wrong format.<br><br>

6. `itos`

   Converts an `int` to a `string`. Takes an `int` as argument.<br><br>


<i>This section will be updated as new builtin functions are added.</i>