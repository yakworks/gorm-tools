scan("5 seconds")
println "#### using external app/logback.grovy"
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = java.nio.charset.Charset.forName('UTF-8')
        pattern = '%d{HH:mm:ss.SSS} [%t] %-5level %logger{48} - %msg%n'
    }
}
root(ERROR, ['STDOUT'])
logger("grails.app", INFO, ['STDOUT'], false)
logger("nine", INFO, ['STDOUT'], false)
