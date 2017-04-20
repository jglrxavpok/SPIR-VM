package org.jglr.spirvm

import org.jglr.sbm.visitors.CodeVisitor
import org.jglr.sbm.visitors.ModuleReader
import java.io.IOException

class VMExecutorReader(array: ByteArray, val state: VMState) : ModuleReader(array) {

    fun execute(entryPointName: String) {
        visitHeader()
        seek(state.entryPoints[entryPointName]!!.position)
        visitCode()
    }

    override fun newCodeVisitor(): CodeVisitor {
        return VMCodeExecutor(state, this)
    }

    override fun visitCode(): CodeVisitor {
        // this version does NOT reset the position field before visiting
        return visitCodeLoop()
    }


}