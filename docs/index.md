<h1 align="center">Samosa</h1>
<p align="center"><i>Welcome, samosa lovers!</i><br><br></p>
<p align="left">
  <b>Samosa is a programming language written in Java and Kotlin, that runs on the JVM.</b>
<br><br><i>Note: This programming language, "samosa", is named after an Indian snack called "samosa", and is pronounced as "some-o-saa" (the part "saa" is pronounced like the word "sour", but without the "r").</i>
<br>

<h2 align="center">Installation</h2>
There are some alternatives for installing samosa. <i>This section will be updated soon with the other alternatives.</i>
<h3>Building from source</h3>
<b>Note: Ensure that you have the following installed (and in your PATH) before using the upcoming commands to build from source:</b>
<ul>
<li><b>git</b></li>
<li><b>&ge; JDK 11 (the project was developed on JDK 17, but the code is compatible with java version >= 11.)</b></li>
<li><b>Apache Maven 3.1 or higher version</b></li>
</ul>
<br><br>To download the source and build it using maven, run these in the terminal of your choice:

<br><pre><code>git clone https://github.com/souris-dev/samosac-jvm.git
cd samosac-jvm
mvn compile
</code></pre>

Then, to build the compiler jar, use (from within the project directory):
<br><pre><code>mvn package</code></pre>

<br>This will create a <code>samosac-1.0-full.jar</code> in the <code>target</code> folder. This is the compiler jar.
<br><i>Easier installation methods will be provided soon.</i>

<h2 align="center">Usage</h2>
<b>Note: Ensure that you have the JRE (minimum java version 11) installed before starting this section.</b><br>

<h3>Compilation</h3>
Type your samosa program in a file, and name it something (for example samosa.samo).
Then use the .jar file of the compiler to compile it <b>(ensure that you have java in you PATH)</b>:<br>
<br>
<pre><code>java -jar samosac-1.0-full.jar samosa.samo
</code></pre>

(Replace <code>samosac-1.0-full.jar</code> with the full path to the compiler jar file, and <code>samosa.samo</code> with the name of the file you wrote your program in.)

<br><i>This section will be updated.</i>

<h3>Running the program</h3>

As samosa compiles to JVM bytecode, a <code>.class</code> is generated, named as per your filename.
So for the above example, a file named <code>SamosaSamo.class</code> would be created in the <code>.&#47;out</code> directory.
<br><br>To run it, do this <b>(ensure that you have java in your PATH)</b>:
<br><br>
<pre><code>cd out
java SamosaSamo</code>
</pre>

<h2 align="center">Syntax</h2>

As samosa is still in its first release, it has limited features. More features will be added soon in upcoming releases.
<br>

<h3>Starting a program</h3>
Any samosa program must start with <code>&lt;samosa&gt;</code> and end with <code>&lt;&#47;samosa&gt;</code>.
<br>Note that statements in samosa end with a period (<code>.</code>).
For example:

<br><br><pre><code>&lt;samosa&gt;
("Hello World!") -> putout.
&lt;&#47;samosa&gt;
</code></pre>
(Note: in the example above, we are making a function call to <code>putout</code>, a built-in function. That line is equivalent to <code>System.out.println("Hello World!")</code> in Java.)

<h3>Comments</h3>
Comments in samosa can span multiple lines. They start with <code>&#47;&#42;</code> and end with <code>&#42;/</code>.
Example:

<br><br><pre><code>&lt;samosa&gt;
/* This line is a comment and will not be executed. */
("This line is executed.") -> putout.
&lt;&#47;samosa&gt;</code></pre>

<h3>Variables</h3>
Variables are declared with the keyword <code>bro,</code> &nbsp;(yes, the comma is necessary :-)).
Currently, variables can be only of three types: <code>int</code> (for integers), <code>string</code> (for strings), or <code>boolie</code> (for boolean values).

Some examples of declaration and initialization:

<br><br><pre><code>&lt;samosa&gt;
bro, i: int = 0.
bro, str: string = "hello!".
bro, aBoolVal: boolie = true.

/* Variables can also just be declared. */
bro, j: int.
bro, str2: string.
bro, boolieVal: boolie.
&lt;&#47;samosa&gt;</code></pre>

If a variable is only declared, the variable is assigned the default value for that type:
1. for <code>int</code>, the default value is 57005 (in hex, <code>0xDEAD</code>).
2. for <code>string</code>, the default value is </code>lawl</code>.
3. for <code>boolie</code>, the default value is <code>true</code>.

If you're initializing a variable at the same time when you are declaring it, you can skip writing its type:

<br><br><pre><code>&lt;samosa&gt;
/* Types will be inferred for these: */
bro, i = 0.
bro, str = "string".
bro, aBoolVal = true.
&lt;&#47;samosa&gt;</code></pre>

<h3>Expressions</h3>

Expressions in samosa work in pretty much the same way as in Java/C++ or many other languages.
<p>Integer literals allow digits from 0 to 9. Hex or octal number systems are not yet supported.
If your result is a floating point number, it will be converted to an int
by taking off the part after the decimal point.</p>

<p>String literals start and end with double quotes. You can use the <code>+</code> operator for string concatenation.</p>
Some example expressions:

<br><br><pre><code>&lt;samosa&gt;
bro, str = "string" + "literal".

bro, anIntVal = 3 + 4 / 4.

