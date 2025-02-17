package fr.abes.indexationsolr.services;

import static jdk.nashorn.internal.objects.Global.println;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.TransformerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class IndexationSolrTest {

    int iddoc = 123796;
    String cheminXsl = "src/main/resources/xsl/sujets2solr.xsl";
    String urlSolr = "http://denim-test.v202.abes.fr:8080/solrSujets";

    @Mock
    private Logger logger;

    @InjectMocks
    private IndexationSolr indexationSolr; // Remplace par le vrai nom de ta classe

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        IndexationSolr indexer = new IndexationSolr();
        indexer.supprimerDeSolr(iddoc, urlSolr + "/update");
    }

    @After
    public void tearDown() throws IOException {
        IndexationSolr indexer = new IndexationSolr();
        indexer.supprimerDeSolr(iddoc, urlSolr + "/update");
    }

    public String selectDocDansSolr(int idDoc, String urlSolr) throws IOException {
        HttpURLConnection urlc = null;
        URL url = new URL(urlSolr + "/select/?q=id:" + idDoc);
        urlc = (HttpURLConnection) url.openConnection();
        urlc.setRequestMethod("GET");
        System.out.println(urlc.getContent());
        BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    @Test
    public void indexerDansSolr_Success() throws Exception {
        // Indexe dans Solr
        IndexationSolr indexer = new IndexationSolr();
        String docOkXML = IndexationSolr.readFile("src/test/resources/ok.xml", StandardCharsets.UTF_8);
        Boolean response = indexer.indexerDansSolr(iddoc,
                docOkXML,
                cheminXsl,
                urlSolr + "/update");
        assertTrue(response);

        // Vérifie l'indexation
        String response2 = selectDocDansSolr(iddoc, urlSolr);
        assertTrue(response2.contains("<result name=\"response\" numFound=\"1\" start=\"0\">"));
        assertTrue(response2.contains("<str name=\"id\">" + iddoc + "</str>"));

        // Supprime et vérifie
        indexer.supprimerDeSolr(iddoc, urlSolr + "/update");
        String response3 = selectDocDansSolr(iddoc, urlSolr);
        assertTrue(response3.contains("<result name=\"response\" numFound=\"0\" start=\"0\"/>"));
        assertFalse(response3.contains("<str name=\"id\">" + iddoc + "</str>"));
    }
}
