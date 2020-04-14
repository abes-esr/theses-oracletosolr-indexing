package fr.abes.indexationsolr.services;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class IndexationSolrSujet extends IndexationSolr {

    private Logger logger = LogManager.getLogger(IndexationSolrSujet.class);

    @Value("${urlSolrSujets}")
    private String urlSolrSujets;

    @Value("${cheminXsl.sujets}")
    private String cheminXslSujets;


    IndexationSolrSujet() {
        super();
    }

    //@Transactional(transactionManager="sujetsTransactionManager")
    public boolean indexation(int iddoc, String doc) throws Exception {

        boolean res = false;
        try {
            setUrlSolr(urlSolrSujets);
            setCheminXsl(cheminXslSujets);
            setIddoc(iddoc);
            setTef(doc);
            if (indexerDansSolr(this.getIddoc(), this.getTef())) {
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
            setUrlSolr(urlSolrSujets);
            setCheminXsl(cheminXslSujets);
            setIddoc(iddoc);
            if (supprimerDeSolr(this.getIddoc())) {
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