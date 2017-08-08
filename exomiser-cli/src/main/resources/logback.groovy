
appender("FILE", FileAppender) {
  file = "logs/exomiser.log"
  append = true
  encoder(PatternLayoutEncoder) {
    pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n"
  }
}

appender("CONSOLE", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n"
  }
}

logger("org.monarchinitiative.exomiser", INFO)
logger("org.thymeleaf", ERROR)

root(INFO, ["CONSOLE", "FILE"])