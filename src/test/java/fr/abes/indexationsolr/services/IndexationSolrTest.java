package fr.abes.indexationsolr.services;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.TransformerException;

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

    @Mock
    private Logger logger;

    @InjectMocks
    private IndexationSolr indexationSolr; // Remplace par le vrai nom de ta classe

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void indexerDansSolr_Success() throws Exception {
        // Arrange
        int iddoc = 123;
        String tef = "<xml>test</xml>";
        String cheminXsl = "src/main/resources/xsl/tef2solr.xsl";
        String urlSolr = "http://denim.v202.abes.fr:8080/solrSujets/update";

        IndexationSolr indexer = new IndexationSolr();

        // Act
        //boolean result = indexer.indexerDansSolr(iddoc, tef, cheminXsl, urlSolr);

        // Assert
        //assertTrue(result);
        //verify(indexer).transfoXSL(tef, iddoc, cheminXsl);

        String docOkXML = IndexationSolr.readFile("src/test/resources/ok.xml", StandardCharsets.UTF_8);
        indexer.indexerDansSolr(123796,
                docOkXML,
                cheminXsl,
                urlSolr);

        assertTrue();
    }

    @Test
    public void indexerDansSolr_ThrowsTransformerException() throws Exception {
        // Arrange
        int iddoc = 123;
        String tef = "<xml>test</xml>";
        String cheminXsl = "/path/to/xsl";
        String urlSolr = "http://localhost:8983/solr";

        IndexationSolr spyIndexer = spy(indexationSolr);

        doThrow(new TransformerException("Transformation error")).when(spyIndexer).transfoXSL(tef, iddoc, cheminXsl);

        // Act & Assert
        try {
            spyIndexer.indexerDansSolr(iddoc, tef, cheminXsl, urlSolr);
            fail("Expected TransformerException");
        } catch (TransformerException e) {
            assertEquals("Transformation error", e.getMessage());
        }

        verify(spyIndexer).transfoXSL(tef, iddoc, cheminXsl);
        verify(spyIndexer, never()).envoieSurSolr(anyString(), anyString());
    }

    @Test
    public void indexerDansSolr_ThrowsIOException() throws Exception {
        // Arrange
        int iddoc = 123;
        String tef = "<xml>test</xml>";
        String cheminXsl = "/path/to/xsl";
        String urlSolr = "http://localhost:8983/solr";

        String transformedXml = "<solr><doc>test</doc></solr>";
        IndexationSolr spyIndexer = spy(indexationSolr);

        doReturn(transformedXml).when(spyIndexer).transfoXSL(tef, iddoc, cheminXsl);
        doThrow(new IOException("Solr unreachable")).when(spyIndexer).envoieSurSolr(transformedXml, urlSolr);

        // Act & Assert
        try {
            spyIndexer.indexerDansSolr(iddoc, tef, cheminXsl, urlSolr);
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("Solr unreachable", e.getMessage());
        }

        verify(spyIndexer).transfoXSL(tef, iddoc, cheminXsl);
        verify(spyIndexer).envoieSurSolr(transformedXml, urlSolr);
    }
}
