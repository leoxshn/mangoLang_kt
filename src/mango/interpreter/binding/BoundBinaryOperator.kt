package mango.interpreter.binding

import mango.interpreter.symbols.TypeSymbol
import mango.interpreter.syntax.SyntaxType

class BoundBinaryOperator(
    val syntaxType: SyntaxType,
    val type: BoundBinaryOperatorType,
    val leftType: TypeSymbol,
    val rightType: TypeSymbol = leftType,
    val resultType: TypeSymbol = leftType
) {
    companion object {

        private val operators = arrayOf(
            BoundBinaryOperator(SyntaxType.Plus, BoundBinaryOperatorType.Add, TypeSymbol.int),
            BoundBinaryOperator(SyntaxType.Minus, BoundBinaryOperatorType.Sub, TypeSymbol.int),
            BoundBinaryOperator(SyntaxType.Mul, BoundBinaryOperatorType.Mul, TypeSymbol.int),
            BoundBinaryOperator(SyntaxType.Div, BoundBinaryOperatorType.Div, TypeSymbol.int),
            BoundBinaryOperator(SyntaxType.Rem, BoundBinaryOperatorType.Rem, TypeSymbol.int),
            BoundBinaryOperator(SyntaxType.BitAnd, BoundBinaryOperatorType.BitAnd, TypeSymbol.int),
            BoundBinaryOperator(SyntaxType.BitOr, BoundBinaryOperatorType.BitOr, TypeSymbol.int),

            BoundBinaryOperator(SyntaxType.LessThan, BoundBinaryOperatorType.LessThan, TypeSymbol.int, resultType = TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.MoreThan, BoundBinaryOperatorType.MoreThan, TypeSymbol.int, resultType = TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.IsEqual, BoundBinaryOperatorType.IsEqual, TypeSymbol.int, resultType = TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.IsEqualOrLess, BoundBinaryOperatorType.IsEqualOrLess, TypeSymbol.int, resultType = TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.IsEqualOrMore, BoundBinaryOperatorType.IsEqualOrMore, TypeSymbol.int, resultType = TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.IsNotEqual, BoundBinaryOperatorType.IsNotEqual, TypeSymbol.int, resultType = TypeSymbol.bool),

            BoundBinaryOperator(SyntaxType.IsIdentityEqual, BoundBinaryOperatorType.IsIdentityEqual, TypeSymbol.int, resultType = TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.IsNotIdentityEqual, BoundBinaryOperatorType.IsNotIdentityEqual, TypeSymbol.int, resultType = TypeSymbol.bool),

            BoundBinaryOperator(SyntaxType.BitAnd, BoundBinaryOperatorType.BitAnd, TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.BitOr, BoundBinaryOperatorType.BitOr, TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.LogicAnd, BoundBinaryOperatorType.LogicAnd, TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.LogicOr, BoundBinaryOperatorType.LogicOr, TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.IsEqual, BoundBinaryOperatorType.IsEqual, TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.IsNotEqual, BoundBinaryOperatorType.IsNotEqual, TypeSymbol.bool),

            BoundBinaryOperator(SyntaxType.IsIdentityEqual, BoundBinaryOperatorType.IsIdentityEqual, TypeSymbol.bool, resultType = TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.IsNotIdentityEqual, BoundBinaryOperatorType.IsNotIdentityEqual, TypeSymbol.bool, resultType = TypeSymbol.bool),

            BoundBinaryOperator(SyntaxType.Plus, BoundBinaryOperatorType.Add, TypeSymbol.string),
            BoundBinaryOperator(SyntaxType.IsEqual, BoundBinaryOperatorType.IsEqual, TypeSymbol.string, resultType = TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.IsNotEqual, BoundBinaryOperatorType.IsNotEqual, TypeSymbol.string, resultType = TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.IsIdentityEqual, BoundBinaryOperatorType.IsIdentityEqual, TypeSymbol.string, resultType = TypeSymbol.bool),
            BoundBinaryOperator(SyntaxType.IsNotIdentityEqual, BoundBinaryOperatorType.IsNotIdentityEqual, TypeSymbol.string, resultType = TypeSymbol.bool))

        fun bind(syntaxType: SyntaxType, leftType: TypeSymbol, rightType: TypeSymbol): BoundBinaryOperator? {
            for (op in operators) {
                if (op.syntaxType == syntaxType &&
                    op.leftType == leftType &&
                    op.rightType == rightType) {
                    return op
                }
            }
            return null
        }


        fun getString(type: BoundBinaryOperatorType): String = when (type) {
            BoundBinaryOperatorType.Add -> "+"
            BoundBinaryOperatorType.Sub -> "-"
            BoundBinaryOperatorType.Mul -> "*"
            BoundBinaryOperatorType.Div -> "/"
            BoundBinaryOperatorType.Rem -> "%"
            BoundBinaryOperatorType.BitAnd -> "&"
            BoundBinaryOperatorType.BitOr -> "|"
            BoundBinaryOperatorType.LogicAnd -> "&&"
            BoundBinaryOperatorType.LogicOr -> "||"
            BoundBinaryOperatorType.LessThan -> "<"
            BoundBinaryOperatorType.MoreThan -> ">"
            BoundBinaryOperatorType.IsEqual -> "=="
            BoundBinaryOperatorType.IsEqualOrMore -> ">="
            BoundBinaryOperatorType.IsEqualOrLess -> "<="
            BoundBinaryOperatorType.IsNotEqual -> "!="
            BoundBinaryOperatorType.IsIdentityEqual -> "==="
            BoundBinaryOperatorType.IsNotIdentityEqual -> "!=="
        }
    }
}