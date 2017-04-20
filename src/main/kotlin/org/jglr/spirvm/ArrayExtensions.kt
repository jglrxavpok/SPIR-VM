package org.jglr.spirvm

operator fun Array<VMValue<*>>.get(index: Long): VMValue<*> = this[index.toInt()]
operator fun Array<VMValue<*>>.set(index: Long, value: VMValue<*>) {
    this[index.toInt()] = value
}