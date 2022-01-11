package fr.abes.indexationsolr.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PathsFromProperties {

    private static final Logger logger = LoggerFactory
            .getLogger(PathsFromProperties.class);

    @Autowired
    private Environment env;

    private String cheminXslSujets;
    private String cheminXslStar;
    private String cheminXslPortail;

    private String urlSolrSujets;
    private String urlSolrStar;
    private String urlSolrPortail;
    private String urlSolrHighlight;
    private String urlSolrPersonne;


    public void setPathsParam(){

        try {
            this.cheminXslSujets = env.getProperty("cheminXslSujets");
            this.cheminXslStar = env.getProperty("cheminXslStar");
            this.cheminXslPortail = env.getProperty("cheminXslPortail");
            this.urlSolrSujets = env.getProperty("urlSolrSujets");
            this.urlSolrStar = env.getProperty("urlSolrStar");
            this.urlSolrPortail = env.getProperty("urlSolrPortail");
            this.urlSolrHighlight = env.getProperty("urlSolrHighlight");
            this.urlSolrPersonne = env.getProperty("urlSolrPersonne");
        }
        catch (Exception e) {
            logger.info("setPathsParam() KO : env = " + env + " ; " + e.getMessage());
        }
    }

    public String getCheminXslSujets(){
        return this.cheminXslSujets;
    }
    public String getCheminXslStar(){
        return this.cheminXslStar;
    }
    public String getCheminXslPortail(){
        return this.cheminXslPortail;
    }
    public String getUrlSolrSujets(){
        return this.urlSolrSujets;
    }
    public String getUrlSolrStar(){
        return this.urlSolrStar;
    }
    public String getUrlSolrPortail(){
        return this.urlSolrPortail;
    }
    public String getUrlSolrHighlight(){
        return this.urlSolrHighlight;
    }
    public String getUrlSolrPersonne() {
        return this.urlSolrPersonne;
    }
}
