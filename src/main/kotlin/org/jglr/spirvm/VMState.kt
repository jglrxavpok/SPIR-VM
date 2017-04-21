package org.jglr.spirvm

import org.jglr.sbm.types.FloatType
import org.jglr.sbm.types.FunctionType
import org.jglr.sbm.types.IntType
import org.jglr.sbm.types.Type

class VMState {

    var slots = Array<VMValue<*>>(0) { UndefinedValue }
        private set
    val entryPoints = hashMapOf<String, FunctionInfo>()

    fun allocateMemorySlots(amount: Long) {
        slots = Array<VMValue<*>>(amount.toInt()) { UndefinedValue }
    }

    fun registerEntryPoint(name: String, info: FunctionInfo) {
        entryPoints[name] = info
    }

    fun type(id: Long): Type {
        val slot = slots[id.toInt()]
        if(slot is TypeValue)
            return slot.value
        throw IllegalArgumentException("No type at slot $id: found $slot")
    }

}

data class FunctionInfo(val id: Long, val position: Int, val funcType: FunctionType, val argumentTypes: MutableMap<Long, Type> = hashMapOf())

abstract class VMValue<ObjectType>(val type: Type) {
    abstract var value: ObjectType
}

class FunctionInfoValue(override var value: FunctionInfo) : VMValue<FunctionInfo>(value.funcType)
class FloatValue(type: FloatType, override var value: Float) : VMValue<Float>(type)
class DoubleValue(type: FloatType, override var value: Double) : VMValue<Double>(type)
class SignedInt32Value(type: IntType, override var value: Int) : VMValue<Int>(type)
class SignedInt64Value(type: IntType, override var value: Long) : VMValue<Long>(type)
class TypeValue(type: Type) : VMValue<Type>(type) { override var value: Type = type }
class BasicValue(type: Type, override var value: Any): VMValue<Any>(type)

val UndefinedValue = object : VMValue<Unit>(Type.VOID) { override var value: Unit = Unit }