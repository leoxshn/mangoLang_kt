package mango.interpreter.binding.nodes.expressions

import mango.interpreter.symbols.TypeSymbol

class StructFieldAccess(
    val struct: Expression,
    val i: Int
) : Expression() {

    inline val field: TypeSymbol.StructTypeSymbol.Field get() = (struct.type as TypeSymbol.StructTypeSymbol).getField(i)

    override val type = field.type
    override val kind = Kind.StructFieldAccess

    override fun toString() = field.name
}
