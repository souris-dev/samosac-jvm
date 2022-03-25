---
layout: default
title: Probable Statements
nav_order: 3
---

# Probable Statements
{: .no_toc }
Samosa includes a set of features to assist in seamlessly including randomization in your programs.
You can execute (or not execute) a statement based on a given probability at runtime. You can, alternatively, specify an alternate statement to execute if a statement is not executed because of the probability factor.

**Note: Variable declarations, function definitions, and return statements cannot be probabilistically executed as of now.**

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

## Specifying the probability

The `?[<int expression>]` at the end of the statement is used to specify the probability of the execution of the statement.

The `<int expression>` is an expression (evaluated at runtime) that evaluates to an int between 0 and 100. It can technically have values more than 100 or less than 0 too, but a value of more than 100 is equivalent to 100 and a value less than 0 is equivalent to 0.

### Probabilistically execute a single statement

Using the syntax: `?[<int expression>]...` at the end of a statement (after the statement end marker, `.`) would specify the chances of executing that statement.
Higher the value yielded by the expression, more the chances of the statement getting executed.

#### Example
{: .no_toc }
The probability can be a constant:
```
<samosa>
("This line is printed 35% of times.") -> putout. ?[35]...
</samosa>
```

or be something that is evaluated at runtime:

```
<samosa>
bro, prob: int = () -> putinInt.
(prob) -> putout. ?[prob + 5]...
</samosa>
```

### Probabilistically execute a statement or an alternative

You can specify an alternate statement to be executed if a statement does not get executed due to probability factors.

The syntax for the same is: `<statement>. ?[<int expression>] <statement>.`

#### Example
{: .no_toc }
```
<samosa>
("This line is printed 35% of times.") -> putout. ?[35] 
    ("And this line is printed the rest of the time.") -> putout.
</samosa>
```

### Probabilistic compound statements

Samosa also plans to support probabilistic `if` and `while` statements.

_Probabilistic compound statements are currently under development._
_There will be other features added in too to assist probabilistic execution (especially conditional probabilities)._