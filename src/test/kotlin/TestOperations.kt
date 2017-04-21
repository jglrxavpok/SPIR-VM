import org.jglr.sbm.ExecutionModel
import org.jglr.sbm.StorageClass
import org.jglr.sbm.instructions.ResultInstruction
import org.jglr.sbm.types.FloatType
import org.jglr.sbm.types.FunctionType
import org.jglr.sbm.types.IntType
import org.jglr.sbm.types.Type
import org.jglr.sbm.utils.ModuleFunction
import org.jglr.sbm.utils.ModuleGenerator
import org.jglr.sbm.utils.ModuleVariable
import org.jglr.sbm.visitors.CodeCollector
import org.jglr.sbm.visitors.ModuleReader
import org.jglr.spirvm.SPIRVM
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

class TestOperations {

    @Test
    fun addNumbers() {
        val generator = ModuleGenerator()
        val float32 = FloatType(32)
        val resultVar = generator.declareVariable("result", float32, StorageClass.Output)
        val f1 = generator.constantFloat("f1", float32, 1f)
        val f2 = generator.constantFloat("f2", float32, 2f)
        val functionDef = ModuleFunction("main", FunctionType(Type.VOID))
        generator.addEntryPoint(functionDef, ExecutionModel.Kernel, emptyArray())
        val fungen = generator.createFunction(functionDef)
        fungen.addFloat(resultVar, f1, f2)
        fungen.end()
        generator.end()
        val module = generator.toBytes()
        printContent(module)
        val vm = SPIRVM()
        vm.execute(module, "main")
        val resultID = generator.getComponentID(resultVar)
        assert(vm.state.slots[resultID.toInt()].value == 3f)
    }

    @Test
    fun functionCall() {
        val generator = ModuleGenerator()
        val float32 = FloatType(32)
        val f1 = generator.constantFloat("f1", float32, 1f)
        val f2 = generator.constantFloat("f2", float32, 2f)
        val functionDef = ModuleFunction("main", FunctionType(Type.VOID))
        generator.addEntryPoint(functionDef, ExecutionModel.Kernel, emptyArray())

        val otherFunctionDef = ModuleFunction("add", FunctionType(Type.VOID))
        val otherfunc = generator.createFunction(otherFunctionDef)
        val aParameter = ModuleVariable("a", float32)
        val bParameter = ModuleVariable("b", float32)
        otherfunc.parameter(aParameter)
        otherfunc.parameter(bParameter)
        val addResult = ModuleVariable("addResult", float32)
        otherfunc.addFloat(addResult, aParameter, bParameter)
        otherfunc.returnValue(addResult)
        otherfunc.end()

        val fungen = generator.createFunction(functionDef)
        val funcCallResult = fungen.callFunction(otherFunctionDef, f1, f2)
        fungen.returnVoid()
        fungen.end()

        generator.end()
        val module = generator.toBytes()
        printContent(module)
        val vm = SPIRVM()
        vm.execute(module, "main")
        val resultID = generator.getComponentID(funcCallResult)

        val result = vm.state.slots[resultID.toInt()].value
        assert(result == 3f) { "result was $result instead of 3f" }
    }

    private fun printContent(bytes: ByteArray) {
        println("=== START ===")
        val reader = ModuleReader(bytes)
        reader.visitHeader()
        val codeCollector = reader.visitCode() as CodeCollector
        codeCollector.instructions.forEach { i ->
            print(i.wordCount.toString() + " words: ")
            if (i is ResultInstruction)
                print("%" + i.resultID + " = ")
            println(i.toString())
        }
        println("=== END ===")
    }
}