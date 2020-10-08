package fr.abes.indexationsolr.services;


import fr.abes.indexationsolr.sujets.repositories.SujetsRepository;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


@Service
public class IndexationSolrSujet extends IndexationSolr {

    private Logger logger = LogManager.getLogger(IndexationSolrSujet.class);


    @Autowired
    private Environment env;

    @Autowired
    private SujetsRepository sujetsRepository;


    IndexationSolrSujet() {
        super();
    }

    //@Transactional(transactionManager="sujetsTransactionManager")
    public boolean indexation() throws Exception {
        boolean res = false;
        try {
            logger.info(("this.getIddoc()" + this.getIddoc()));
            logger.info(env.getProperty("urlSolrSujets"));
            logger.info(env.getProperty("cheminXsl.sujets"));
            setUrlSolr(env.getProperty("urlSolrSujets"));
            setCheminXsl(env.getProperty("cheminXsl.sujets"));
            this.setTef(sujetsRepository.getTefByIddoc(this.getIddoc()));
            logger.info(("sujets this.getTef() =" + this.getTef()));

            if (indexerDansSolr(this.getIddoc(),this.getTef())) {
                res = true;
            }
            logger.info("res dans indexation sujets = " + res);
        } catch (Exception e) {
            logger.info("Erreur dans indexation sujets :"+e.getMessage());
            throw new Exception(e);
        }
        return res;
    }

    public boolean suppression(int iddoc) throws Exception {

        boolean res = false;
        try {
            setUrlSolr(env.getProperty("urlSolrSujets"));
            setCheminXsl(env.getProperty("cheminXsl.sujets"));
            setIddoc(iddoc);
            if (supprimerDeSolr(this.getIddoc())) {
                res = true;
            }
            logger.info("res dans suppression sujets = " + res);
        } catch (Exception e) {
            logger.info("Erreur dans suppression sujets :"+e.getMessage());
            throw new Exception(e);
        }
        return res;
    }
}