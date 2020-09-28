package fr.abes.indexationsolr.config;

import org.hibernate.dialect.Oracle12cDialect;


public class CustomOracle12cDialect extends Oracle12cDialect {

    @Override
    public boolean useInputStreamToInsertBlob() {
        //This forces the use of CLOB binding when inserting
        return false;
    }
}

