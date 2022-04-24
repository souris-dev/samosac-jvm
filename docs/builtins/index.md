---
layout: default
title: Builtin Functions
nav_order: 4
---

# Builtin Functions
{: .no_toc }

Samosa has a few builtin functions (more will be added soon, in addition to a small standard library). Some builtin functions have overloads.

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

### `(expression) -> putout`

This function takes a single argument and prints it to stdout, and prints a newline after it. It returns nothing.
The argument can be a `string`, `int` or a `boolie` (three overloads).
Example:

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

### `() -> putinInt: int`

Takes in an `int` as user input (from stdin). Example:

```
<samosa>
bro, anInt = () -> putinInt.

/* or: */

bro, anInt: int = () -> putinInt.
</samosa>
```

### `() -> putinBoolie: boolie`

Similar to `putinInt` but inputs a boolean value.

```
<samosa>
bro, aBoolie = () -> putinBoolie.

/* or: */

bro, aBoolie: boolie = () -> putinBoolie.
</samosa>
```

### `() -> putinString: string`

Similar to `putinInt` but inputs a string value.

```
<samosa>
bro, aString = () -> putinString.

/* or: */

bro, aString: string = () -> putinString.
</samosa>
```

### `(stringexpr) -> stoi: int`

Converts a `string` to an `int`. Takes a `string` as argument. Will throw an exception if the number is of the wrong format.

### `(intexpr) -> itos: string`

Converts an `int` to a `string`. Takes an `int` as argument.

### `(intexpr) -> exit`

Exits and stops the program. Takes an integer argument as an exit code.

_This section will be updated as new builtin functions are added._
