package ustinov.sergey.shortener.auxiliary

import org.slf4j.LoggerFactory
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

class ExampleServletContextListener(private val disposableBeans: List<Disposable>) : ServletContextListener {

    private val logger = LoggerFactory.getLogger(ExampleServletContextListener::class.java)

    override fun contextDestroyed(event: ServletContextEvent?) {
        logger.info("Performing shutdown hook on SpringContext")
        disposableBeans.forEach { it.dispose() }
    }

    override fun contextInitialized(event: ServletContextEvent?) {}
}