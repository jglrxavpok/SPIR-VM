package org.jglr.spirvm

import org.jglr.sbm.ExecutionModel
import org.jglr.sbm.FunctionControl
import org.jglr.sbm.types.FloatType
import org.jglr.sbm.visitors.AbstractCodeVisitor
import org.jglr.sbm.visitors.ModuleReader

class VMPopulator(val state: VMState, val reader: ModuleReader) : AbstractCodeVisitor() {

    private val entries = hashMapOf<Long, String>()
    private val functions = mutableListOf<FunctionInfo>()

    override fun visitEntryPoint(model: ExecutionModel, entryPoint: Long, name: String, interfaces: LongArray) {
        entries[entryPoint] = name
    }

    override fun visitFunction(resultType: Long, resultID: Long, control: FunctionControl, funcType: Long) {
        functions += FunctionInfo(resultID, reader.position)
    }

    override fun visitEnd() {
        functions
                .filter { entries.containsKey(it.id) }
                .forEach { state.registerEntryPoint(entries[it.id]!!, it) }
    }

    override fun visitFloatType(resultID: Long, width: Long) {
        state.slots[resultID] = TypeValue(FloatType(width))
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