/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.openapi


import java.nio.file.Files
import java.nio.file.Path

import groovy.transform.CompileStatic

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

/**
 * Helper to dump yml to file
 */
@CompileStatic
class YamlUtils {

    static void saveYaml(Path path, Object yml){
        DumperOptions dops = new DumperOptions()
        dops.indent = 2
        dops.prettyFlow = true
        dops.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        //dops.width = 120
        Yaml yaml = new Yaml(dops)
        yaml.dump(yml, new FileWriter(path.toString()))
        // path.toFile().withWriter {Writer writer ->
        //     yaml.dump(yml, writer)
        // }

    }

    static Object loadYaml(Path path){
        Yaml yaml = new Yaml()
        yaml.load(Files.newInputStream(path))
    }

}
