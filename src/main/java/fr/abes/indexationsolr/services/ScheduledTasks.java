package fr.abes.indexationsolr.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fr.abes.indexationsolr.sujets.repositories.TempSujetsRepository;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    @Autowired
    private TempSujetsRepository tempSujetsRepository;

    @Autowired
    private TempSujetsRepository tempStarRepository;

    @Autowired
    private TempSujetsRepository tempPortailRepository;

    @Autowired
    private IndexationSolrSujet indexationSolrSujet;

    @Autowired
    private IndexationSolrStar indexationSolrStar;

    @Autowired
    private IndexationSolrPortail indexationSolrPortail;

    private Logger logger = LogManager.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        logger.info("The time is now {}", dateFormat.format(new Date()));
    }
    @Scheduled(fixedRate = 5000)
    public void getTefSujets() throws Exception {
        logger.info("beginning getTefSujets", dateFormat.format(new Date()));
        List<Integer> iddocs = tempSujetsRepository.iddocsFromTemp();
        logger.info("iddocs sujets = " + iddocs);
        for (int iddoc : iddocs){
            indexationSolrSujet.setIddoc(iddoc);
            logger.info("ici");
            indexationSolrSujet.setTef(tempSujetsRepository.getTefByIddoc(iddoc));
            logger.info("tef tempsujets = " + tempSujetsRepository.getTefByIddoc(iddoc));
            indexationSolrSujet.indexation();
            tempSujetsRepository.deleteById(iddoc);
        }
    }
    @Scheduled(fixedRate = 5000)
    public void getTefStar() throws Exception {
        logger.info("beginning getTefStar", dateFormat.format(new Date()));
        List<Integer> iddocs = tempStarRepository.iddocsFromTemp();
        logger.info("iddocs star = " + iddocs);
        for (int iddoc : iddocs){
            indexationSolrStar.setIddoc(iddoc);
            indexationSolrStar.setTef(tempStarRepository.getTefByIddoc(iddoc));
            indexationSolrStar.indexation();
            tempStarRepository.deleteById(iddoc);
        }
    }
    @Scheduled(fixedRate = 5000)
    public void getTefPortail() throws Exception {
        logger.info("beginning getTefPortail", dateFormat.format(new Date()));
        List<Integer> iddocs = tempPortailRepository.iddocsFromTemp();
        logger.info("iddocs portail = " + iddocs);
        for (int iddoc : iddocs){
            indexationSolrPortail.setIddoc(iddoc);
            String dateInsertion = tempPortailRepository.getDateInsertionByIddoc(iddoc);
            indexationSolrPortail.setDateInsertion(dateInsertion);
            indexationSolrPortail.indexation();
            tempPortailRepository.deleteById(iddoc);
        }
    }
}