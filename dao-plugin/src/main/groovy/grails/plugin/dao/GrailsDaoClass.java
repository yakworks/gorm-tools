package grails.plugin.dao;

import grails.core.InjectableGrailsClass;

/**
 * @author Joshua Burnett
 * based on the grails source GrailsServiceClass
 */
public interface GrailsDaoClass extends InjectableGrailsClass {

    String DATA_SOURCE = "datasource";
    String DEFAULT_DATA_SOURCE = "DEFAULT";
    String ALL_DATA_SOURCES = "ALL";

    /**
     * Service should be configured with transaction demarcation.
     *
     * @return configure with transaction demarcation
     */
    boolean isTransactional();

    /**
     * Get the datasource name that this service class works with.
     * @return the name
     */
    String getDatasource();

    /**
     * Check if the service class can use the named DataSource.
     * @param name the name
     * @return true if uses
     */
    boolean usesDatasource(String name);

}
