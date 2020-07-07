package mango.interpreter.symbols

object BuiltinFunctions {

    val print = FunctionSymbol(
        "print",
        arrayOf(VariableSymbol.param("text", TypeSymbol.String)),
        TypeSymbol.Unit, "mango.io.print", null, FunctionSymbol.MetaData().apply { isExtern = true })

    val println = FunctionSymbol(
        "println",
        arrayOf(VariableSymbol.param("text", TypeSymbol.String)),
        TypeSymbol.Unit, "mango.io.println", null, FunctionSymbol.MetaData().apply { isExtern = true })

    val readln = FunctionSymbol(
        "readln",
        arrayOf(),
        TypeSymbol.String, "mango.io.readln", null, FunctionSymbol.MetaData().apply { isExtern = true })

    val typeOf = FunctionSymbol(
        "typeof",
        arrayOf(VariableSymbol.param("object", TypeSymbol.Any)),
        TypeSymbol.String,"mango.util.typeof", null, FunctionSymbol.MetaData().apply { isExtern = true })

    fun getAll() = listOf(print, println, readln, typeOf)
}