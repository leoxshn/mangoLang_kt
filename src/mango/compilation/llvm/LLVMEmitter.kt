package mango.compilation.llvm

import mango.compilation.Emitter
import mango.compilation.llvm.LLVMValue.*
import mango.compilation.llvm.LLVMValue.Float
import mango.compilation.llvm.LLVMValue.Int
import mango.interpreter.binding.Program
import mango.interpreter.binding.nodes.BoundNode
import mango.interpreter.binding.nodes.UnOperator
import mango.interpreter.binding.nodes.expressions.*
import mango.interpreter.binding.nodes.statements.*
import mango.interpreter.symbols.Symbol
import mango.interpreter.symbols.TypeSymbol
import mango.interpreter.symbols.VisibleSymbol
import mango.util.BinderError
import mango.util.EmitterError

object LLVMEmitter : Emitter {

    override fun emit(
        program: Program,
        moduleName: String
    ): String {

        val builder = ModuleBuilder()
        lateinit var initBlock: BlockBuilder

        for (struct in TypeSymbol.map.values) {
            if (struct is TypeSymbol.StructTypeSymbol) {
                builder.declareStruct(struct.name, struct.fields.map {
                    LLVMType[it.type]
                }.toTypedArray())
            }
        }

        for (f in program.functions) {
            val symbol = f.key
            val body = f.value
            if (symbol.meta.isExtern) {
                builder.addDeclaration(symbol)
                continue
            }
            val function = builder.createFunction(symbol)
            if (symbol.meta.isInline) {
                function.addAttribute("alwaysinline")
            }
            var currentBlock = if (symbol.meta.isEntry) {
                initBlock = function.entryBlock()
                function.createBlock(null)
            } else function.entryBlock()
            for (statement in body!!.statements) {
                when (statement.kind) {
                    BoundNode.Kind.ExpressionStatement -> {
                        val expression = (statement as ExpressionStatement).expression
                        emitValue(currentBlock, expression)
                    }
                    BoundNode.Kind.VariableDeclaration -> {
                        statement as VariableDeclaration
                        if (statement.initializer.kind == BoundNode.Kind.LiteralExpression ||
                            statement.initializer.kind == BoundNode.Kind.NameExpression ||
                            statement.initializer.kind == BoundNode.Kind.ReferenceExpression ||
                            statement.initializer.kind == BoundNode.Kind.StructInitialization) {
                            val value = emitValue(currentBlock, statement.initializer)!!
                            val alloc = currentBlock.alloc(statement.variable.realName, value.type)
                            currentBlock.store(alloc, value)
                        } else {
                            val initializer = emitInstruction(currentBlock, statement.initializer)!!
                            val alloc = currentBlock.alloc(statement.variable.realName, initializer.type)
                            currentBlock.store(alloc, currentBlock.tmpVal(initializer).ref)
                        }
                    }
                    BoundNode.Kind.LabelStatement -> {
                        statement as LabelStatement
                        val lastInstruction = currentBlock.lastOrNull()
                        if (lastInstruction.isJump) {
                            currentBlock.addInstruction(Jmp(statement.symbol.name))
                        }
                        currentBlock = currentBlock.functionBuilder.createBlock(statement.symbol.name)
                    }
                    BoundNode.Kind.GotoStatement -> currentBlock.jump((statement as GotoStatement).label.name)
                    BoundNode.Kind.ConditionalGotoStatement -> {
                        statement as ConditionalGotoStatement
                        val condition = emitValue(currentBlock, statement.condition)!!
                        val antiName = statement.label.name + "_anti"
                        if (statement.jumpIfTrue) {
                            currentBlock.conditionalJump(condition, statement.label.name, antiName)
                        } else {
                            currentBlock.conditionalJump(condition, antiName, statement.label.name)
                        }
                        currentBlock = currentBlock.functionBuilder.createBlock(antiName)
                    }
                    BoundNode.Kind.ReturnStatement -> {
                        statement as ReturnStatement
                        if (statement.expression == null) {
                            currentBlock.ret()
                        } else {
                            currentBlock.ret(emitValue(currentBlock, statement.expression)!!)
                        }
                    }
                    BoundNode.Kind.AssignmentStatement -> {
                        statement as Assignment
                        val value = emitValue(currentBlock, statement.expression)!!
                        when (statement.assignee.kind) {
                            BoundNode.Kind.NameExpression -> {
                                val assignee = statement.assignee as NameExpression
                                val variable = assignee.symbol
                                currentBlock.store(if (variable is VisibleSymbol) {
                                    GlobalRef(assignee.symbol.realName, LLVMType.Ptr(LLVMType[assignee.symbol.type]))
                                } else {
                                    LocalRef(assignee.symbol.realName, LLVMType.Ptr(LLVMType[assignee.symbol.type]))
                                }, value)
                            }
                            BoundNode.Kind.StructFieldAccess -> {
                                val assignee = statement.assignee as StructFieldAccess
                                currentBlock.setStructField(emitValue(currentBlock, assignee.struct)!!, assignee.i, assignee.field, value)
                            }
                            else -> throw BinderError("${statement.assignee.kind} isn't a valid assignee")
                        }
                    }
                    BoundNode.Kind.PointerAccessAssignment -> {
                        statement as PointerAccessAssignment
                        val pointer = emitValue(currentBlock, statement.expression)!!
                        val i = emitValue(currentBlock, statement.i)!!
                        val ptr = currentBlock.tmpVal(GetPtr((pointer.type as LLVMType.Ptr).element, pointer, i))
                        val value = emitValue(currentBlock, statement.value)!!
                        currentBlock.store(ptr.ref, value)
                    }
                    BoundNode.Kind.NopStatement -> {}
                    else -> throw EmitterError("internal error: Unknown statement to LLVM")
                }
            }
            if (symbol.returnType == TypeSymbol.Unit && body.statements.lastOrNull()?.kind != BoundNode.Kind.ReturnStatement) {
                currentBlock.ret()
            }
        }
        for (v in program.statement.statements) {
            v as VariableDeclaration
            val expression = v.initializer
            val value: LLVMValue? = when (expression.kind) {
                BoundNode.Kind.LiteralExpression -> emitLiteral(initBlock, expression as LiteralExpression)
                BoundNode.Kind.NameExpression -> emitVariableExpression(initBlock, expression as NameExpression)
                else -> {
                    val instruction = emitInstruction(initBlock, expression)!!
                    val type = instruction.type
                    if (type != LLVMType.Void) {
                        initBlock.tmpVal(instruction).ref
                    }
                    else {
                        initBlock.addInstruction(instruction)
                        null
                    }
                }
            }
            builder.globalVariable((v.variable as VisibleSymbol).mangledName(), value!!)
        }
        return builder.code()
    }

