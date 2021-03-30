package mango.compiler.binding.nodes.statements

import mango.compiler.binding.nodes.expressions.Expression
import mango.compiler.symbols.VariableSymbol

class VariableDeclaration(
        val variable: VariableSymbol,
        val initializer: Expression
) : Statement() {

    override val kind = Kind.ValVarDeclaration
}