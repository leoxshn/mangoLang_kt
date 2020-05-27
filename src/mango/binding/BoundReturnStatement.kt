package mango.binding

class BoundReturnStatement(
    val expression: BoundExpression?
) : BoundStatement() {
    override val boundType = BoundNodeType.ReturnStatement
}