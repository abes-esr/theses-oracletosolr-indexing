package fr.abes.indexationsolr.services;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Getter @Setter
@NoArgsConstructor
public class IndexationSolr {

    private Logger logger = LogManager.getLogger(IndexationSolr.class);

    private String urlSolrHighlight;
    private String urlSolrPersonne;
    private int iddoc;
    private String tef;

    public static void main(String[] args) throws IOException, TransformerException {
        IndexationSolr indexationSolr = new IndexationSolr(
                "",
                "",
                123796,
                "");

        /*indexationSolr.indexerDansSolr(123820,
                readFile("src/test/nok2.xml", StandardCharsets.UTF_8));*/

        final StringWriter sw = new StringWriter();
        indexationSolr.postData(new StringReader(readFile("src/test/okTransfo.xml", StandardCharsets.UTF_8)),
                sw,
                "http://denim.v102.abes.fr:8080/solrSujets/update");
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public IndexationSolr(String urlSolrHighlight, String urlSolrPersonne, int iddoc, String tef) {
        this.urlSolrHighlight = urlSolrHighlight;
        this.urlSolrPersonne = urlSolrPersonne;
        this.iddoc = iddoc;
        this.tef = tef;
    }

    public boolean indexerDansSolr(int iddoc, String tef, String cheminXsl, String urlSolr) throws TransformerException, IOException {
        boolean res;
        try {
            String docSolr = transfoXSL(tef, iddoc, cheminXsl);
            envoieSurSolr(docSolr, urlSolr);
            res = true;
        } catch (Exception e) {
            logger.info("Erreur dans indexerDansSolr :"+e.getMessage());
            throw e;
        }
        return res;
    }


    public boolean supprimerDeSolr(int idDoc, String urlSolrt) throws IOException {
        boolean res = false;
        final StringWriter sw = new StringWriter();
        try
        {
            postData(new StringReader("<delete><id>" + idDoc + "</id></delete>"), sw, urlSolrt);
            if (sw.toString().indexOf("<int name=\"status\">0</int>") < 0) {
                logger.info("unexpected response from solr...");
            }
            postData(new StringReader("<commit/>"), sw, urlSolrt);
            res = true;
        }
        catch (Exception e) {
            logger.info("Erreur dans supprimerDeSolr :"+e.getMessage());
            throw e;
        }
        return res;
    }


    /**
     * Pour la transformation XSL :
     * chargement de saxon dans oracle
     * et utilisation de net.sf.saxon.TransformerFactoryImpl
     * note : il faut utiliser un chemin de fichier sans le protocole (file://C:\\)
     * pour saxon pour charger l'xsl
     * note: il faut preciser que le tef (xmltype) est encode en utf-8 = InputStreamReader(tef.getInputStream(), "UTF-8"))
     */
    public String transfoXSL(String tef, int idDoc, String cheminXslt) throws TransformerException {
        InputStream stream = new ByteArrayInputStream(tef.getBytes(StandardCharsets.UTF_8));
        TransformerFactory tFactory = new net.sf.saxon.TransformerFactoryImpl();
        Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(cheminXslt));
        transformer.setParameter("idDeLaBase", idDoc);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.transform(new javax.xml.transform.stream.StreamSource(stream), new javax.xml.transform.stream.StreamResult(out));
        return out.toString();
    }




    //-----------------------------------pour Portail------------------------------------//



