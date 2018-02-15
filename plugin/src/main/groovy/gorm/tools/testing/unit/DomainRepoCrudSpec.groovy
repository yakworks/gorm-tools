package gorm.tools.testing.unit

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * executes a series automatic "sanity" checks on the domain and repo for CRUD.
 * Allow methods to be overriden for tweaks to build the binding map and asserts
 */
//@CompileStatic
abstract class DomainRepoCrudSpec<D> extends Specification implements DomainRepoTest<D> {
//order on the above Traits is important as both have mockDomains and we want the one in DataRepoTest to be called
    @Shared Map ignore = [:]

//    def "create tests"() {
//        when:
//            D ent = createEntity(params)
//        then:
//            assertsCall "assertCreate", ent
//            if(expected) assert subsetEquals(expected, ent.properties)
//        where:
//            dataPipes << ensureList(whereCreate())
//            expected = dataPipes.expected
//            params = dataPipes.params
//    }

    def "create tests"() {
        expect:
            testCreate()
    }

    //override this to customize or disable
    void testCreate(){
        assert createEntity().id
    }

    def "update tests"() {
        expect:
            testUpdate()
    }

    //override this to customize or disable
    void testUpdate(){
        assert updateEntity().version > 0
    }

    def "persist tests"() {
        expect:
            testPersist()
    }

    //override this to customize or disable
    void testPersist(){
        assert persistEntity().id
    }

    def "remove tests"() {
        expect:
            testRemove()
    }

    //override this to customize or disable
    void testRemove(){
        assert persistEntity().id
    }

    /************************ Helpers Methods *************/

    Map buildCreateMap(Map args) {
        buildMap(args)
    }

    Map buildUpdateMap(Map args) {
        buildMap(args)
    }

    D buildPersist(Map args) {
        args['save'] = false
        build(args)
    }

    def whereCreate() {
        [params: [:], expected: [:]]
    }

    def whereUpdate() {
        [params: [:], expected: [:]]
    }

    D get(id){
        flushAndClear()
        def ret = entityClass.get(id)
        assert ret
        return ret
    }

    D createEntity(Map args = [:]){
        D instance = entityClass.create(buildCreateMap(args))
        return get(instance.id)
    }

    D updateEntity(Map args = [:]){
        def id = args.id ? args.remove('id') : createEntity().id
        Map updateMap = buildUpdateMap(args)
        updateMap.id = id
        assert entityClass.update(updateMap)
        return get(id)
    }

    D persistEntity(Map args = [:]){
        D instance = buildPersist(args)
        assert instance.persist()
        return get(instance.id)
    }

    @Ignore
    def removeEntity(){
        //def id = createEntity().id
        def id = build(save: true).id
        flushAndClear()
        def ge = get(id)
        ge.remove()
        flushAndClear()
        assert entityClass.get(id) == null
        return id
    }

    void assertsCall(String method, obj){
        //if (this.metaClass.respondsTo(this, method, D)) {
        if (this.metaClass.respondsTo(this, method)) {
            "$method"(obj)
        }
    }

    /** makes sure the passed in object is a list, if not then it wraps it in one
     * helpful when creating spocks data pipes */
    List ensureList(obj){
        obj instanceof List ? obj : [obj]
    }

    /**
     * Loosely test 2 maps for equality
     * asserts more or less that subset:[a: 1, b: 2] == full:[a: 1, b: 2, c: 3] returns true
     * if subset is an empty map or null returns false
     *
     * @param subset the full map
     * @param full the full map
     * http://csierra.github.io/posts/2013/02/12/loosely-test-for-map-equality-using-groovy/
     */
    boolean subsetEquals(Map subset, Map full, List<String> exclude=[]) {
        println "subset: $subset"
        println "full: $full"
        if(!subset) return false
        //if (!full.keySet().containsAll(subset.keySet())) return false
        return subset.findAll{!exclude.contains(it.key)}.every {  it.value == full[it.key]}
    }

}
