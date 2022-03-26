---
layout: default
nav_order: 2
parent: Probable Statements
---

## Nested probable statements

Probable statements can be nested. Some examples would help understand how they work.

### Single statement, nested probabilities

In such cases, probabilities are multiplied, starting from **right to left**.

#### Example
{: .no_toc }

```
<samosa>
("Hello world!") -> putout.   ?[40]... ?[80]...
</samosa>
```

In the example above, the probability of execution of the probable statement `("Hello world!") -> putout. ?[40]...` (let's call this event _A_) is 80% (as indicated by `?[80]...` beside it.)

And further, the probability of execution of the statement `("Hello world!") -> putout.` (let's call this event _B_) is 40% _provided that the probable statement `("Hello world!") -> putout. ?[40]...` itself executes_.

Hence, the effective probability of `("Hello world!") -> putout.` executing can be expressed as `P(B|A)`.

One can go on nesting probabilities like this. But **remember, probabilities are nested from _right to left_.**

Also, as mentioned before, the probabilities specified may not necessarily be constants. They can be expressions that are evaluated to an `int` at compile time.

### Multiple statements, nested probabilities

Just like single probable statements, probable statements with alternatives can also be nested.

#### Example
{: .no_toc }

```
<samosa>

("nested3a") -> putout. ?[55] ("nested3b") -> putout. ?[70]
    ("nested2b") -> putout. ?[60]
         ("nested1b") -> putout.

</samosa>
```

Again, probabilities are nested from the _right to left_.

The above example will be easier to understand, if seen this way:
(**the snippet below is not actual code, just a pseudocode to make things easy to understand**)
```
Let A signify the statement ("nested1b") -> putout.

Then, the top level probable statement is:
B ?[60] A

where B is:
    C ?[70] ("nested2b") -> putout.

    where C is:
        ("nested3a") -> putout. ?[55] ("nested3b") -> putout.  
```

The above should have made things easier to understand :-)

In case it is still not that clear, here's an attempt at an explanation:

First, the probability `?[60]` is evaluated. Hence, 40% of the time, `("nested1b") -> putout.` is executed.
The rest 60% of the times:
* The next probability, `?[70]` is evaluated. 30% of times (after the first probability makes this statement run), `("nested2b") -> putout.` gets executed. The rest 70% of the times:
    * The next probability, `?[55]` is evaluated. So, 45% of times (after the previous nested probability statement makes this one run), the statement `("nested3b") -> putout.` is executed, while for 55% of times, the statement `("nested3a") -> putout.` gets executed.

Hopefully that served as a good explanation.

Mathematically, the probability of `("nested3a") -> putout.` getting executed can be calculated by evaluating `P(exec(C)|exec(B)|~exec(A))` (where `exec(B)` and so on depict the event of that statement executing, and `~exec(A)` depict the probability of that statement not executing.)