    // ************************* SOLR HIGHLIGHT ********************************
    private void decouperIndexerSolrHighlight(String texte, int idDoc, int longueurPage)  {
        logger.info("IndexationSolrDansOraclePortail decouperIndexerSolrHighlight debut de decoupage du texte + envoi sur solrhighlight");
        logger.info("texte = " + texte);
        logger.info("iddoc = " + idDoc);
        logger.info("longueurPage = " + longueurPage);
        try {
            int numPage = 1;
            String idNumPage = idDoc + "_" + numPage;
            int borneInf = 0;
            int borneSup = longueurPage;
            while (borneInf + longueurPage < texte.length()) {
                logger.info("inf = " + borneInf);
                logger.info("sup = " + borneSup);

                String page = texte.substring(borneInf, borneSup);
                int point = page.lastIndexOf('.');
                if (point != -1) {
                    page = page.substring(0, point + 1);
                    point++;
                    borneSup = borneSup - (longueurPage - point);
                }
                idNumPage = idDoc + "_" + numPage;
                String docSolr = getDocSolrHighlight(idNumPage, idDoc, page);

                envoieSurSolr(docSolr, urlSolrHighlight);
                borneInf = borneSup;
                borneSup = borneSup + longueurPage;
                numPage++;
            }
            String dernierePage = texte.substring(borneInf);
            String docSolr = getDocSolrHighlight(idNumPage, idDoc, dernierePage);
            envoieSurSolr(docSolr, urlSolrHighlight);
            logger.info("Fin de decouperIndexerSolrHighlight iddoc="+idDoc);
        } catch (Exception e) {
            logger.error("ERREUR DANS decouperIndexer iddoc=" + idDoc + " msg:" + e.getMessage());
            //throw e; //Pr voir si ca continue...
        }
        logger.info("sortie de indexationSolrDansOraclePortail decouperIndexerSolrHighlight");
    }

    public String getDocSolrHighlight(String idNumPage, int idDoc, String extrait) {
        return "<add><doc>"
                + "<field name=\"idNumPage\">"
                + idNumPage
                + "</field>"
                + "<field name=\"texteHL\">"
                + extrait
                + "</field>"
                + "<field name=\"id\">"
                + idDoc
                + "</field>"
                + "</doc></add>";
    }
    // ************************* SOLR HIGHLIGHT FIN ********************************


    //-----------------------------------fin methodes pour Portail------------------------------------//

    public void envoieSurSolr(String docSolr, String urlSolr) throws IOException {
        final StringWriter sw = new StringWriter();
        logger.info("envoieSurSolr => urlSolr = " + urlSolr);
        postData(new StringReader(docSolr), sw, urlSolr);
        logger.info( "sw.toString() = " + sw.toString());
        logger.info( "res sw = " + sw.toString().indexOf("<int name=\"status\">0</int>"));
        if (sw.toString().indexOf("<int name=\"status\">0</int>") < 0) {
            logger.error("unexpected response from solr...");
        }
    }


    /**
     * Reads data from the data reader and posts it to solr,
     * writes to the response to output
     * @throws Exception
     */
    public void postData(Reader data, Writer output, String url) throws IOException {
        logger.info("postData");
        URL solrUrl = new URL(url);
        logger.info("url solr dans postData = " + url);
        HttpURLConnection urlc = null;
        try {
            urlc = (HttpURLConnection) solrUrl.openConnection();
            try {
                urlc.setRequestMethod("POST");
            } catch (ProtocolException e) {
                throw new ProtocolException("Shouldn't happen: HttpURLConnection doesn't support POST??");
            }
            urlc.setDoOutput(true);
            urlc.setDoInput(true);
            urlc.setUseCaches(false);
            urlc.setAllowUserInteraction(false);
            urlc.setRequestProperty("Content-type", "text/xml; charset=UTF-8");

            try (OutputStream out = urlc.getOutputStream()) {
                Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                pipe(data, writer);
                writer.close();
            } catch (IOException e) {
                throw new IOException("IOException while posting data", e);
            }

            try (InputStream in = urlc.getInputStream()) {
                Reader reader = new InputStreamReader(in);
                pipe(reader, output);
                reader.close();
            } catch (IOException e) {
                throw new IOException("IOException while reading response", e);
            }

        } catch (IOException e) {
            try {
                assert urlc != null;
                logger.info("Solr returned an error: " + urlc.getResponseMessage());
                throw new IOException("Erreur lors du post sur solr : "
                        + urlc.getResponseMessage(), e);
            } catch (IOException f) {
                logger.info("Connection error (is Solr running at " + solrUrl + " ?): " + e);
                throw new IOException("Erreur de connexion à solr", e);
            }
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
    }

    /**
     * Pipes everything from the reader to the writer via a buffer
     */
    private void pipe(Reader reader, Writer writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            writer.write(buf, 0, read);
        }
        writer.flush();

    }


}
