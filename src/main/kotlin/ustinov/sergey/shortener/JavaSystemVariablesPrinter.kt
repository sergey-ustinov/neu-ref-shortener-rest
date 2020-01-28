package ustinov.sergey.shortener

import org.slf4j.LoggerFactory
import java.text.NumberFormat

object JavaSystemVariablesPrinter {

    val logger = LoggerFactory.getLogger(JavaSystemVariablesPrinter::class.java)

    fun printJavaVariables() {
        val version = System.getProperty("java.version")
        val runtime = Runtime.getRuntime()
        val processors = runtime.availableProcessors()
        val format = NumberFormat.getInstance()
        val maxMemory = runtime.maxMemory()
        val freeMemory = runtime.freeMemory()
        val mb = 1024 * 1024

        logger.info("""
            
            
            ========================== JVM Info ==========================
            Java version is: $version
            Available processors: $processors
            Max memory: ${format.format(maxMemory / mb)} MB
            Free memory: ${format.format(freeMemory / mb)} MB
            ==============================================================

        """.trimIndent())
    }
}
