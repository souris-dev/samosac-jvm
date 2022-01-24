## TODO:

1. Add custom error handling. (Specifically, add an override of visitErrorNode in FunctionControlPathAnalyzer 
and something similar in StaticChecker).
2. Improve logging format (something like gcc would be good).

## Features that may be added:

1. An asap operator for functions that will call the function as soon as possible.
This function can also be called later on too of course.
For example:
    ```
    action asap doThis() {
        // do something here
    }
    ```
    The above is equivalent to doing this:
    
    ```
    action doThis() {
        // do something here
    }
    () -> doThis.
    ```


2. Add null check operator.
3. Add floating point numbers.
4. Add hexadecimal numbers.
5. Add a for loop (and other kinds of loops).
6. Add break and continue statements.
7. Add the conditional ternary operator.
8. Add something like a switch statement.
9. Exception handling?
10. Lambdas would be so good.
11. Import statements. Freaking required.
12. Deferred blocks would be quite cool. Shouldn't be too tough to implement too.
13. Escaped characters in strings.