/* Boolean expressions: */
bro, aBoolVal = true or false.
bro, anotherBoolVal = anIntVal > 10.
bro, someBoolVal = anIntVal == 10 and anotherBoolVal.
bro, boolval = anIntVal != 100.
&lt;&#47;samosa&gt;</code></pre>

For boolean expressions, any of <code>true</code>, <code>True</code>, <code>yes</code>, <code>TRUE</code> can be used for a truthy value.
<br>For a falsy value, any of <code>false</code>, <code>False</code>, <code>nope</code>, <code>FALSE</code> can be used.

In boolean expressions:
<ul>
<li><code>||</code> or the keyword <code>or</code> stands for a <b>logical OR (not short-circuited)</b></li>
<li><code>&&</code> or the keyword<code>and</code> stands for a <b>logical AND (not short-circuited)</b></li>
<li><code>||!</code> or the keyword<code>strictor</code> stands for a <b>logical XOR</b></li>
<li><code>!!</code> or the keyword<code>not</code> stands for a <b>logical NOT</b></li>
</ul>

<h3>Conditional statements</h3>
Samosa supports <code>if</code> statements (and if-else if-else ladders). The syntax for <code>if</code> statements in samosa is similar to that found in many other languages. <br />
An example:

<br><br><pre><code>&lt;samosa&gt;
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
&lt;&#47;samosa&gt;</code></pre>

<b>Disclaimer: The example above is just for demonstration purposes. Please do not use such lame conditional statements. Thanks.</b>

<h3>Loops</h3>

Samosa currently supports only one kind of loops: <code>while</code> loops. It works in a similar way as in other languages:
<br>The following example prints the numbers 3, 2, 1 sequentially on three lines.

<br><br><pre><code>&lt;samosa&gt;

bro, i: int = 3.

while (i > 0) {
    (i) -> putout.
    i = i - 1.
}

&lt;&#47;samosa&gt;</code></pre>

Other kinds of loops will also be added in subsequent releases.

<h3>Functions</h3>

Yep, samosa also supports functions!
<br>(<i>Samosa does not yet support first-class functions though, but support for the same is planned.</i>)
<br><br>

<h4>Defining a function</h4>
A function in samosa is defined using the keyword <code>let</code>.
A function may declare some formal parameters, and can either return no value or return a value of a supported type (varargs are not yet supported).

Some examples:
<br><br><pre><code>&lt;samosa&gt;

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

&lt;&#47;samosa&gt;</code></pre>

<h4>Calling a function</h4>

A function can be called as a standalone statement or within an expression, like in many languages.
The syntax for the same is: <code>(&lt;arguments&gt;) -> &lt;function name&gt;</code>, where <code>&lt;function name&gt;</code> is the name of the function to be called
and <code>&lt;arguments&gt;</code> is the list of passed arguments to the function, separated by commas.<br>
<br><b>Note: This syntax is will probably be changed as it sometimes causes readability issues.</b>
<br>
<br>An extended example of the program above would demonstrate this:

<br><br><pre><code>&lt;samosa&gt;

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

&lt;&#47;samosa&gt;</code></pre>

<b>Note: To call a function, it must be defined before the point where it is being called. So, the following program will not work:</b>

<br><br><pre><code>&lt;samosa&gt;

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

&lt;&#47;samosa&gt;</code></pre>

Recursion is supported, but the compiler does not currently perform tail-call optimization (support is planned for later releases).
Function overloading is not currently supported for user defined functions (but is supported for builtin functions).

<h4>Builtin functions</h4>

Samosa has a few builtin functions (more will be added soon, in addition to a small standard library).
Some builtin functions have overloads.<br><br>

<ul>
<li><code>putout(expression)</code>
<br>
   This function takes a single argument and prints it to stdout, and prints a newline after it. It returns nothing.
   <br>The argument can be a <code>string</code>, <code>int</code> or a <code>boolie</code> (three overloads).
   <br>Example:

<br><br><pre><code>&lt;samosa&gt;

bro, i: int = 0.
bro, str: string = "hello ".
bro, boolVal: boolie = "boolieVal".

(i) -> putout.
(str) -> putout.
(boolVal) -> putout.

&lt;&#47;samosa&gt;</code></pre>
</li>
<li><code>putinInt(): int</code>
<br>
   Takes in an <code>int</code> as user input (from stdin). Example:

<br><br><pre><code>&lt;samosa&gt;
bro, i = () -> putinInt.
&lt;&#47;samosa&gt;</code></pre>
</li>

<li><code>putinBoolie(): boolie</code>
<br>
   Similar to <code>putinInt</code> but inputs a boolean value.<br><br>
</li>

<li><code>putinString(): string</code>
<br>
   Similar to <code>putinInt</code> but inputs a string value.<br><br>
</li>
<li><code>stoi(stringexpr): int</code>
<br>
   Converts a <code>string</code> to an <code>int</code>. Takes a <code>string</code> as argument. Will throw an exception if the number is of the wrong format.<br><br>
</li>
<li><code>itos(intexpr): string</code>
<br>
   Converts an <code>int</code> to a <code>string</code>. Takes an <code>int</code> as argument.<br><br>

<li><code>exit(intexpr)</code><br>
    Exits and stops the program. Takes an integer argument as an exit code.
</li>
</ul>
<i>This section will be updated as new builtin functions are added.</i>