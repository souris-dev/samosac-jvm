---
layout: default
title: Builtin Functions
---

# Builtin Functions

Samosa has a few builtin functions (more will be added soon, in addition to a small standard library). Some builtin functions have overloads.

* `putout(expression)`

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

* `putinInt(): int`

Takes in an `int` as user input (from stdin). Example:

```
<samosa>
bro, i = () -> putinInt.
</samosa>
```

* `putinBoolie(): boolie`

Similar to `putinInt` but inputs a boolean value.

* `putinString(): string`

Similar to `putinInt` but inputs a string value.

* `stoi(stringexpr): int`

Converts a `string` to an `int`. Takes a `string` as argument. Will throw an exception if the number is of the wrong format.

* `itos(intexpr): string`

Converts an `int` to a `string`. Takes an `int` as argument.

* `exit(intexpr)`

Exits and stops the program. Takes an integer argument as an exit code.

_This section will be updated as new builtin functions are added._