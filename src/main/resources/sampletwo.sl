<SLANG>

needs {
    java::lang::System
}

let khana(stringArg: string) {
    (stringArg) -> putout.
}

let mana(var1: int, var2: string): int {
    if (var1 == 0) {
        return 0.
    }
    (var1) -> putout.
    (var2) -> khana.
    return 0.
}

bro, num: int = () -> putinInt.
bro, msg: string = () -> putinString.
bro, result: int = 50 + (num, msg) -> mana.
(result) -> putout.
("Hello world!") -> putout.
</SLANG>