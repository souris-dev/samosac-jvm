<SLANG>

needs {
    java::util:: {
        Scanner,
        ArrayList
    },
    java::lang::Parser,
}

bro, initCounter: int = 10 / 2.
bro, nextCounter: int = initCounter + 3.
bro, stringVar: string = "hi".
bro, ball: string = "heh" + stringVar.

bro, someBoolVal: boolie = false || true.
bro, anotherBoolVal: boolie = someBoolVal and false || (true strictor someBoolVal).

let main(var1: int, var2: string): int {
    bro, sum: int = (1 + 3) * 6.

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

bro, res: int = (initCounter + 11 + ((initCounter, ball) -> main), ball) -> main.
(res, ball) -> main.

</SLANG>