    private fun emitValue(
        block: BlockBuilder,
        expression: Expression
    ): LLVMValue? = when (expression.kind) {
        BoundNode.Kind.LiteralExpression -> emitLiteral(block, expression as LiteralExpression)
        BoundNode.Kind.NameExpression -> emitVariableExpression(block, expression as NameExpression)
        BoundNode.Kind.ReferenceExpression -> emitReference(block, (expression as Reference).expression)
        BoundNode.Kind.StructInitialization -> emitStructInitialization(block, expression as StructInitialization)
        BoundNode.Kind.PointerArrayInitialization -> emitPointerArrayInitialization(block, expression as PointerArrayInitialization)
        BoundNode.Kind.ErrorExpression -> throw EmitterError("Error expression got to the emission stage")
        else -> {
            val instruction = emitInstruction(block, expression)!!
            val type = instruction.type
            if (type == LLVMType.Void) {
                block.addInstruction(instruction)
                null
            } else {
                block.tmpVal(instruction).ref
            }
        }
    }

    private fun emitPointerArrayInitialization(
        block: BlockBuilder,
        expression: PointerArrayInitialization
    ): LLVMValue {
        return if (expression.length == null) {
            val alloc = block.mallocArray(LLVMType[expression.type], expression.expressions!!.size)
            for (i in expression.expressions.indices) {
                block.setArrayElement(alloc, Int(i, LLVMType.I32), emitValue(block, expression.expressions[i])!!)
            }
            alloc
        } else {
            if (expression.length.constantValue == null) {
                block.mallocArray(LLVMType[expression.type], emitValue(block, expression.length)!!)
            } else {
                block.mallocArray(LLVMType[expression.type], expression.length.constantValue!!.value as kotlin.Int)
            }
        }
    }

    private fun emitStructInitialization(
        block: BlockBuilder,
        expression: StructInitialization
    ): LLVMValue {
        val alloc = block.malloc(LLVMType[expression.type])
        for (entry in expression.fields) {
            val field = entry.key
            val value = entry.value
            block.setStructField(alloc, expression.type.fields.indexOf(field), field, emitValue(block, value)!!)
        }
        return alloc
    }

    private fun emitLiteral(
        block: BlockBuilder,
        expression: LiteralExpression
    ): LLVMValue = when {
        expression.value == null -> Null(LLVMType[expression.type])
        expression.type.isOfType(TypeSymbol["String"]!!) -> block.stringConstForContent(expression.value as String).ref
        expression.type.isOfType(TypeSymbol.Integer) -> Int((expression.value as Number).toInt(), LLVMType[expression.type])
        expression.type.isOfType(TypeSymbol.UInteger) -> Int((expression.value as Number).toInt(), LLVMType[expression.type])
        expression.type.isOfType(TypeSymbol.Float) -> Float((expression.value as Number).toFloat(), LLVMType.Float)
        expression.type.isOfType(TypeSymbol.Double) -> Float((expression.value as Number).toFloat(), LLVMType.Double)
        expression.type.isOfType(TypeSymbol.Bool) -> Bool(expression.value as Boolean)
        else -> throw EmitterError("Unknown literal type")
    }

