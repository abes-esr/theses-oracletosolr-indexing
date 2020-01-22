package fr.abes.indexationsolr.services;

import fr.abes.indexationsolr.portail.repositories.PortailRepository;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Clob;

@Getter
@Setter
@Service
public class IndexationSolrPortail extends IndexationSolr {

    private Logger logger = LogManager.getLogger(IndexationSolrPortail.class);

    @Value("${urlSolrPortail}")
    private String urlSolrPortailp;

    @Value("${urlSolrHighlight}")
    private String urlSolrHighlightp;

    @Value("${urlSolrPersonne}")
    private String urlSolrPersonnep;

    @Value("${cheminXsl.portail}")
    private String cheminXslPortail;

    private int longueurPage;

    public String perimetre; //tout|principal|principalThese|principalSujet|personne|organisme|highlight

    @Autowired
    private PortailRepository portailRepository;


    IndexationSolrPortail() {
        super();
    }

    public IndexationSolrPortail(String cheminXsl, String urlSolr, String urlSolrHighlight, String urlSolrPersonne, int iddoc, String tef, int longueurPage, String perimetre) {
        super(cheminXsl, urlSolr, urlSolrHighlight, urlSolrPersonne, iddoc, tef);
        this.longueurPage = longueurPage;
        this.perimetre = perimetre;
    }

    @Transactional(transactionManager="portailTransactionManager")
    public boolean indexation(int iddoc) throws Exception {

        boolean res = false;
        try {
            setUrlSolr(urlSolrPortailp);
            setUrlSolrPersonne(urlSolrPersonnep);
            setUrlSolrHighlight(urlSolrHighlightp);
            setCheminXsl(cheminXslPortail);
            setIddoc(iddoc);
            setTef(portailRepository.getTefByIddoc(iddoc));
            setPerimetre("tout");
            setLongueurPage(1000);
            Clob texte = portailRepository.getTexteByIddoc(iddoc);
            String dateInsertion = portailRepository.getOne(iddoc).getDateinsertion().toString();
            //2019-11-22 00:00:00.0 ==> 2019-11-22T00:00:00Z
            dateInsertion = dateInsertion.replace(" ","T");
            dateInsertion = dateInsertion.substring(0, dateInsertion.length() - 2);
            dateInsertion+="Z";
            logger.info("dateInsertion = " + dateInsertion);
            logger.info("urlSolr = " + urlSolrPortailp);
            logger.info("urlSolrHighlight = " + urlSolrHighlightp);
            logger.info("urlSolrPersonne = " + urlSolrPersonnep);

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
            setUrlSolr(urlSolrPortailp);
            setUrlSolrPersonne(urlSolrPersonnep);
            setUrlSolrHighlight(urlSolrHighlightp);

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