package mango.interpreter.binding

import mango.interpreter.symbols.TypeSymbol
import mango.interpreter.syntax.SyntaxType

class BoundUnaryOperator(
    val syntaxType: SyntaxType,
    val type: BoundUnaryOperatorType,
    val operandType: TypeSymbol,
    val resultType: TypeSymbol = operandType
) {
    companion object {

        private val operators = arrayOf(
            BoundUnaryOperator(SyntaxType.Not, BoundUnaryOperatorType.Not, TypeSymbol.bool),
            BoundUnaryOperator(SyntaxType.Plus, BoundUnaryOperatorType.Identity, TypeSymbol.int),
            BoundUnaryOperator(SyntaxType.Minus, BoundUnaryOperatorType.Negation, TypeSymbol.int))

        fun bind(syntaxType: SyntaxType, operandType: TypeSymbol): BoundUnaryOperator? {
            for (op in operators) {
                if (op.syntaxType == syntaxType && op.operandType == operandType) {
                    return op
                }
            }
            return null
        }

        fun getString(type: BoundUnaryOperatorType): String = when (type) {
            BoundUnaryOperatorType.Identity -> "+"
            BoundUnaryOperatorType.Negation -> "-"
            BoundUnaryOperatorType.Not -> "!"
        }

        fun getString(type: SyntaxType): String = when (type) {
            SyntaxType.Plus -> "+"
            SyntaxType.Minus -> "-"
            SyntaxType.Not -> "!"
            else -> type.toString()
        }
    }
}