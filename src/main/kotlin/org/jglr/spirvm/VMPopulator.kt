package org.jglr.spirvm

import org.jglr.sbm.ExecutionModel
import org.jglr.sbm.FunctionControl
import org.jglr.sbm.types.FloatType
import org.jglr.sbm.types.FunctionType
import org.jglr.sbm.types.Type
import org.jglr.sbm.visitors.AbstractCodeVisitor
import org.jglr.sbm.visitors.ModuleReader

class VMPopulator(val state: VMState, val reader: ModuleReader) : AbstractCodeVisitor() {

    private val entries = hashMapOf<Long, String>()
    private val functions = mutableListOf<FunctionInfo>()
    private var currentFunction: FunctionInfo? = null

    override fun visitEntryPoint(model: ExecutionModel, entryPoint: Long, name: String, interfaces: LongArray) {
        entries[entryPoint] = name
    }

    override fun visitFunction(resultType: Long, resultID: Long, control: FunctionControl, funcType: Long) {
        val info = FunctionInfo(resultID, reader.position, state.type(funcType) as FunctionType)
        state.slots[resultID] = FunctionInfoValue(info)
        functions += info
        currentFunction = info
    }

    override fun visitFunctionParameter(resultType: Long, resultID: Long) {
        if(currentFunction == null) {
            throw InvalidModuleException("Found a function parameter outside of a function declaration")
        } else {
            currentFunction!!.argumentTypes[resultID] = state.type(resultType)
        }
    }

    override fun visitFunctionEnd() {
        currentFunction = null
    }

    override fun visitEnd() {
        functions
                .filter { entries.containsKey(it.id) }
                .forEach { state.registerEntryPoint(entries[it.id]!!, it) }
    }

    override fun visitFloatType(resultID: Long, width: Long) {
        state.slots[resultID] = TypeValue(FloatType(width))
    }

    override fun visitVoidType(resultID: Long) {
        state.slots[resultID] = TypeValue(Type.VOID)
    }

    override fun visitFunctionType(resultID: Long, returnTypeID: Long, parameterTypes: LongArray) {
        val returnType = state.type(returnTypeID)
        val parameters = Array(parameterTypes.size) { state.type(it.toLong()) }
        state.slots[resultID] = TypeValue(FunctionType(returnType, *parameters))
    }

    override fun visitConstant(type: Long, resultID: Long, bitPattern: LongArray) {
        val constantType = state.type(type)
        if(constantType is FloatType) {
            val floatValue = java.lang.Float.intBitsToFloat(bitPattern[0].toInt())
            state.slots[resultID] = FloatValue(constantType, floatValue)
        } else {
            state.slots[resultID] = BasicValue(constantType, bitPattern)
        }
    }
}