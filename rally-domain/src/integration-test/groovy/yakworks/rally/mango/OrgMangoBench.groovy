package yakworks.rally.mango


import gorm.tools.mango.MangoBuilder
import gorm.tools.mango.MangoTidyMap
import gorm.tools.mango.api.QueryArgs
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
            assert i >= 0
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    static void findWithHQL() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def org = Org.executeQuery("from Org where name='Org23'")[0]
            assert org
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    static void findByName() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def org = Org.findByName("Org23")
            assert org
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    static void findWhere() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def org = Org.findWhere(name: "Org23")
            assert org
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    //this should be the same as the mangoOrgQuery
    void mangoBuilderBuild() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def crit = mangoBuilder.build(Org, QueryArgs.of([name: "Org23"]), null)
            assert crit != null
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    static void mangoOrgQuery() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def crit = Org.repo.query([name: "Org23"])
            assert crit != null
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    static void mangoOrgQueryWithRepo() {
        var repo = Org.repo
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def org = repo.query([name: "Org23"])
            assert org != null
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    //closure much faster than map, 1/2 second vs 5 seconds with map
    static void mangoOrgQueryWithClosure() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def crit = Org.query{
                eq "name", "Org23"
            }
            assert crit != null
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    static void mangoOrgQueryGetWithMap() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def org = Org.query([name: "Org23"]).get()
            assert org
        }
        println BenchmarkHelper.endTimeMsg("For loop", start)
    }

    static void mangoOrgQueryGetWithClosure() {
        def start = BenchmarkHelper.startTime()
        for (int i = 0; i < cnt; i++) {
            def org = Org.query{
                eq "name", "Org23"
            }.get()
            assert org
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
