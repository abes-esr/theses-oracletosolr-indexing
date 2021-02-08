package fr.abes.indexationsolr.controller;

import fr.abes.indexationsolr.services.IndexationSolrPortail;
import fr.abes.indexationsolr.services.IndexationSolrStar;
import fr.abes.indexationsolr.services.IndexationSolrSujet;
import fr.abes.indexationsolr.services.PathsFromProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


@RestController
public class IndexationSolrController {

    private Logger logger = LogManager.getLogger(IndexationSolrController.class);

    @Autowired
    private IndexationSolrSujet indexationSolrSujet;

    @Autowired
    private IndexationSolrStar indexationSolrStar;

    @Autowired
    private IndexationSolrPortail indexationSolrPortail;

    @Autowired
    private PathsFromProperties pathsFromProperties;

    public void setPathsParamSujets(){
        indexationSolrSujet.setCheminXsl(pathsFromProperties.getCheminXslSujets());
        indexationSolrSujet.setUrlSolr(pathsFromProperties.getUrlSolrSujets());
    }
    public void setPathsParamStar(){
        indexationSolrStar.setCheminXsl(pathsFromProperties.getCheminXslStar());
        indexationSolrStar.setUrlSolr(pathsFromProperties.getUrlSolrStar());
    }
    public void setPathsParamPortail(){
        indexationSolrPortail.setCheminXsl(pathsFromProperties.getCheminXslPortail());
        indexationSolrPortail.setUrlSolr(pathsFromProperties.getUrlSolrPortail());
        indexationSolrPortail.setUrlSolrHighlight(pathsFromProperties.getUrlSolrHighlight());
        indexationSolrPortail.setUrlSolrPersonne(pathsFromProperties.getUrlSolrPersonne());
    }


    @RequestMapping(method = RequestMethod.POST, value="/GetIndexationSolr")
    public boolean indexation(@RequestBody String indexation) throws Exception {

        ////logger.info("indexationSolrController - indexation début " + indexation);

        String contexte = StringUtils.substringBetween(indexation, "$contexte$\":\"", "\",\"$iddoc$");
        int iddoc = Integer.parseInt(StringUtils.substringBetween(indexation, "$iddoc$\":\"", "\",\"$doc$"));
        StringBuilder docBuilt = new StringBuilder(StringUtils.substringBetween(indexation, "\"$doc$\":\"", "mets>"));
        docBuilt.append("mets>");
        String doc = docBuilt.toString();
        String texte = StringUtils.substringBetween(indexation, "$texte$\":\"", "\",\"$dateinsertion$");
        String dateInsertion = StringUtils.substringBetween(indexation, "$dateinsertion$\":\"", "\",\"$");
        logger.info("contexte = " + contexte);
        logger.info("iddoc = " + iddoc);
        //logger.info("doc = " + doc);
        logger.info("texte = " + texte);
        logger.info("dateInsertion = " + dateInsertion);

        pathsFromProperties.setPathsParam();
        boolean res = false;

        if(contexte.contains("sujets")) {
            logger.info("indexation contexte sujets");
            setPathsParamSujets();
            logger.info("cheminXslSujets = " + indexationSolrSujet.getCheminXsl());
            logger.info("urlSolrSujets = " + indexationSolrSujet.getUrlSolr());
            res = indexationSolrSujet.indexation(iddoc, doc);
            logger.info("indexation sujets iddoc " + iddoc + " = " + res);
            return res;
        }
        if(contexte.contains("star")) {
            logger.info("indexation contexte star");
            setPathsParamStar();
            logger.info("cheminXslStar = " + indexationSolrStar.getCheminXsl());
            logger.info("urlSolrStar = " + indexationSolrStar.getUrlSolr());
            res = indexationSolrStar.indexation(iddoc, doc);
            logger.info("indexation star iddoc " + iddoc + " = " + res);
            return res;
        }
        if(contexte.contains("portail")) {
            logger.info("indexation contexte portail");
            setPathsParamPortail();
            logger.info("cheminXslPortail = " + indexationSolrPortail.getCheminXsl());
            logger.info("urlSolrPortail = " + indexationSolrPortail.getUrlSolr());
            logger.info("urlSolrHighlight = " + indexationSolrPortail.getUrlSolrHighlight());
            logger.info("urlSolrPersonne = " + indexationSolrPortail.getUrlSolrPersonne());
            res = indexationSolrPortail.indexation(iddoc, doc, texte, dateInsertion);
            logger.info("indexation portail iddoc " + iddoc + " = " + res);
        }
        else {
            logger.info("indexation " + contexte + " iddoc " + iddoc + " = " + res);
        }
        return res;
    }


    @RequestMapping(method = RequestMethod.POST, value="/GetSuppressionSolr")
    public boolean suppression(@RequestBody String suppression) throws Exception {

        logger.info("suppressionSolrController - suppression début " + suppression);

        JSONObject supressionJson = new JSONObject(suppression);
        String contexte = supressionJson.getString("contexte");
        int iddoc = Integer.parseInt(supressionJson.getString("iddoc"));
        logger.info("contexte = " + contexte);
        logger.info("iddoc = " + iddoc);

        pathsFromProperties.setPathsParam();
        boolean res = false;

        if(contexte.contains("sujets")) {
            logger.info("supression contexte sujets");
            setPathsParamSujets();
            res = indexationSolrSujet.suppression(iddoc);
            logger.info("suppression sujets iddoc " + iddoc + " = " + res);
            return res;
        }
        if(contexte.contains("star")) {
            logger.info("supression contexte star");
            setPathsParamStar();
            res = indexationSolrStar.suppression(iddoc);
            logger.info("suppression star iddoc " + iddoc + " = " + res);
            return res;
        }
        if(contexte.contains("portail")){
            logger.info("supression contexte portail");
            setPathsParamPortail();
            res = indexationSolrPortail.suppression(iddoc);
            logger.info("suppression portail iddoc " + iddoc + " = " + res);
        }
        else {
            logger.info("suppression " + contexte + " iddoc " + iddoc + " = " + res);
        }
        return res;
    }

}

