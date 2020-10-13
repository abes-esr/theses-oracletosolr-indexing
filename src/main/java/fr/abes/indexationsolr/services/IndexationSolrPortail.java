package fr.abes.indexationsolr.services;

import fr.abes.indexationsolr.portail.repositories.PortailRepository;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


@Getter
@Setter
@Service
public class IndexationSolrPortail extends IndexationSolr {

    private Logger logger = LogManager.getLogger(IndexationSolrPortail.class);

    @Autowired
    private Environment env;

    @Autowired
    private PortailRepository portailRepository;

    private int longueurPage;

    public String perimetre; //tout|principal|principalThese|principalSujet|personne|organisme|highlight



    IndexationSolrPortail() {
        super();
    }

    public IndexationSolrPortail(String cheminXsl, String urlSolr, String urlSolrHighlight, String urlSolrPersonne, int iddoc, String tef, String texte, String dateInsertion, int longueurPage, String perimetre) {
        super(cheminXsl, urlSolr, urlSolrHighlight, urlSolrPersonne, iddoc, tef, texte, dateInsertion);
        this.longueurPage = longueurPage;
        this.perimetre = perimetre;
    }

    //@Transactional(transactionManager="portailTransactionManager")
    public boolean indexation() throws Exception {

        boolean res = false;
        try {
            setUrlSolr(env.getProperty("urlSolrPortail"));
            setUrlSolrPersonne(env.getProperty("urlSolrPersonne"));
            setUrlSolrHighlight(env.getProperty("urlSolrHighlight"));
            setCheminXsl(env.getProperty("cheminXsl.portail"));
            setTef(portailRepository.getTefByIddoc(this.getIddoc()));
            setTexte(portailRepository.getTexteByIddoc(this.getIddoc()));
            logger.info(("tef portail =" + this.getTef()));
            logger.info(("texte portail =" + this.getTexte()));
            setPerimetre("tout");
            setLongueurPage(1000);
            logger.info("portail dateInsertion = " + this.getDateInsertion());
            logger.info("portail urlSolr = " + env.getProperty("urlSolrPortail"));
            logger.info("portail urlSolrHighlight = " + env.getProperty("urlSolrHighlight"));
            logger.info("portail urlSolrPersonne = " +env.getProperty("urlSolrHighlight"));

            if (indexerDansSolr(this.getTef(), this.getIddoc(), this.getTexte(), this.getDateInsertion() ,1, perimetre, longueurPage)) {
                res = true;
            }
            logger.info("res dans indexation portail = " + res);
        } catch (Exception e) {
            logger.info("Erreur dans indexation portail:"+e.getMessage());
            throw new Exception(e);
        }
        return res;
    }

    public boolean suppression(int iddoc) throws Exception {

        boolean res = false;
        try {
            setUrlSolr(env.getProperty("urlSolrPortail"));
            setUrlSolrPersonne(env.getProperty("urlSolrPersonne"));
            setUrlSolrHighlight(env.getProperty("urlSolrHighlight"));

            if (supprimeDeSolr(iddoc)) {
                res = true;
            }
            logger.info("res dans suppression portail= " + res);
        } catch (Exception e) {
            logger.info("Erreur dans suppression portail:"+e.getMessage());
            throw new Exception(e);
        }
        return res;
    }
}