package fr.abes.indexationsolr.services;

import fr.abes.indexationsolr.star.repositories.StarRepository;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


@Service
public class IndexationSolrStar extends IndexationSolr {

    private Logger logger = LogManager.getLogger(IndexationSolrStar.class);

    @Autowired
    private Environment env;

    @Autowired
    private StarRepository starRepository;


    IndexationSolrStar() {
        super();
    }

    //@Transactional(transactionManager="starTransactionManager")
    public boolean indexation() throws Exception {

        boolean res = false;
        String tef;
        boolean tefInitialisation = false;
        try {
            setUrlSolr(env.getProperty("urlSolrStar"));
            setCheminXsl(env.getProperty("cheminXsl.star"));
            tef = starRepository.getTefByIddoc(this.getIddoc());
            tefInitialisation = (tef.contains("RECORDSTATUS=\"initialisation\"") || tef.contains("RECORDSTATUS=\"EnCours\"") || tef == null );
            if(tefInitialisation) {
                logger.info("thread sleep star beginning");
                Thread.sleep(1 * 60 * 1000);
                logger.info("thread sleep star ending");
                tef = starRepository.getTefByIddoc(this.getIddoc());
            }
            else {
                setTef(tef);
            }
            //logger.info(("star this.getTef() =" + this.getTef()));
            logger.info("tef star = " + tef);
            if (indexerDansSolr(this.getIddoc(), this.getTef())) {
                res = true;
            }
            logger.info("res dans indexation star = " + res);
        } catch (Exception e) {
            logger.info("Erreur dans indexation star :"+e.getMessage());
            throw new Exception(e);
        }
        return res;
    }

    public boolean suppression(int iddoc) throws Exception {

        boolean res = false;
        try {
            setUrlSolr(env.getProperty("urlSolrStar"));
            setCheminXsl(env.getProperty("cheminXsl.star"));
            if (supprimerDeSolr(iddoc)) {
                res = true;
            }
            logger.info("res dans suppression star = " + res);
        } catch (Exception e) {
            logger.info("Erreur dans suppression star :"+e.getMessage());
            throw new Exception(e);
        }
        return res;
    }
}