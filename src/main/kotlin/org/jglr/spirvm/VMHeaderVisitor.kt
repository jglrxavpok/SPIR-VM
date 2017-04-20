package org.jglr.spirvm

import org.jglr.sbm.visitors.HeaderVisitor

class VMHeaderVisitor(val state: VMState) : HeaderVisitor {

    override fun visitSpirVersion(value: Long) {
        println("Spir-V Version: $value")
    }

    override fun visitGeneratorMagicNumber(value: Long) {
    }

    override fun visitBound(value: Long) {
        state.allocateMemorySlots(value)
    }

    override fun visitInstructionSchema(value: Long) {
    }

}
