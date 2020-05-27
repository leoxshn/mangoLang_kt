package mango.binding

import mango.symbols.VariableSymbol

class BoundVariableDeclaration(
    val variable: VariableSymbol,
    val initializer: BoundExpression
) : BoundStatement() {

    override val boundType = BoundNodeType.VariableDeclaration
}