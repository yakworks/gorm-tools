/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.hibernate


import java.lang.reflect.Field
import java.text.SimpleDateFormat

import groovy.transform.CompileStatic

import org.hibernate.Criteria
import org.hibernate.engine.spi.LoadQueryInfluencers
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.internal.CriteriaImpl
import org.hibernate.internal.SessionImpl
import org.hibernate.loader.OuterJoinLoader
import org.hibernate.loader.criteria.CriteriaLoader
import org.hibernate.loader.criteria.CriteriaQueryTranslator
import org.hibernate.persister.entity.OuterJoinLoadable

/**
 * WIP to generate the rough sql
 * see https://stackoverflow.com/questions/554481/how-to-get-sql-from-hibernate-criteria-api-not-for-logging
 * https://stackoverflow.com/questions/14358934/how-to-get-the-jpa-generated-sql-query
 * https://antoniogoncalves.org/2012/05/24/how-to-get-the-jpqlsql-string-from-a-criteriaquery-in-jpa/
 */
@SuppressWarnings(["ThrowRuntimeException", "SimpleDateFormatMissingLocale", "AddEmptyString", "UnnecessaryDotClass"])
@CompileStatic
class HibernateCriteriaToSql {

    public static String toSql(Criteria criteria) {
        String sql = "";
        Object[] parameters = null;
        try {
            CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
            SessionImpl sessionImpl = (SessionImpl) criteriaImpl.getSession();
            SessionFactoryImplementor factory = sessionImpl.getSessionFactory();
            String[] implementors = factory.getImplementors(criteriaImpl.getEntityOrClassName());
            OuterJoinLoadable persister = (OuterJoinLoadable) factory.getEntityPersister(implementors[0]);
            LoadQueryInfluencers loadQueryInfluencers = new LoadQueryInfluencers();
            CriteriaLoader loader = new CriteriaLoader(persister, factory,
                criteriaImpl, implementors[0].toString(), loadQueryInfluencers);
            Field f = OuterJoinLoader.class.getDeclaredField("sql");
            f.setAccessible(true);
            sql = (String) f.get(loader);
            Field fp = CriteriaLoader.class.getDeclaredField("translator");
            fp.setAccessible(true);
            CriteriaQueryTranslator translator = (CriteriaQueryTranslator) fp.get(loader);
            parameters = translator.getQueryParameters().getPositionalParameterValues();
        }
        catch (Exception e) {
            throw new RuntimeException(e)
        }
        if (sql != null) {
            int fromPosition = sql.indexOf(" from ");
            sql = "\nSELECT * " + sql.substring(fromPosition);

            if (parameters != null && parameters.length > 0) {
                for (Object val : parameters) {
                    String value = "%";
                    if (val instanceof Boolean) {
                        value = ((Boolean) val) ? "1" : "0";
                    }
                    else if (val instanceof String) {
                        value = "'" + val + "'";
                    }
                    else if (val instanceof Number) {
                        value = val.toString();
                    }
                    else if (val instanceof Class) {
                        value = "'" + ((Class) val).getCanonicalName() + "'";
                    }
                    else if (val instanceof Date) {
                        SimpleDateFormat sdf = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss.SSS");
                        value = "'" + sdf.format((Date) val) + "'";
                    }
                    else if (val instanceof Enum) {
                        value = "" + ((Enum) val).ordinal();
                    }
                    else {
                        value = val.toString();
                    }
                    sql = sql.replaceFirst("\\?", value);
                }
            }
        }
        return sql.replaceAll("left outer join", "\nleft outer join").replaceAll(
            " and ", "\nand ").replaceAll(" on ", "\non ").replaceAll("<>",
            "!=").replaceAll("<", " < ").replaceAll(">", " > ");
    }

}
