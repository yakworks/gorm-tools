/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.audit.ast

import groovy.transform.CompileStatic

/**
 * Just a helper to load config during AST
 */
@CompileStatic
class AuditStampConfigLoader {
    private static final String APP_GROOVY = "application.groovy"
    private static final String CONFIG_GROOVY = "audit-trail-config.groovy"

    ConfigObject stampConfig = null

    /**
     * Load order
     * 1. grails-app/conf/audit-trail-config.groovy
     * 2. classpath: audit-trail-config.groovy
     * 3. grails-app/conf/application.groovy
     *
     * @return config
     */
    ConfigObject load() {
        if (!stampConfig ) {
            File file = loadFileFromConfigDir(CONFIG_GROOVY) // conf/audit-trail-config.groovy
            if (file.exists()) {
                stampConfig = new ConfigSlurper().parse(getContents(file))
            }
            else {
                URL cfgIn = getClass().getResource("/" + CONFIG_GROOVY) //from classpath
                if (cfgIn) {
                    stampConfig = new ConfigSlurper().parse(cfgIn)
                } else {
                    file = loadFileFromConfigDir(APP_GROOVY)
                    String cnts = getContents(file)
                    stampConfig = new ConfigSlurper().parse(cnts)
                }
            }
        }
        return stampConfig
    }

    /*
     * Loads the given file from grails-app/conf directory
     * See README for module.path
     */
    private File loadFileFromConfigDir(String name) {
        String modulePath = System.getProperty("gradle.projectDir")
        //assert modulePath
        String configPath
        if (modulePath != null){
            configPath = modulePath + "/grails-app/conf/" + name
        }
        else {
            configPath = "grails-app/conf/" + name
        }
        return new File(configPath)
    }

    @SuppressWarnings(['PrintStackTrace'])
    private static String getContents(File aFile) {
        StringBuilder contents = new StringBuilder()

        try {
            //use buffering, reading one line at a time
            //FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(aFile))
            try {
                String line = null
                while ((line = input.readLine()) != null) {
                    contents.append(line)
                    contents.append(System.getProperty("line.separator"))
                }
            } finally {
                input.close()
            }
        } catch (IOException ex) {
            ex.printStackTrace()
        }
        return contents.toString()
    }
}
