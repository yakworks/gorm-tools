/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing

import gorm.tools.beans.Pager
import gorm.tools.testing.hibernate.GormToolsHibernateSpec

class PagerSpec extends GormToolsHibernateSpec {

    def "test default values"() {
        when:
        Pager pager = new Pager()

        then: 'defaults should be as follows'
        pager.max == 20
        pager.page == 1
        pager.recordCount == 0
        pager.data == null

    }

}
