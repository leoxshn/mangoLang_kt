package mango.interpreter.binding.nodes.statements

import mango.interpreter.binding.nodes.expressions.BoundExpression

class ReturnStatement(
    val expression: BoundExpression?
) : Statement() {
    override val kind = Kind.ReturnStatement
}