    private fun emitVariableExpression(
        block: BlockBuilder,
        expression: NameExpression
    ): LLVMValue = when (expression.symbol.kind) {
        Symbol.Kind.Parameter -> {
            val i = block.functionBuilder.symbol.parameters.indexOfFirst {
                it.realName == expression.symbol.realName
            }
            block.functionBuilder.paramReference(i)
        }
        else -> {
            var value = emitReference(block, expression)
            var type = value.type as LLVMType.Ptr
            if (value is LocalRef && type.element is LLVMType.Fn) {
                type = LLVMType.Ptr(type)
                value = LocalRef(value.name, type)
            }
            if (type.element is LLVMType.Fn) { value } else block.load(value)
        }
    }

    private fun emitStructFieldAccess(
        block: BlockBuilder,
        expression: StructFieldAccess
    ) = block.getStructField(emitValue(block, expression.struct)!!, expression.i, expression.field)

    private fun emitCastExpression(
        block: BlockBuilder,
        expression: CastExpression
    ): LLVMInstruction {
        val value = emitValue(block, expression.expression)!!
        val type = LLVMType[expression.type]
        return Conversion(when {
            value.type is LLVMType.I && value.type.bits > type.bits -> Conversion.Kind.Truncate
            value.type is LLVMType.U && value.type.bits < type.bits -> Conversion.Kind.ZeroExt
            value.type is LLVMType.I && value.type.bits < type.bits -> Conversion.Kind.SignExt
            else -> Conversion.Kind.BitCast
        }, value, type)
    }

    private fun emitReference(
        block: BlockBuilder,
        expression: NameExpression
    ) = if (expression.symbol is VisibleSymbol) {
        val variable = expression.symbol
        GlobalRef(variable.mangledName(), LLVMType[variable.type])
    } else {
        val variable = expression.symbol
        LocalRef(variable.realName, LLVMType.Ptr(LLVMType[variable.type]))
    }

    private fun emitInstruction(
        block: BlockBuilder,
        expression: Expression
    ): LLVMInstruction? = when (expression.kind) {
        BoundNode.Kind.CallExpression -> {
            expression as CallExpression
            val type = LLVMType[expression.type]
            val function = emitValue(block, expression.expression)!!
            Call(type, function, *Array(expression.arguments.size) {
                emitValue(block, expression.arguments.elementAt(it))!!
            })
        }
        BoundNode.Kind.UnaryExpression -> emitUnaryExpression(block, expression as UnaryExpression)
        BoundNode.Kind.BinaryExpression -> emitBinaryExpression(block, expression as BinaryExpression)
        BoundNode.Kind.PointerAccessExpression -> emitPointerAccessExpression(block, expression as PointerAccess)
        BoundNode.Kind.ReferenceExpression -> emitInstruction(block, (expression as Reference).expression)
        BoundNode.Kind.StructFieldAccess -> emitStructFieldAccess(block, expression as StructFieldAccess)
        BoundNode.Kind.CastExpression -> emitCastExpression(block, expression as CastExpression)
        BoundNode.Kind.ErrorExpression -> throw EmitterError("Error expression got to the emission stage")
        else -> throw EmitterError("internal error: Unknown expression to LLVM (${expression.kind}):\n${expression.structureString(1, false)}")
    }

    private fun emitUnaryExpression(
        block: BlockBuilder,
        expression: UnaryExpression
    ): LLVMInstruction {
        val operand = emitValue(block, expression.operand)!!
        return when (expression.operator.type) {
            UnOperator.Type.Identity -> Operation(Operation.Kind.IntAdd, Int(0, operand.type), operand)
            UnOperator.Type.Negation -> Operation(Operation.Kind.IntSub, Int(0, operand.type), operand)
            UnOperator.Type.Not -> Operation(Operation.Kind.IntSub, Int(1, LLVMType.Bool), operand)
        }
    }

    private fun emitBinaryExpression(
        block: BlockBuilder,
        expression: BinaryExpression
    ): LLVMInstruction? {
        val left = emitValue(block, expression.left)!!
        val right = emitValue(block, expression.right)!!
        val operation = Operation[left.type, expression.operator.type, right.type]
        if (operation != null) {
            val max = max(left.type, right.type)
            return Operation(operation, block.extendIfNecessary(left, max), block.extendIfNecessary(right, max))
        }
        val comparison = Icmp[left.type, expression.operator.type, right.type]
        if (comparison != null) {
            return Icmp(comparison, left, right)
        }
        throw EmitterError("Binary operator (${expression.operator.type}) couldn't be converted to LLVM IR")
    }

    private fun emitPointerAccessExpression(
        block: BlockBuilder,
        expression: PointerAccess
    ): LLVMInstruction {
        val pointer = emitValue(block, expression.expression)!!
        val i = emitValue(block, expression.i)!!
        return block.getArrayElement(pointer, i)
        /*
        val ptr = block.tmpVal(GetPtr((pointer.type as LLVMType.Ptr).element, pointer, i))
        return Load(ptr.ref, (ptr.type as LLVMType.Ptr).element)*/
    }
}