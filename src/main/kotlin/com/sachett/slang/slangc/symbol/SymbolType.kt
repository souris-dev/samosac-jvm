package com.sachett.slang.slangc.symbol

enum class SymbolType(
    val asString: String,
    val canBeUsedWithRelOp: Boolean,
    val canBeUsedWithCompOp: Boolean,
    val defaultValue: Any? = null
) {
    FUNCTION("function", false, false),
    INT("int", true, true, 0xDEAD),
    STRING("string", false, true, "lawl"),
    BOOL("boolie", false, true, true),
    VOID("void", false, false),
    UNSUPPORTED("thing", false, false, null)
}