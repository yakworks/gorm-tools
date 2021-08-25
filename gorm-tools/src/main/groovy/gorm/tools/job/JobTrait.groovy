/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import gorm.tools.source.SourceTrait
import groovy.transform.CompileStatic

@CompileStatic
trait JobTrait implements SourceTrait {

    Boolean ok = false // change to TRUE if State.Finished without any issues
    JobState state


}
