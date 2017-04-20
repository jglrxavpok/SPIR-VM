package org.jglr.spirvm

import org.jglr.sbm.visitors.CodeVisitor
import org.jglr.sbm.visitors.HeaderVisitor
import org.jglr.sbm.visitors.ModuleReader

class SPIRVM {

    val state = VMState()

    fun execute(module: ByteArray, entryPointName: String) {
        populateState(module)

        val executor = VMExecutorReader(module, state)
        executor.execute(entryPointName)
    }

    private fun populateState(module: ByteArray) {
        val populator = object : ModuleReader(module) {
            override fun newHeaderVisitor(): HeaderVisitor {
                return VMHeaderVisitor(state)
            }

            override fun newCodeVisitor(): CodeVisitor {
                return VMPopulator(state, this)
            }
        }
        populator.visitHeader()
        populator.visitCode()
    }

}