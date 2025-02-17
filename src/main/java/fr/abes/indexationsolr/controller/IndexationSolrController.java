package fr.abes.indexationsolr.controller;

import fr.abes.indexationsolr.services.*;
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
    private IndexationSolr indexationSolr;

    @Autowired
    private PathsFromProperties pathsFromProperties;

    public void setPathsParam() {
        indexationSolr.setUrlSolrPersonne(pathsFromProperties.getUrlSolrPersonne());
        indexationSolr.setUrlSolrHighlight(pathsFromProperties.getUrlSolrHighlight());
    }

    @RequestMapping(method = RequestMethod.POST, value = "/GetIndexationSolr")
    public boolean indexation(@RequestBody String indexation) throws Exception {

        String contexte = StringUtils.substringBetween(indexation, "$contexte$\":\"", "\",\"$iddoc$");
        int iddoc = Integer.parseInt(StringUtils.substringBetween(indexation, "$iddoc$\":\"", "\",\"$doc$"));
        StringBuilder docBuilt = new StringBuilder(StringUtils.substringBetween(indexation, "\"$doc$\":\"", "mets>"));
        docBuilt.append("mets>");
        String doc = docBuilt.toString();
        String texte = StringUtils.substringBetween(indexation, "$texte$\":\"", "\",\"$dateinsertion$");
        String dateInsertion = StringUtils.substringBetween(indexation, "$dateinsertion$\":\"", "\",\"$");
        logger.info("contexte = " + contexte);
        logger.info("iddoc = " + iddoc);
        logger.info("texte = " + texte);
        logger.info("dateInsertion = " + dateInsertion);

        pathsFromProperties.setPathsParam();
        boolean res = false;

        setPathsParam();

        if (contexte.contains("sujets")) {
            res = indexationSolr.indexerDansSolr(
                    iddoc,
                    doc,
                    pathsFromProperties.getCheminXslSujets(),
                    pathsFromProperties.getUrlSolrSujets());
            return res;
        }
        if (contexte.contains("star")) {
            res = indexationSolr.indexerDansSolr(iddoc,
                    doc,
                    pathsFromProperties.getCheminXslStar(),
                    pathsFromProperties.getUrlSolrStar());
            return res;
        } else {
            logger.error("indexation " + contexte + " iddoc " + iddoc + " = " + res);
        }
        return res;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/GetSuppressionSolr")
    public boolean suppression(@RequestBody String suppression) throws Exception {

        logger.info("suppressionSolrController - suppression début " + suppression);

        JSONObject supressionJson = new JSONObject(suppression);
        String contexte = supressionJson.getString("contexte");
        int iddoc = Integer.parseInt(supressionJson.getString("iddoc"));
        logger.info("contexte = " + contexte);
        logger.info("iddoc = " + iddoc);

        pathsFromProperties.setPathsParam();
        boolean res = false;

        setPathsParam();

        if (contexte.contains("sujets")) {
            res = indexationSolr.supprimerDeSolr(iddoc, pathsFromProperties.getUrlSolrSujets());
            return res;
        }
        if (contexte.contains("star")) {
            res = indexationSolr.supprimerDeSolr(iddoc, pathsFromProperties.getUrlSolrSujets());
            return res;
        } else {
            logger.info("suppression " + contexte + " iddoc " + iddoc + " = " + res);
        }
        return res;
    }

}

