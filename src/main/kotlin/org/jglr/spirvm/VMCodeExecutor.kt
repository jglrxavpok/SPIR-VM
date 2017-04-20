package org.jglr.spirvm

import org.jglr.sbm.types.FloatType
import org.jglr.sbm.visitors.AbstractCodeVisitor

class VMCodeExecutor(val state: VMState, val reader: VMExecutorReader) : AbstractCodeVisitor() {

    override fun visitFAdd(resultTypeID: Long, resultID: Long, leftID: Long, rightID: Long) {
        val left = state.slots[leftID]
        val right = state.slots[rightID]
        checkFloat32Type(left)
        checkFloat32Type(right)
        state.slots[resultID] = FloatValue(state.type(resultTypeID) as FloatType, left.value as Float + right.value as Float)
    }

    fun checkFloat32Type(value: VMValue<*>) {
        assert(value.type is FloatType)
        assert((value.type as FloatType).width == 32L)
    }

    fun checkFloat64Type(value: VMValue<*>) {
        assert(value.type is FloatType)
        assert((value.type as FloatType).width == 64L)
    }

    override fun visitFunctionEnd() {
        // TODO: Implement a call stack ?
        reader.seek(reader.input.size) // jumps to the end to stop the execution
    }
}