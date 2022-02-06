<SLANG>

/* This is a comment */

/* Variable declarations and definitions: */
/* note that "bro," or "sis," or the words "def" or "var" can be used for this */
bro, initCounter: int = 10 / 2.
bro, nextCounter: int = initCounter + 3.
bro, lol: string = "Result is: ".
bro, someBoolVal: boolie = false || true.
bro, anotherBoolVal: boolie = someBoolVal and false || (true strictor someBoolVal).

/* Can we use "bhai," or "behen," too :-)) */

/* TODO: add support for multiple declarations/definitions
 * like: bro, a: int, b: int, c: string = "Hello", d: int = 3.
 * and for initializing many items together:
 * sis, (a: int, b: int, c: int) = 0, d: int = 3. */

/* Function definition */
action main(var1: int, var2: string): int {
    bro, sum: int = 1 + 3 * 6.

    /* Loop */
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

/* Function call expression */
bro, res: int = (initCounter + 3 + ((initCounter, lol) -> main), lol) -> main.

/* Function call statement */
(res, lol) -> main.

</SLANG>