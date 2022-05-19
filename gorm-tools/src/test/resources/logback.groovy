import grails.util.BuildSettings
import grails.util.Environment

// // See http://logback.qos.ch/manual/groovy.html for details on configuration
// appender('STDOUT', ConsoleAppender) {
//     encoder(PatternLayoutEncoder) {
//         pattern = "%level %logger - %msg%n"
//     }
// }

// def targetDir = BuildSettings.TARGET_DIR
// if (Environmentzzz.isDevelopmentMode() && targetDir != null) {
//     appender("FULL_STACKTRACE", FileAppender) {
//         file = "${targetDir}/stacktrace.log"
//         append = true
//         encoder(PatternLayoutEncoder) {
//             pattern = "%level %logger - %msg%n"
//         }
//     }
//     logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
//     root(ERROR, ['STDOUT', 'FULL_STACKTRACE'])
// } else {
//     root(ERROR, ['STDOUT'])
// }
root(DEBUG)
logger("org.grails.datastore.mapping.core", OFF)
logger 'org.hibernate.SQL', DEBUG

// this one is very noisy but if neede will show the values passed to sql statments
logger 'org.hibernate.type.descriptor.sql.BasicBinder', TRACE
