package fr.abes.indexationsolr.services;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Getter
@Setter
@Service
public class IndexationSolrPortail extends IndexationSolr {

    private Logger logger = LogManager.getLogger(IndexationSolrPortail.class);

    /*@Value("${urlSolrPortail}")
    private String urlSolrPortailp;

    @Value("${urlSolrHighlight}")
    private String urlSolrHighlightp;

    @Value("${urlSolrPersonne}")
    private String urlSolrPersonnep;

    @Value("${cheminXsl.portail}")
    private String cheminXslPortail;
*/
    private int longueurPage;

    public String perimetre; //tout|principal|principalThese|principalSujet|personne|organisme|highlight



    IndexationSolrPortail() {
        super();
    }

    public IndexationSolrPortail(String cheminXsl, String urlSolr, String urlSolrHighlight, String urlSolrPersonne, int iddoc, String tef, int longueurPage, String perimetre) {
        super(cheminXsl, urlSolr, urlSolrHighlight, urlSolrPersonne, iddoc, tef);
        this.longueurPage = longueurPage;
        this.perimetre = perimetre;
    }

    public boolean indexation(int iddoc, String doc, String texte, String dateInsertion) throws Exception {

        boolean res = false;
        try {
            /*setUrlSolr(urlSolrPortailp);
            setUrlSolrPersonne(urlSolrPersonnep);
            setUrlSolrHighlight(urlSolrHighlightp);
            setCheminXsl(cheminXslPortail);*/
            setIddoc(iddoc);
            setTef(doc);
            setPerimetre("tout");
            setLongueurPage(1000);
            logger.info("dateInsertion = " + dateInsertion);
            /*logger.info("urlSolr = " + urlSolrPortailp);
            logger.info("urlSolrHighlight = " + urlSolrHighlightp);
            logger.info("urlSolrPersonne = " + urlSolrPersonnep);*/

            if (indexerDansSolr(this.getTef(), this.getIddoc(), texte, dateInsertion ,1, perimetre, longueurPage)) {
                res = true;
            }
            logger.info("res dans indexation = " + res);
        } catch (Exception e) {
            logger.info("Erreur dans indexation :"+e.getMessage());
            throw new Exception(e);
        }
        return res;
    }

    public boolean suppression(int iddoc) throws Exception {

        boolean res = false;
        try {
            /*setUrlSolr(urlSolrPortailp);
            setUrlSolrPersonne(urlSolrPersonnep);
            setUrlSolrHighlight(urlSolrHighlightp);*/

            if (supprimeDeSolr(iddoc)) {
                res = true;
            }
            logger.info("res dans suppression = " + res);
        } catch (Exception e) {
            logger.info("Erreur dans suppression :"+e.getMessage());
            throw new Exception(e);
        }
        return res;
    }

}