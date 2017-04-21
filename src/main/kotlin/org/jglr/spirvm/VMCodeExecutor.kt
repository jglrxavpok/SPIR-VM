package org.jglr.spirvm

import org.jglr.sbm.types.FloatType
import org.jglr.sbm.visitors.AbstractCodeVisitor
import java.util.*

class VMCodeExecutor(val state: VMState, val reader: VMExecutorReader) : AbstractCodeVisitor() {

    private val callStack = Stack<Int>()
    private val returnIDStack = Stack<Long>()

    override fun visitFAdd(resultTypeID: Long, resultID: Long, leftID: Long, rightID: Long) {
        val left = state.slots[leftID]
        val right = state.slots[rightID]
        checkFloat32Type(left)
        checkFloat32Type(right)
        state.slots[resultID] = FloatValue(state.type(resultTypeID) as FloatType, left.value as Float + right.value as Float)
    }

    override fun visitFunctionCall(resultType: Long, resultID: Long, functionID: Long, arguments: LongArray) {
        // TODO: check argument types
        val info = (state.slots[functionID] as FunctionInfoValue).value
        val expectedArgumentCount = info.argumentTypes.size
        val actualArgumentCount = arguments.size
        assert(expectedArgumentCount == actualArgumentCount) { "Called function %${info.id} with wrong number of arguments, expected $expectedArgumentCount, got $actualArgumentCount" }

        callStack.push(reader.position) // the reader will then return to the next instruction after this call
        returnIDStack.push(resultID)
        reader.seek(info.position)

        info.argumentTypes.keys.forEachIndexed { index, id ->
            // transfer argument values to parameters
            state.slots[id] = state.slots[arguments[index]]
        }
    }

    fun checkFloat32Type(value: VMValue<*>) {
        assert(value.type is FloatType) {"Type must be float32, is ${value.type}"}
        assert((value.type as FloatType).width == 32L)  {"Type must be float32, is ${value.type}"}
    }

    fun checkFloat64Type(value: VMValue<*>) {
        assert(value.type is FloatType) {"Type must be float64, is ${value.type}"}
        assert((value.type as FloatType).width == 64L) {"Type must be float64, is ${value.type}"}
    }

    override fun visitFunctionEnd() {
        throw IllegalStateException("Entered non-function code, maybe you forgot a return in a function?")
    }

    override fun visitReturn() {
        if(callStack.isEmpty()) {
            reader.seek(reader.input.size) // jumps to the end to stop the execution
        } else {
            val destination = callStack.pop()
            returnIDStack.pop() // returned void, nothing to save
            reader.seek(destination)
        }
    }

    override fun visitReturnValue(valueID: Long) {
        if(callStack.isEmpty()) {
            reader.seek(reader.input.size) // jumps to the end to stop the execution
        } else {
            val destination = callStack.pop()
            val saveLocation = returnIDStack.pop()
            state.slots[saveLocation] = state.slots[valueID]
            reader.seek(destination)
        }
    }
}