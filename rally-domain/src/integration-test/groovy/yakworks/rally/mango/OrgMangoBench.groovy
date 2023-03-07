package yakworks.rally.mango

import groovy.transform.CompileStatic

import gorm.tools.mango.MangoBuilder
import gorm.tools.mango.MangoTidyMap
import gorm.tools.utils.BenchmarkHelper
import grails.compiler.GrailsCompileStatic
import yakworks.rally.orgs.model.Org

/**
 * Shows the
 */
@GrailsCompileStatic
class OrgMangoBench{

    MangoBuilder mangoBuilder
    static int cnt = 100000

    static void forLoopBaseLine() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            print(i)
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    static void mangoOrgFindBy() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def org = Org.findByName("Org23")
            assert org
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    static void mangoOrgQuery() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def org = Org.query([name: "Org23"])
            assert org != null
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    static void mangoOrgGet() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def org = Org.query([name: "Org23"]).get()
            assert org
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    void mangoBuilder() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def crit = mangoBuilder.build(Org, [name: "Org23"], null)
            assert crit != null
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    static void tidyMap() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def tidyMap = MangoTidyMap.tidy([name: "Org23"] as Map<String, Object>)
            assert tidyMap
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }


}
