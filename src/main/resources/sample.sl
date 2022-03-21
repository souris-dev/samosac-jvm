<SLANG>

/* Imports these classes */

needs {
    java::util:: {
        Scanner,
        ArrayList
    },
    java::lang::Parser,
}

/* This is a comment */

/* Variable declarations and definitions: */
/* note that "bro," or "sis," or the words "def" or "var" can be used for this */
bro, initCounter: int = 10 / 2.
bro, nextCounter: int = initCounter + 3.
bro, stringVar: string = "hi".
bro, ball: string = "heh" + stringVar.

bro, someBoolVal: boolie = false || true.
bro, anotherBoolVal: boolie = someBoolVal and false || (true strictor someBoolVal).
bro, somethingA = 3.
bro, somethingB = 2.
bro, relOpValue: boolie = someBoolVal and somethingA > somethingB.

ball = "myGawd" + stringVar.
relOpValue = nextCounter > nextCounter.
someBoolVal = anotherBoolVal.

if (someBoolVal) {
    ball = "ball1".
}
else if (relOpValue) {
    ball = "ball2".
}
else if (somethingA > somethingB) {
    ball = "ball3".
}
else {
    ball = "myGawd" + "lawl".
}

while (somethingA < 5) {
    if (relOpValue) {
        yamete_kudasai.
    }

    somethingA = somethingA + 1.
}

/* Can we use "bhai," or "behen," too :-)) */

/* TODO: add support for multiple declarations/definitions
 * like: bro, a: int, b: int, c: string = "Hello", d: int = 3.
 * and for initializing many items together:
 * sis, (a: int, b: int, c: int) = 0, d: int = 3. */

/* Function definition */
let mana (var1: int, var2: string): int {
    bro, sum: int = (1 + 3) * 6.
    sum = 0.

    /* Loop */
    while ((var1 < 4) and (var1 > 10 + 4)) {
        sum = sum + 1.
        bro, g: int = 19.

        if (sum > 10) {
            yamete_kudasai.
        }

        if (sum > 3) {
            thanku_next.
        }

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
        sum = 17.
    }

    return sum.
}

/* Function call expression */
bro, res: int = (initCounter + 11 + ((initCounter, ball) -> mana), ball) -> mana.

/* Function call statement */
(res, ball) -> mana.

</SLANG>