import ch.qos.logback.core.joran.spi.ConsoleTarget
import ch.qos.logback.core.util.FileSize

def environment = System.getenv().getOrDefault("ENVIRONMENT", "dev")

def defaultLevel = INFO
def defaultTarget = ConsoleTarget.SystemErr

if (environment == "dev") {
    defaultLevel = DEBUG
    defaultTarget = ConsoleTarget.SystemOut

    // Silence warning about missing native PRNG
    logger("io.ktor.util.random", ERROR)
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%boldGreen(%d{yyyy-MM-dd}) %boldYellow(%d{HH:mm:ss}) %gray(|) %highlight(%5level) %gray(|) %boldMagenta(%40.40logger{40}) %gray(|) %msg%n"

        withJansi = true
    }

    target = defaultTarget
}

def fileName = "./bot.log"

appender("FILE", RollingFileAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd}%d{HH:mm:ss} | %5level | %40.40logger{40} | %msg%n"
    }

    file = fileName

    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${fileName}.%d{yyyy-MM-dd}.gz"

        maxHistory = 14
        totalSizeCap = FileSize.valueOf("3 gb")
    }
}

root(defaultLevel, ["CONSOLE", "FILE"])
logger("org.mongodb.driver", INFO)
