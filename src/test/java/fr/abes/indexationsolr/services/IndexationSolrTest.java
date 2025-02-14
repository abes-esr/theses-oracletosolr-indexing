package fr.abes.indexationsolr.services;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static fr.abes.indexationsolr.services.IndexationSolr.readFile;

public class IndexationSolrTest extends TestCase {

    private String urlSolr = "http://denim.v202.abes.fr:8080/solrSujets/update";

    public void testIndexerDansSolr() throws IOException {

        IndexationSolr indexationSolr = new IndexationSolr(
                "",
                "",
                123796,
                "");

        indexationSolr.indexerDansSolr(123820,
                readFile("src/test/nok2.xml", StandardCharsets.UTF_8));

        final StringWriter sw = new StringWriter();
        indexationSolr.postData(new StringReader(readFile("src/test/okTransfo.xml", StandardCharsets.UTF_8)),
                sw,
                "http://denim.v102.abes.fr:8080/solrSujets/update");


    }

    public void testSupprimerDeSolr() {
    }
}