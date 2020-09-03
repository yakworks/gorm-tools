package gorm;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

/**
 * Just a helper to load config during AST
 */
public class AuditStampConfigLoader {
    private static final String APP_GROOVY = "application.groovy";
    private static final String CONFIG_GROOVY = "audit-trail-config.groovy";

    static ConfigObject _CO = null;

    /**
     * Load order
     * 1. grails-app/conf/audit-trail-config.groovy
     * 2. classpath: audit-trail-config.groovy
     * 3. grails-app/conf/application.groovy
     *
     * @return config
     */
    public ConfigObject load() {
        if (_CO != null) return _CO;
        else {
            File file = loadFileFromConfigDir(CONFIG_GROOVY); // conf/audit-trail-config.groovy
            if (file.exists()) _CO = new ConfigSlurper().parse(getContents(file));
            else {
                URL in = getClass().getResource("/" + CONFIG_GROOVY); //from classpath
                if (in != null) {
                    _CO = new ConfigSlurper().parse(in);
                } else {
                    file = loadFileFromConfigDir(APP_GROOVY);
                    _CO = new ConfigSlurper().parse(getContents(file));
                }
            }
        }
        return _CO;
    }

    /*
     * Loads the given file from grails-app/conf directory
     * See README for module.path
     */
    private File loadFileFromConfigDir(String name) {
        //need the module.path - when the plugin is used as inplace.
        String modulePath = System.getProperty("module.path");
        String configPath;
        if (modulePath != null) configPath = modulePath + "/grails-app/conf/" + name;
        else configPath = "grails-app/conf/" + name;
        return new File(configPath);
    }

    private static String getContents(File aFile) {
        StringBuilder contents = new StringBuilder();

        try {
            //use buffering, reading one line at a time
            //FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return contents.toString();
    }
}
