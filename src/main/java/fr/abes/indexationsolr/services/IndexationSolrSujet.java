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
        String tef;
        boolean tefInitialisation = false;
        try {
            setUrlSolr(env.getProperty("urlSolrSujets"));
            setCheminXsl(env.getProperty("cheminXsl.sujets"));
            do {
                tef = sujetsRepository.getTefByIddoc(this.getIddoc());
                tefInitialisation = (tef.contains("RECORDSTATUS=\"initialisation\"") || tef.contains("RECORDSTATUS=\"EnCours\"") || tef == null );
            }while (tefInitialisation);

            setTef(tef);
            //logger.info(("star this.getTef() =" + this.getTef()));
            logger.info("tef sujets = " + tef);
            if (indexerDansSolr(this.getIddoc(), this.getTef())) {
                res = true;
            }
            logger.info("res dans indexation sujets= " + res);
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