package fr.abes.indexationsolr.controller;

import fr.abes.indexationsolr.services.DatabasePostRequest;
import fr.abes.indexationsolr.services.IndexationSolrPortail;
import fr.abes.indexationsolr.services.IndexationSolrStar;
import fr.abes.indexationsolr.services.IndexationSolrSujet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
public class IndexationSolrController {

    private Logger logger = LogManager.getLogger(IndexationSolrController.class);

    @Autowired
    private IndexationSolrSujet indexationSolrSujet;

    @Autowired
    private IndexationSolrStar indexationSolrStar;

    @Autowired
    private IndexationSolrPortail indexationSolrPortail;

    /*@Value("${testFile}")
    private String testFile;

    @RequestMapping(method = RequestMethod.GET, value="/tomcatTest")
    @ResponseBody
    public String test() {
        return "test tomcat";
    }

    @RequestMapping(method = RequestMethod.POST, value="/tomcatTestFile")
    public String testTomcatFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes)  {
        logger.info("beginning of /upload");
        if (file.isEmpty()) {
            logger.info("file is empty");
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {

            // Get the file and save it somewhere
            logger.info("beginning of try testFile = " + testFile);
            byte[] bytes = file.getBytes();
            Path path = Paths.get(testFile + file.getOriginalFilename());
            logger.info("path = " + path);
            Files.write(path, bytes);

            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/uploadStatus";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }
*/


    @PostMapping("/GetIndexationSolr")
    public boolean indexation(@RequestBody String indexation) throws Exception {

        logger.info("indexationSolrController - indexation début " + indexation);
        JSONObject indexationJson = new JSONObject(indexation);
        String contexte = indexationJson.getString("contexte");
        int iddoc = Integer.parseInt(indexationJson.getString("iddoc"));
        String texte = indexationJson.getString("texte");
        String dateInsertion = indexationJson.getString("dateinsertion");
        logger.info("contexte = " + contexte);
        logger.info("iddoc = " + iddoc);
        //logger.info("doc = " + doc);
        logger.info("texte = " + texte);
        logger.info("dateInsertion = " + dateInsertion);
        boolean res = false;


        /*String contexte = StringUtils.substringBetween(indexation, "$contexte$\":\"", "\",\"$iddoc$");
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

        boolean res = false;*/

        if(contexte.contains("sujets")) {
            logger.info("indexation contexte sujets");
            res = indexationSolrSujet.indexation(iddoc);
            //res = true;
            logger.info("indexation sujets iddoc " + iddoc + " = " + res);
            return res;
        }
        if(contexte.contains("star")) {
            logger.info("indexation contexte star");
            res = indexationSolrStar.indexation(iddoc);
            logger.info("indexation star iddoc " + iddoc + " = " + res);
            return res;
        }
        if(contexte.contains("portail")) {
            logger.info("indexation contexte portail");
            res = indexationSolrPortail.indexation(iddoc, texte, dateInsertion);
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

