package fr.abes.indexationsolr.controller;

import fr.abes.indexationsolr.services.*;
import fr.abes.indexationsolr.sujets.repositories.SujetsRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.http.HttpServletResponse;


@RestController
@Getter
@Setter
public class IndexationSolrController {

    //https://stackoverflow.com/questions/32072255/how-to-send-response-before-actions-in-spring-mvc

    private Logger logger = LogManager.getLogger(IndexationSolrController.class);

    @Autowired
    private IndexationSolrSujet indexationSolrSujet;

    @Autowired
    private IndexationSolrStar indexationSolrStar;

    @Autowired
    private IndexationSolrPortail indexationSolrPortail;




    @PostMapping("/SetIddoc")
    public void setIddocAndContext(@RequestParam("iddoc") String iddocsujet, @RequestParam("contexte") String contexte, HttpServletResponse response) throws Exception {


        int code = (iddocsujet!=null && !iddocsujet.isEmpty()) ? HttpServletResponse.SC_OK
                : HttpServletResponse.SC_NOT_FOUND;
        if (code != HttpServletResponse.SC_OK) {
            response.sendError(code, "ok");
            return;
        }
        java.io.PrintWriter wr = response.getWriter();
        response.setStatus(code);
        wr.print("ok");
        wr.flush();
        wr.close();


        launchingIndexationSolr(iddocsujet,contexte);




        /*///logger.info("indexationSolrController - indexation début " + indexation);
        String message = "";
        //JSONObject indexationJson = new JSONObject(indexation);
        //String contexte = indexationJson.getString("contexte");
        int iddoc = Integer.parseInt(iddocsujet);


        logger.info("contexte = " + contexte);
        logger.info("iddoc = " + iddoc);
        //logger.info("doc = " + doc);

        boolean res = false;

        if(contexte.contains("sujets")) {
            logger.info("indexation contexte sujets");
            //indexationSolrSujet.setIddoc(iddoc);


            //indexationInterceptor.setContexte("sujets");
            //indexationInterceptor.setIndexationSolrSujet(indexationSolrSujet);
            //if (indexationSolrSujet.getIddoc()!=0) indexationSolrSujet.indexation();

            //logger.info("indexation sujets iddoc " + indexationSolrSujet.getIddoc());
            //res = indexationSolrSujet.indexation();
            //logger.info("indexation sujets iddoc " + iddoc + " = " + res);
            //return res;
            message = "setIddoc sujet Ok ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        }
        if(contexte.contains("star")) {
            logger.info("indexation contexte star");
            indexationSolrStar.setIddoc(iddoc);
            //indexationSolrStar.setIddoc(iddoc);
            //indexationInterceptor.setContexte("star");
            //indexationInterceptor.setIndexationSolrStar(indexationSolrStar);
            //res = indexationSolrStar.indexation();
            logger.info("indexation star iddoc " + iddoc + " = " + res);
            //return res;
            message = "setIddoc star Ok ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        }
        if(contexte.contains("portail")) {
            logger.info("indexation contexte portail");
            //indexationSolrPortail.setIddoc(iddoc);
            //String texte = indexationJson.getString("texte");
            //String dateInsertion = indexationJson.getString("dateinsertion");
            //logger.info("texte = " + texte);
            //logger.info("dateInsertion = " + dateInsertion);
           // indexationSolrPortail.setTexte(texte);
           // indexationSolrPortail.setDateInsertion(dateInsertion);
            //indexationInterceptor.setContexte("portail");
            //indexationInterceptor.setIndexationSolrPortail(indexationSolrPortail);
            //res = indexationSolrPortail.indexation();
            logger.info("indexation portail iddoc " + iddoc + " = " + res);
            message = "setIddoc portail Ok ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        }
        else {
            logger.info("indexation " + contexte + " iddoc " + iddoc + " = " + res);
            message = "setIddoc ko ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        }*/

    }
    //@GetMapping("/LaunchingIndexationSolr")

    public void launchingIndexationSolr(String iddocsujet, String contexte) throws Exception {
        //logger.info("indexationSolrController - /LaunchingIndexationSolr " );
        //boolean res = false;
        //logger.info("indexationSolrSujet.getIddoc() = " + indexationSolrSujet.getIddoc());
        logger.info("thread sleep beginning");
        Thread.sleep(20000);
        logger.info("thread sleep ending");
        int iddoc = Integer.parseInt(iddocsujet);
        indexationSolrSujet.setIddoc(iddoc);
        indexationSolrSujet.indexation();
        //return res;
    }

    @RequestMapping(method = RequestMethod.POST, value="/GetSuppressionSolr")
    public boolean suppression(@RequestBody String suppression) throws Exception {

        logger.info("suppressionSolrController - suppression début " + suppression);

        JSONObject supressionJson = new JSONObject(suppression);
        String contexte = supressionJson.getString("contexte");
        int iddoc = Integer.parseInt(supressionJson.getString("iddoc"));
        logger.info("contexte = " + contexte);
        logger.info("iddoc = " + iddoc);
        boolean res = false;

        if(contexte.contains("sujets")) {
            logger.info("supression contexte sujets");
            res = indexationSolrSujet.suppression(iddoc);
            logger.info("suppression sujets iddoc " + iddoc + " = " + res);
            return res;
        }
        if(contexte.contains("star")) {
            logger.info("supression contexte star");
            res = indexationSolrStar.suppression(iddoc);
            logger.info("suppression star iddoc " + iddoc + " = " + res);
            return res;
        }
        if(contexte.contains("portail")){
            logger.info("supression contexte portail");
            res = indexationSolrPortail.suppression(iddoc);
            logger.info("suppression portail iddoc " + iddoc + " = " + res);
        }
        else {
            logger.info("suppression " + contexte + " iddoc " + iddoc + " = " + res);
        }
        return res;
    }

}

