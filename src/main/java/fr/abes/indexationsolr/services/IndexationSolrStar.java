package fr.abes.indexationsolr.services;

import fr.abes.indexationsolr.star.repositories.StarRepository;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IndexationSolrStar extends IndexationSolr {

    //private Logger logger = Logger.getLogger(IndexationSolrStar.class);
    private Logger logger = LogManager.getLogger(IndexationSolrStar.class);

    @Value("${urlSolrStar}")
    private String urlSolrStar;

    @Value("${cheminXsl.star}")
    private String cheminXslStar;

    @Autowired
    private StarRepository starRepository;

    IndexationSolrStar() {
        super();
    }

    @Transactional(transactionManager="starTransactionManager")
    public boolean indexation(int iddoc) throws Exception {

        boolean res = false;
        try {
            setUrlSolr(urlSolrStar);
            setCheminXsl(cheminXslStar);
            setIddoc(iddoc);
            setTef(starRepository.getTefByIddoc(iddoc));
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
            setUrlSolr(urlSolrStar);
            setCheminXsl(cheminXslStar);
            if (supprimerDeSolr(iddoc)) {
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