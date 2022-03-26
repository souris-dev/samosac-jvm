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
```
<samosa>
("Hello world!") -> putout.   ?[40]... ?[80]...
</samosa>
```

In the example above, the probability of execution of the probable statement `("Hello world!") -> putout. ?[40]...` (let's call this event _A_) is 80% (as indicated by `?[80]...` beside it.)

And further, the probability of execution of the statement `("Hello world!") -> putout.` (let's called this event _B_) is 40% _provided that the probable statement `("Hello world!") -> putout. ?[40]...` itself executes_.

Hence, the effective probability of `("Hello world!") -> putout.` executing can be expressed as _P(B|A)_.

One can go on nesting probabilities like this. But **remember, probabilities are evaluated from _right to left_.**

### Multiple statements, nested probabilities

Just like the nesting single statements, probable statements with alternatives can also be nested.

#### Example

```
<samosa>

("nested3a") -> putout. ?[55] ("nested3b") -> putout. ?[70]
    ("nested2b") -> putout. ?[60]
         ("nested1b") -> putout.

</samosa>
```

Again, evaluate probabilities from the right to left.

The above example will be easier to understand, if seen this way:
(**the snippet below is not actual code, just a pseudocode to make things easy to understand**)
```
A ?[60] ("nested1b") -> putout.
where A is:
    B ?[70] ("nested2b") -> putout.
    where B is:
        ("nested3a") -> putout. ?[55] ("nested3b") -> putout.  
```

Hope that made things easier to understand :-)

In case it is still not that clear, here's an attempt at an explanation:

Firstly, the probability `?[60]` is evaluated. Hence, 40% of the time, `("nested1b") -> putout.` is executed.
The rest 60% of the times:
* The next probability, `?[70]` is evaluated. 30% of times (after the first probability makes this statement run), `("nested2b") -> putout.` gets executed. The rest 70% of the times:
    * The next probability, `?[55]` is evaluated. So, 45% of times (after the previous nested probability statement makes this one run), the statement `("nested3b") -> putout.` is executed, while for 55% of times, the statement `("nested3a") -> putout.` gets executed.

Hopefully, that served as a good explanation.