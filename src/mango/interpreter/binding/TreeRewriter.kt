package mango.interpreter.binding

import mango.interpreter.binding.nodes.BoundNode
import mango.interpreter.binding.nodes.expressions.PointerAccess
import mango.interpreter.binding.nodes.expressions.*
import mango.interpreter.binding.nodes.statements.*
import mango.util.BinderError

open class TreeRewriter {

    fun rewriteStatement(node: Statement): Statement {
        return when (node.kind) {
            BoundNode.Kind.BlockStatement -> rewriteBlockStatement(node as BlockStatement)
            BoundNode.Kind.ExpressionStatement -> rewriteExpressionStatement(node as ExpressionStatement)
            BoundNode.Kind.VariableDeclaration -> rewriteVariableDeclaration(node as VariableDeclaration)
            BoundNode.Kind.IfStatement -> rewriteIfStatement(node as IfStatement)
            BoundNode.Kind.WhileStatement -> rewriteWhileStatement(node as WhileStatement)
            BoundNode.Kind.ForStatement -> rewriteForStatement(node as ForStatement)
            BoundNode.Kind.LabelStatement -> rewriteLabelStatement(node as LabelStatement)
            BoundNode.Kind.GotoStatement -> rewriteGotoStatement(node as GotoStatement)
            BoundNode.Kind.ConditionalGotoStatement -> rewriteConditionalGotoStatement(node as ConditionalGotoStatement)
            BoundNode.Kind.ReturnStatement -> rewriteReturnStatement(node as ReturnStatement)
            BoundNode.Kind.NopStatement -> rewriteNopStatement(node as NopStatement)
            else -> throw BinderError("Unexpected node: ${node.kind}")
        }
    }

    protected open fun rewriteBlockStatement(node: BlockStatement): BlockStatement {
        var statements: ArrayList<Statement>? = null
        for (i in node.statements.indices) {
            val oldStatement = node.statements.elementAt(i)
            val newStatement = rewriteStatement(oldStatement)
            if (newStatement != oldStatement) {
                if (statements == null) {
                    statements = ArrayList()
                    for (j in 0 until i) {
                        statements.add(node.statements.elementAt(j))
                    }
                }
            }
            statements?.add(newStatement)
        }

        if (statements == null) {
            return node
        }
        return BlockStatement(statements)
    }

    protected open fun rewriteExpressionStatement(node: ExpressionStatement): Statement {
        val expression = rewriteExpression(node.expression)
        if (expression == node.expression) {
            return node
        }
        return ExpressionStatement(expression)
    }

    protected open fun rewriteVariableDeclaration(node: VariableDeclaration): Statement {
        val initializer = rewriteExpression(node.initializer)
        if (initializer == node.initializer) {
            return node
        }
        return VariableDeclaration(node.variable, initializer)
    }

    protected open fun rewriteIfStatement(node: IfStatement): Statement {
        val condition = rewriteExpression(node.condition)
        val body = rewriteBlockStatement(node.statement)
        val elseStatement = if (node.elseStatement == null) { null } else {
            rewriteStatement(node.elseStatement)
        }
        if (condition == node.condition && body == node.statement && elseStatement == node.elseStatement) {
            return node
        }
        return IfStatement(condition, body, elseStatement)
    }

    protected open fun rewriteWhileStatement(node: WhileStatement): Statement {
        val condition = rewriteExpression(node.condition)
        val body = rewriteBlockStatement(node.body)
        if (condition == node.condition && body == node.body) {
            return node
        }
        return WhileStatement(condition, body, node.breakLabel, node.continueLabel)
    }

    protected open fun rewriteForStatement(node: ForStatement): Statement {
        val lowerBound = rewriteExpression(node.lowerBound)
        val upperBound = rewriteExpression(node.upperBound)
        val body = rewriteBlockStatement(node.body)
        if (lowerBound == node.lowerBound && upperBound == node.upperBound && body == node.body) {
            return node
        }
        return ForStatement(node.variable, lowerBound, upperBound, body, node.breakLabel, node.continueLabel)
    }

    protected open fun rewriteLabelStatement(node: LabelStatement) = node
    protected open fun rewriteGotoStatement(node: GotoStatement) = node

    protected open fun rewriteConditionalGotoStatement(node: ConditionalGotoStatement): Statement {
        val condition = rewriteExpression(node.condition)
        if (condition == node.condition) {
            return node
        }
        return ConditionalGotoStatement(node.label, condition, node.jumpIfTrue)
    }

    protected open fun rewriteReturnStatement(node: ReturnStatement): Statement {
        return ReturnStatement(node.expression?.let { rewriteExpression(it) })
    }

