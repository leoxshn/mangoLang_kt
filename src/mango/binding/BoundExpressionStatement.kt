package mango.binding

class BoundExpressionStatement(
    val expression: BoundExpression
) : BoundStatement() {
    override val boundType = BoundNodeType.ExpressionStatement
}
