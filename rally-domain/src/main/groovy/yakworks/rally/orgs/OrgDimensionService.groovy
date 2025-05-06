/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs

import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.OrgType

/**
 * See docs/design-principles/org-members-dimensions.md
 * Organization types are arranged in hierarchie, and configured in AppSetupConfig.
 *
 * OrgDimensionService provides methods to parse the orgdimension level hierarchie and get children, parents and
 * immediate parent orgtypes for any orgtype.
 */
@Service @Lazy
@Slf4j
@CompileStatic
class OrgDimensionService {

    @Autowired OrgProps orgProps

    @Autowired(required = false) //optional so integration tests dont need it setup to test
    CacheManager cacheManager

    protected boolean isInitialized = false

    @CompileStatic
    static enum DimLevel {
        PARENTS, CHILDREN
    }

    /*
     DimensionLevel instances created by parsing the paths specified in config are cached here.
     DimensionLevel contains just immediate parents and children not all children and parents recursive.
    */
    private final Map<OrgType, DimensionLevel> dimensionsCache = new ConcurrentHashMap<OrgType, DimensionLevel>()

    //all levels configured in the dimensions
    private final List<OrgType> allLevels = []

    /**
     * Parse dimension levels from appsetup config and cache on server startup,
     * but it can be called manually afterwards for testing, and it will reset the cache and repopulate it based
     * on the current appsetup config.
     */
    @PostConstruct
    void init() {
        if(isInitialized) return
        clearCache()
        if(orgProps.members.enabled){
            initDims()
        }
        isInitialized = true
    }

    /**
     * Get all parent levels for given orgtype
     */
    @Cacheable('OrgDimension.parentLevels')
    List<OrgType> getParentLevels(OrgType type) {
        return getLevels(DimLevel.PARENTS, type)
    }

    /**
     * Get all child levels for given orgtype
     * NOT USED OUTSIDE TESTS RIGHT NOW
     */
    @Cacheable('OrgDimension.childLevels')
    List<OrgType> getChildLevels(OrgType typeEnum) {
        return getLevels(DimLevel.CHILDREN, typeEnum)
    }

    /**
     * Get immediate parents for given orgtype
     * For example, given org dimensions
     *   - CustAccount.Branch.Division
     *   - CustAccount.Customer.Division
     * then getImmediateParents(CustAccount) will return [Branch,Customer]
     * and getImmediateParents(Customer) and getImmediateParents(Branch) will both return [Division]
     * and getImmediateParents(Division) would return and empty list meaning its top level
     */
    List<OrgType> getImmediateParents(OrgType type) {
        if(!isInitialized) init()
        DimensionLevel level = dimensionsCache[type]
        if (!level) return []
        Set<DimensionLevel> parents = level.parents
        List<OrgType> parentList = parents*.orgType
        return parentList ?: [] as List<OrgType>
    }

    protected void initDims(){
        initDimensions(orgProps.members.dimension)
        //if dimension2 is set then do that one too
        initDimensions(orgProps.members.dimension2)
        createClientCompanyDimLevels()
    }

    protected void initDimensions(List<OrgType> typePath) {
        if(!typePath) return
        typePath = typePath.reverse() //reverse it so the top starts first
        OrgType previousType
        for (OrgType otype : typePath) {
            DimensionLevel dlevel = getOrCreateCachedDimensionLevel(otype)
            if (previousType) {
                DimensionLevel parent = getOrCreateCachedDimensionLevel(previousType)
                parent.addChild(dlevel)
            }
            previousType = otype
        }
    }

    protected void clearCache(){
        dimensionsCache.clear()
        allLevels.clear()
        cacheManager?.getCache("OrgDimension.parentLevels")?.clear()
        cacheManager?.getCache("OrgDimension.childLevels")?.clear()
    }

    protected List<OrgType> getLevels(DimLevel dimLevel, OrgType orgType) {
        DimensionLevel dimensionLevel = dimensionsCache[orgType]
        if (!dimensionLevel) return []

        def levels = [] as List<OrgType>

        if (dimLevel == DimLevel.CHILDREN) {
            dimensionLevel.children.each {
                levels.add(it.orgType)
                levels.addAll(getLevels(DimLevel.CHILDREN, it.orgType))
            }
        } else if (dimLevel == DimLevel.PARENTS) {
            dimensionLevel.parents.each {
                levels.add(it.orgType)
                levels.addAll(getLevels(DimLevel.PARENTS, it.orgType))
            }
        }
        return levels.unique().collect()
    }

    /**
     * All levels configured in dimension paths
     * USED ONLY IN TESTS RIGHT NOW
     * @return List < OrgType >
     */
    protected List<OrgType> getAllLevels() {
        return allLevels
    }

    /**
     * Find if a DimensionLevel is already created for given name and cached in dimensionsCache, or create a new and
     * cache it.
     */
    protected DimensionLevel getOrCreateCachedDimensionLevel(OrgType orgType) {
        DimensionLevel dlevel = dimensionsCache.get(orgType)
        if (!dlevel) {
            dlevel = new DimensionLevel(orgType)
            dimensionsCache[orgType] = dlevel
            allLevels << orgType
        }
        return dlevel
    }

    /**
     * create the Company and Client dimension levels
     */
    protected void createClientCompanyDimLevels() {
        createClientCompanyDimLevel(OrgType.Client)
        createClientCompanyDimLevel(OrgType.Company)
    }

    /**
     * Find if a DimensionLevel is already created for given name and cached in dimensionsCache, or create a new and
     * cache it.
     */
    protected DimensionLevel createClientCompanyDimLevel(OrgType orgType) {
        DimensionLevel dlevel = dimensionsCache.get(orgType)
        if (!dlevel) {
            dlevel = new DimensionLevel(orgType)
            allLevels.each {
                def childDim = getOrCreateCachedDimensionLevel(it)
                dlevel.children.add(childDim)
            }
            dimensionsCache[orgType] = dlevel
        }
        return dlevel
    }

    /**
     * Data structure to hold a dimension level and its immediate parents and children.
     */
    @CompileStatic
    protected class DimensionLevel {
        OrgType orgType
        Set<DimensionLevel> parents = [] as Set<DimensionLevel>
        Set<DimensionLevel> children = [] as Set<DimensionLevel>

        DimensionLevel(OrgType orgType){
            this.orgType = orgType
        }
        void addChild(DimensionLevel child) {
            if (!children.contains(child)) {
                children.add(child)
                if (!child.parents.contains(this)) {
                    child.parents.add(this)
                }
            }
        }
    }

}
