package fr.abes.indexationsolr.controller;

import fr.abes.indexationsolr.services.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


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

    private void getResponseCode(String iddocparam, String contexte, HttpServletResponse response) throws IOException {
        int code = (iddocparam!=null && !iddocparam.isEmpty() && contexte!=null && !contexte.isEmpty()) ? HttpServletResponse.SC_OK
                : HttpServletResponse.SC_NOT_FOUND;
        if (code != HttpServletResponse.SC_OK) {
            response.sendError(code, "ko");
            return;
        }
        java.io.PrintWriter wr = response.getWriter();
        response.setStatus(code);
        wr.print("ok");
        wr.flush();
        wr.close();

    }

    private void getResponseCodePortail(String iddocparam, String contexte, String dateInsertion, HttpServletResponse response) throws IOException {
        int code = (iddocparam!=null && !iddocparam.isEmpty() && contexte!=null && !contexte.isEmpty()
                    && dateInsertion!=null && !dateInsertion.isEmpty())?
                    HttpServletResponse.SC_OK: HttpServletResponse.SC_NOT_FOUND;
        if (code != HttpServletResponse.SC_OK) {
            response.sendError(code, "ko");
            return;
        }
        java.io.PrintWriter wr = response.getWriter();
        response.setStatus(code);
        wr.print("ok");
        wr.flush();
        wr.close();

    }

    private void getResponseSuppCode(boolean res, HttpServletResponse response) throws IOException {
        int code = (res=true) ? HttpServletResponse.SC_OK
                : HttpServletResponse.SC_NOT_FOUND;
        if (code != HttpServletResponse.SC_OK) {
            response.sendError(code, "ko");
            return;
        }
        java.io.PrintWriter wr = response.getWriter();
        response.setStatus(code);
        wr.print("ok");
        wr.flush();
        wr.close();

    }

    @PostMapping("/GetIndexationSolr")
    public void indexation(@RequestParam("iddoc") String iddocparam, @RequestParam("contexte") String contexte,
                           HttpServletResponse response) throws Exception {

        //we give the response before looking for the tef
        getResponseCode(iddocparam, contexte,response);

        //we set iddoc and look for the tef
        //logger.info("thread sleep beginning => response already given");
        //Thread.sleep(20000);
        //logger.info("thread sleep ending");
        int iddoc = Integer.parseInt(iddocparam);


        if(contexte.contains("sujets")) {
            logger.info("indexation contexte sujets");
            indexationSolrSujet.setIddoc(iddoc);
            indexationSolrSujet.indexation();
        }
        else {
            logger.info("indexation contexte star");
            indexationSolrStar.setIddoc(iddoc);
            indexationSolrStar.indexation();
        }

    }

    @PostMapping("/GetIndexationSolrPortail")
    public void indexationPortail(@RequestParam("iddoc") String iddocparam, @RequestParam("contexte") String contexte,
                           @RequestParam("dateInsertion") String dateInsertion,
                           HttpServletResponse response) throws Exception {

        //we give the response before looking for the tef
        getResponseCodePortail(iddocparam,contexte,dateInsertion,response);

        //we set iddoc and look for the tef
        //logger.info("thread sleep beginning => response already given");
        //Thread.sleep(20000);
        //logger.info("thread sleep ending");
        int iddoc = Integer.parseInt(iddocparam);
        logger.info("indexation contexte portail");
        indexationSolrPortail.setIddoc(iddoc);
        indexationSolrPortail.setDateInsertion(dateInsertion);
        indexationSolrPortail.indexation();
    }
    /*
    if we prefer using Spring Handler Interceptor instead of java solution, the method above is code empty
    all the code above goes inside the interceptor or inside launchingIndexationSolr method
    @GetMapping("/LaunchingIndexationSolr")
    public void launchingIndexationSolr(@RequestParam("iddoc") String iddocsujet, @RequestParam("contexte") String contexte) throws Exception {
        logger.info("iddoc" + iddocsujet);
        logger.info("contexte" + contexte);
        logger.info("thread sleep beginning");
        Thread.sleep(20000);
        logger.info("thread sleep ending");
        int iddoc = Integer.parseInt(iddocsujet);
        indexationSolrSujet.setIddoc(iddoc);
        indexationSolrSujet.indexation();
    }*/

    @PostMapping("/GetSuppressionSolr")
    public ResponseEntity<String> suppression(@RequestParam("iddoc") String iddocparam, @RequestParam("contexte") String contexte,
                                                      HttpServletResponse response) throws Exception {

        boolean res = false;

        //we do the removing operation
        int iddoc = Integer.parseInt(iddocparam);

        if(contexte.contains("sujets")) {
            logger.info("supression contexte sujets");
            res = indexationSolrSujet.suppression(iddoc);
        }
        if(contexte.contains("star")) {
            logger.info("supression contexte star");
            res = indexationSolrStar.suppression(iddoc);
        }
        if(contexte.contains("portail")){
            logger.info("supression contexte portail");
            res = indexationSolrPortail.suppression(iddoc);
        }
        if (res = true) return ResponseEntity.status(HttpStatus.OK).body("OK");
        else return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("KO");
    }

}