    private fun rewriteNopStatement(node: NopStatement): Statement {
        return node
    }

    private fun rewriteExpression(node: BoundExpression) = when (node.kind) {
        BoundNode.Kind.UnaryExpression -> rewriteUnaryExpression(node as UnaryExpression)
        BoundNode.Kind.BinaryExpression -> rewriteBinaryExpression(node as BinaryExpression)
        BoundNode.Kind.LiteralExpression -> rewriteLiteralExpression(node as LiteralExpression)
        BoundNode.Kind.VariableExpression -> rewriteVariableExpression(node as NameExpression)
        BoundNode.Kind.AssignmentExpression -> rewriteAssignmentExpression(node as AssignmentExpression)
        BoundNode.Kind.CallExpression -> rewriteCallExpression(node as CallExpression)
        BoundNode.Kind.CastExpression -> rewriteCastExpression(node as CastExpression)
        BoundNode.Kind.ErrorExpression -> node
        BoundNode.Kind.StructFieldAccess -> rewriteStructFieldAccess(node as StructFieldAccess)
        BoundNode.Kind.BlockExpression -> rewriteBlockExpression(node as BlockExpression)
        BoundNode.Kind.ReferenceExpression -> rewriteReferenceExpression(node as Reference)
        BoundNode.Kind.PointerAccessExpression -> rewritePointerAccessExpression(node as PointerAccess)
        else -> throw BinderError("Unexpected node: ${node.kind}")
    }

    protected open fun rewriteLiteralExpression(node: LiteralExpression) = node
    protected open fun rewriteVariableExpression(node: NameExpression) = node

    protected open fun rewriteUnaryExpression(node: UnaryExpression): BoundExpression {
        val operand = rewriteExpression(node.operand)
        if (operand == node.operand) {
            return node
        }
        return UnaryExpression(node.operator, operand)
    }

    protected open fun rewriteBinaryExpression(node: BinaryExpression): BoundExpression {
        val left = rewriteExpression(node.left)
        val right = rewriteExpression(node.right)
        if (left == node.left && right == node.right) {
            return node
        }
        return BinaryExpression(left, node.operator, right)
    }

    protected open fun rewriteAssignmentExpression(node: AssignmentExpression): BoundExpression {
        val expression = rewriteExpression(node.expression)
        if (expression == node.expression) {
            return node
        }
        return AssignmentExpression(node.variable, expression)
    }

    protected fun rewriteCallExpression(node: CallExpression): BoundExpression {
        var args: ArrayList<BoundExpression>? = null
        for (i in node.arguments.indices) {
            val oldArgument = node.arguments.elementAt(i)
            val newArgument = rewriteExpression(oldArgument)
            if (newArgument != oldArgument) {
                if (args == null) {
                    args = ArrayList()
                    for (j in 0 until i) {
                        args.add(node.arguments.elementAt(j))
                    }
                }
            }
            args?.add(newArgument)
        }
        val expression = rewriteExpression(node.expression)
        if (args == null && expression == node.expression) {
            return node
        }
        return CallExpression(node.expression, args ?: node.arguments)
    }

    protected fun rewriteCastExpression(node: CastExpression): BoundExpression {
        val expression = rewriteExpression(node.expression)
        if (expression == node.expression) {
            return node
        }
        return CastExpression(node.type, expression)
    }

    protected fun rewriteStructFieldAccess(node: StructFieldAccess): BoundExpression {
        val struct = rewriteExpression(node.struct)
        if (struct == node.struct) {
            return node
        }
        return StructFieldAccess(struct, node.i)
    }

    protected open fun rewriteBlockExpression(node: BlockExpression): BoundExpression {
        var statements: ArrayList<Statement>? = null
        for (i in node.statements.indices) {
            val oldStatement = node.statements.elementAt(i)
            val newStatement = rewriteStatement(oldStatement)
            if (newStatement != oldStatement) {
                if (statements == null) {
                    statements = ArrayList()
                    for (j in 0 until i) {
                        statements.add(node.statements.elementAt(j))
                    }
                }
            }
            statements?.add(newStatement)
        }

        if (statements == null) {
            return node
        }
        return BlockExpression(statements, node.type)
    }

    protected fun rewriteReferenceExpression(node: Reference): BoundExpression {
        val expression = rewriteVariableExpression(node.expression)
        if (expression == node.expression) {
            return node
        }
        return Reference(expression)
    }

    protected fun rewritePointerAccessExpression(node: PointerAccess): BoundExpression {
        val expression = rewriteExpression(node.expression)
        val i = rewriteExpression(node.i)
        if (expression == node.expression && i == node.i) {
            return node
        }
        return PointerAccess(expression, i)
    }
}