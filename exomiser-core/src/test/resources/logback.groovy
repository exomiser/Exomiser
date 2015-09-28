appender("CONSOLE", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n"
  }
}

logger("de.charite.compbio.exomiser", INFO)
logger("org.thymeleaf", ERROR)

root(INFO, ["CONSOLE"])