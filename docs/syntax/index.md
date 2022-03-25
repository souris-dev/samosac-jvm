---
layout: default
title: Syntax
---

## Syntax
{: .no_toc }

As samosa is still in its first release, it has limited features. More features will be added soon in upcoming releases.

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

### Starting a program

Any samosa program must start with `<samosa>` and end with `</samosa>`.
Note that statements in samosa end with a period (`.`). For example:

```
<samosa>
("Hello World!") -> putout.
</samosa>
```

(Note: in the example above, we are making a function call to `putout`, a built-in function. That line is equivalent to `System.out.println("Hello World!")` in Java.)

### Comments

Comments in samosa can span multiple lines. They start with `/*` and end with `*/`.

Example:

```
<samosa>
/* This line is a comment and will not be executed. */
("This line is executed.") -> putout.
</samosa>
```

### Variables

Variables are declared with the keyword `bro,`  (yes, the comma is necessary :-)). Currently, variables can be only of three types: `int` (for integers), `string` (for strings), or `boolie` (for boolean values). Some examples of declaration and initialization:

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

*   for `int`, the default value is 57005 (in hex, `0xDEAD`).
*   for `string`, the default value is `lawl`
*   for `boolie`, the default value is `true`.

If you're initializing a variable at the same time when you are declaring it, you can skip writing its type:

```
<samosa>

/* Types will be inferred for these: */
bro, i = 0.
bro, str = "string".
bro, aBoolVal = true.

</samosa>
```

### Expressions

Expressions in samosa work in pretty much the same way as in Java/C++ or many other languages.

Integer literals allow digits from 0 to 9\. Hex or octal number systems are not yet supported. If your result is a floating point number, it will be converted to an int by taking off the part after the decimal point.

String literals start and end with double quotes. You can use the `+` operator for string concatenation.

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

For boolean expressions, any of `true`, `True`, `yes`, `TRUE` can be used for a truthy value.
For a falsy value, any of `false`, `False`, `nope`, `FALSE` can be used. In boolean expressions:

*   `||` or the keyword `or` stands for a **logical OR (not short-circuited)**
*   `&&` or the keyword`and` stands for a **logical AND (not short-circuited)**
*   `||!` or the keyword`strictor` stands for a **logical XOR**
*   `!!` or the keyword`not` stands for a **logical NOT**

### Conditional statements

Samosa supports `if` statements (and if-else if-else ladders). The syntax for `if` statements in samosa is similar to that found in many other languages.
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

**Disclaimer: The example above is just for demonstration purposes. Please do not use such lame conditional statements. Thanks.**

### Loops

Samosa currently supports only one kind of loops: `while` loops. It works in a similar way as in other languages:
The following example prints the numbers 3, 2, 1 sequentially on three lines.

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

### Functions

Yep, samosa also supports functions!

(_Samosa does not yet support first-class functions though, but support for the same is planned._)

#### Defining a function

A function in samosa is defined using the keyword `let`. A function may declare some formal parameters, and can either return no value or return a value of a supported type (varargs are not yet supported). Some examples:

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

#### Calling a function

A function can be called as a standalone statement or within an expression, like in many languages. The syntax for the same is: `(<arguments>) -> <function name>`, where `<function name>` is the name of the function to be called and `<arguments>` is the list of passed arguments to the function, separated by commas.

**Note: This syntax is will probably be changed as it sometimes causes readability issues.**

An extended example of the program above would demonstrate this:

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

**Note: To call a function, it must be defined before the point where it is being called. So, the following program will not work:**

```
<samosa>

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

</samosa>
```

Recursion is supported, but the compiler does not currently perform tail-call optimization (support is planned for later releases). Function overloading is not currently supported for user defined functions (but is supported for builtin functions).
