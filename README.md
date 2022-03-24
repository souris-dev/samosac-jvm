# samosa
#### A statically-typed JVM-based language.

Sample program (this is just an arbitrary program):
```
<SAMOSA>

\\* This is a comment *\\

\\* Variable declarations and definitions: *\\
bro, initCounter: int = 5.
bro, lol: string = "Result is: ".
bro, someBoolVal: boolie = false || true.
bro, anotherBoolVal: boolie = someBoolVal and false || (true strictor someBoolVal).

\\* Function definition *\\
action main(var1: int, var2: string): int {
    bro, sum: int = 1 + 3 * 6.

    \\* Loop *\\
    while ((var1 < 4) and (var1 > 10 + 4)) {
        sum = sum + 1.
    }

    if (var2 == "lol") {
        sum = 0.

        if (var1 > 3) {
            sum = 3 + 4.
            return sum.
        }
        else if (var1 < 2) {
            sum = 4 + 0.
        }
        else {
            return sum.
        }
    }
    else if (var2 == "big lol") {
        sum = 1.
        return 5.
    }
    else {
        sum = 10.
    }
    
    return sum.
}

\\* Function call expression *\\
bro, res: int = (initCounter + 3 + ((initCounter, lol) -> main), lol) -> main.

\\* Function call statement *\\
(res, lol) -> main.

</SAMOSA>
```