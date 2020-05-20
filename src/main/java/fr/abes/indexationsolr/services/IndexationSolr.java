package fr.abes.indexationsolr.services;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Getter @Setter
@NoArgsConstructor
public class IndexationSolr {

    private Logger logger = LogManager.getLogger(IndexationSolr.class);

    private String cheminXsl;
    private String urlSolr;// star ou step ou portail
    private String urlSolrHighlight;
    private String urlSolrPersonne;
    private int iddoc;
    private String tef;

    public IndexationSolr(String cheminXsl, String urlSolr, String urlSolrHighlight, String urlSolrPersonne, int iddoc, String tef) {
        this.cheminXsl = cheminXsl;
        this.urlSolr = urlSolr;
        this.urlSolrHighlight = urlSolrHighlight;
        this.urlSolrPersonne = urlSolrPersonne;
        this.iddoc = iddoc;
        this.tef = tef;
    }

    public boolean indexerDansSolr(int iddoc, String tef) throws TransformerException, IOException {
        boolean res = false;
        final StringWriter sw = new StringWriter();
        try {
            logger.info("indexerDansSolr pour Step ou Star");
            String docSolr = transfoXSL(tef, iddoc);
            logger.info("transfoXSL ok");
            envoieSurSolr(docSolr, urlSolr);
            logger.info("envoieSurSolr ok");
            postData(new StringReader("<commit/>"), sw, urlSolr);
            res = true;
        } catch (Exception e) {
            logger.info("Erreur dans indexerDansSolr :"+e.getMessage());
            throw e;
        }
        return res;
    }


    public boolean supprimerDeSolr(int idDoc) throws IOException {
        boolean res = false;
        final StringWriter sw = new StringWriter();
        try
        {
            postData(new StringReader("<delete><id>" + idDoc + "</id></delete>"), sw, urlSolr);
            if (sw.toString().indexOf("<int name=\"status\">0</int>") < 0) {
                logger.info("unexpected response from solr...");
            }
            postData(new StringReader("<commit/>"), sw, urlSolr);
            res = true;
        }
        catch (Exception e) {
            logger.info("Erreur dans supprimerDeSolr :"+e.getMessage());
            throw e;
        }
        return res;
    }


    /**
     * Pour la transformation XSL :
     * chargement de saxon dans oracle
     * et utilisation de net.sf.saxon.TransformerFactoryImpl
     * note : il faut utiliser un chemin de fichier sans le protocole (file://C:\\)
     * pour saxon pour charger l'xsl
     * note: il faut preciser que le tef (xmltype) est encode en utf-8 = InputStreamReader(tef.getInputStream(), "UTF-8"))
     */
    public String transfoXSL(String tef, int idDoc) throws TransformerException {
        InputStream stream = new ByteArrayInputStream(tef.getBytes(StandardCharsets.UTF_8));
        logger.info("transfoXsl idDoc = " + idDoc);
        logger.info("cheminXsl = " + cheminXsl);
        TransformerFactory tFactory = new net.sf.saxon.TransformerFactoryImpl();
        Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(cheminXsl));
        transformer.setParameter("idDeLaBase", idDoc);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.transform(new javax.xml.transform.stream.StreamSource(stream), new javax.xml.transform.stream.StreamResult(out));
        return out.toString();
    }




    //-----------------------------------pour Portail------------------------------------//

    public boolean indexerDansSolr(String tef, int iddoc, String texte, String dateInsertion, int envoiSolr, String perimetre, int longueurPage) throws IOException, ParseException, TransformerException, JSONException {
        boolean res = false;
        try {
            logger.info("SolrPortail.indexerDansSolr");

            logger.info("texte = " + texte);
            logger.info("dateinsertion = " + dateInsertion);
            logger.info("urlSolr = " + urlSolr);
            logger.info("urlSolrHighlight = " + urlSolrHighlight);
            logger.info("urlSolrPersonne = " + urlSolrPersonne);

            if (envoiSolr == 1) {
                String docSolr = transfoXSL(tef, iddoc, texte, dateInsertion);
                logger.info("transfoXSLportail ok");
                final StringWriter sw = new StringWriter();

                deletePersonne(iddoc);

                if (perimetre.equals("tout") || perimetre.equals("principal")) { //perimetre.equals("personne") || perimetre.equals("organisme") --> Pas besoin car que des maj, non ?
                    envoieSurSolr(docSolr, urlSolr);
                    postData(new StringReader("<commit/>"), sw, urlSolr);
                }

                if (perimetre.equals("tout") || perimetre.equals("highlight")) {
                    decouperIndexerSolrHighlight(texte, iddoc, longueurPage);
                    postData(new StringReader("<commit/>"), sw, urlSolrHighlight);
                }

                //On peut maintenant ajouter toutes les personnes du TEF mis a jour
                if (perimetre.equals("tout") || perimetre.equals("personne")) {
                    addPersonne(iddoc);
                    postData(new StringReader("<commit/>"), sw, urlSolrPersonne);
                }
                res = true;
                logger.info("SolrPortail.indexerDansSolr termine");

            } else {
                logger.info("pas d'envoi sur Solr");
            }
        } catch (IOException | TransformerException | JSONException | ParseException e) {
            logger.error(e.getMessage());
            throw e;
        }
        return res;
    }

    //FONCTIONS PERSONNES
    //Envoi la requete de suppression de(s) personne(s)
    public Map<String, String> deletePersonne(int iddoc) throws IOException, JSONException {
        //Recuperation de tous les ppn/id personne du TEF
        HashMap<String, String> ppnPersonne = new HashMap<>(); //Liste des ppns/personnes trouvés : sert dans le cas d'une suppression du TEF, il faut alors mettre a jour toutes les personnes concernees
        URL urlPPN = new URL(urlSolr.replace("update", "select/?q=id:") + iddoc + "&fl=auteur,auteurNP,auteurPpn,coAuteur,coAuteurPpn,coAuteurNP,directeurThese,directeurTheseNP,directeurThesePpn,rapporteur,rapporteurNP,rapporteurPpn&rows=1000&wt=json");
        JSONObject doc = getJsonObject(urlPPN).optJSONObject(0);

        if (doc != null) {
            StringBuilder ppnSuppr = new StringBuilder("<delete>");
            int indexPersonne = 0; //Permet de determiner l'id dans le cas ou on n'a pas de ppn. Construction : iddoc+"_"+i

            //AUTEUR
            String auteurPpn = doc.optString("auteurPpn");
            if (auteurPpn.equals("") || auteurPpn.contains("?")) {//Si pas ppn
                auteurPpn = iddoc + "_" + indexPersonne; //construction : iddoc+"_"+i
                indexPersonne++;
            } else {
                ppnPersonne.put(auteurPpn, doc.optString("auteur") + "-||-" + doc.optString("auteurNP"));
            }

            ppnSuppr.append("<id>");
            ppnSuppr.append(auteurPpn);
            ppnSuppr.append("</id>");
            //COAUTEUR
            ppnSuppr.append(getPpnSuppr("coAuteurPpn", "coAuteur", "coAuteurNP", doc, iddoc, indexPersonne, ppnPersonne));

            //DIRECTEURTHESE
            ppnSuppr.append(getPpnSuppr("directeurThesePpn", "directeurThese", "directeurTheseNP", doc, iddoc, indexPersonne, ppnPersonne));

            //RAPPORTEUR
            ppnSuppr.append(getPpnSuppr("rapporteurPpn", "rapporteur", "rapporteurNP", doc, iddoc, indexPersonne, ppnPersonne));

            ppnSuppr.append("</delete>");
            if (ppnSuppr.toString().contains("id")) {//Si il y a des ppns/id à supprimer
                final StringWriter sw = new StringWriter();
                postData(new StringReader(ppnSuppr.toString()), sw, urlSolrPersonne);
                if (sw.toString().indexOf("<int name=\"status\">0</int>") < 0) {
                    logger.info("unexpected reponse from solr...");
                    logger.error("unexpected response from solr...");
                }
                postData(new StringReader("<commit/>"), sw, urlSolrPersonne); //Il faut garder ce commit car sinon on risque de reindexer des personnes absentes
                logger.info(sw.toString());
                logger.info("commit effectue");
            }
        }
        return ppnPersonne;
    }

    private String getPpnSuppr(String fieldPpn, String field, String fieldNp, JSONObject doc, int iddoc, int indexPersonne, HashMap<String, String> ppnPersonne) {
        JSONArray fieldArrayPpn = doc.optJSONArray(fieldPpn);
        StringBuilder ppnSuppr = new StringBuilder();
        if (fieldArrayPpn != null) {
            for (int i = 0; i < fieldArrayPpn.length(); i++) {
                String ppn = fieldArrayPpn.optString(i);
                if (ppn.equals("") || ppn.contains("?")) {//Si pas ppn
                    ppn = iddoc + "_" + indexPersonne; //construction : iddoc+"_"+i
                    indexPersonne++;
                } else {
                    ppnPersonne.put(ppn, doc.optJSONArray(field).optString(i) + "-||-" + doc.optJSONArray(fieldNp).optString(i));
                }
                ppnSuppr.append("<id>");
                ppnSuppr.append(ppn);
                ppnSuppr.append("</id>");
            }
        }
        return ppnSuppr.toString();
    }

    private JSONArray getJsonObject(URL urlPPN) throws IOException, JSONException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlPPN.openStream(), StandardCharsets.UTF_8))) {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            JSONObject json = new JSONObject(builder.toString());
            return json.optJSONObject("response").optJSONArray("docs");
        } catch (IOException e) {
            logger.error("Erreur dans récupération de l'objet JSON");
            throw e;
        }

    }

    //Envoi les requetes d'ajout des personnes trouvees
    public void addPersonne(int iddoc) throws IOException, JSONException, ParseException {

        //Recuperation de toutes les personnes du TEF
        URL urlPPN = new URL(urlSolr.replace("update", "select/?q=id:") + iddoc + "&rows=1000&wt=json");
        JSONObject doc = getJsonObject(urlPPN).optJSONObject(0);

        int indexPersonne = 0; //Sert pr la génération des id qd pas de ppn

        //AUTEUR
        indexPersonne = postPersonne(iddoc, doc.optString("auteurPpn"), doc.optString("auteur"), doc.optString("auteurNP"), "auteur", doc, indexPersonne);

        //COAUTEUR
        if (doc.optJSONArray("coAuteurPpn") != null) {
            for (int i = 0; i < doc.optJSONArray("coAuteurPpn").length(); i++) {
                indexPersonne = postPersonne(iddoc, doc.optJSONArray("coAuteurPpn").optString(i), doc.optJSONArray("coAuteur").optString(i), doc.optJSONArray("coAuteurNP").optString(i), "auteur", doc, indexPersonne);
            }
        }

        //DIRECTEURTHESE
        if (doc.optJSONArray("directeurThesePpn") != null) {
            for (int i = 0; i < doc.optJSONArray("directeurThesePpn").length(); i++) {
                indexPersonne = postPersonne(iddoc, doc.optJSONArray("directeurThesePpn").optString(i), doc.optJSONArray("directeurThese").optString(i), doc.optJSONArray("directeurTheseNP").optString(i), "directeurThese", doc, indexPersonne);
            }
        }

        //RAPPORTEUR
        if (doc.optJSONArray("rapporteurPpn") != null) {
            for (int i = 0; i < doc.optJSONArray("rapporteurPpn").length(); i++) {
                indexPersonne = postPersonne(iddoc, doc.optJSONArray("rapporteurPpn").optString(i), doc.optJSONArray("rapporteur").optString(i), doc.optJSONArray("rapporteurNP").optString(i), "rapporteur", doc, indexPersonne);
            }
        }

    }

    //Envoi les requetes d'ajout des personnes à partir de la liste ppns/personnes : appele dans le cas d'une suppression d'un TEF
    public void addPersonne(HashMap<String, String> ppnPersonne) throws ParseException, JSONException, IOException {
        Iterator<String> itr = ppnPersonne.keySet().iterator();
        while (itr.hasNext()) {
            String ppn = itr.next();
            String nomEtnomNP = (String) ppnPersonne.get(ppn);
            String nom = nomEtnomNP.substring(0, nomEtnomNP.indexOf("-||-"));
            String nomNP = nomEtnomNP.substring(nomEtnomNP.indexOf("-||-") + 4);
            logger.info("nom:" + nom + " nomNP:" + nomNP);
            postPersonne(0, ppn, nom, nomNP, "", null, 0 );
        }
    }

    //Envoi le cumul des infos de la personne/ppn
    public int postPersonne(int idDoc, String ppn, String nom, String nomNP, String role, JSONObject doc, int indexPersonne) throws ParseException, IOException, JSONException {
        StringBuilder cumul;
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        if (!nom.trim().equals("")) { //Si on a bien une personne (pas de nom/prenom "vide")
            if (ppn == null || ppn.equals("") || ppn.contains("?")) { //Si pas de PPN, on genere un id et on prend les valeurs du TEF insere
                ppn = idDoc + "_" + indexPersonne;
                indexPersonne++;

                cumul = new StringBuilder("<field name=\"id\">");
                cumul.append(ppn);
                cumul.append("</field>");
                cumul.append("<field name=\"personne\">");
                cumul.append(StringEscapeUtils.escapeXml10(nom));
                cumul.append("</field>");
                cumul.append("<field name=\"personneNP\">");
                cumul.append(StringEscapeUtils.escapeXml10(nomNP));
                cumul.append("</field>");

                //On récupère la date d'insertion (la plus vieille) et la date de maj (la plus recente) des theses liees
                String dateInsert = doc.optString("dateInsert");
                String dateMaj = doc.optString("dateMaj");
                //On récupère, dans dateThese, la date de soutenance ou de 1ere inscription pour calculer actif/inactif
                String dateSoutenance = doc.optString("dateSoutenance");
                String sujDatePremiereInscription = doc.optString("sujDatePremiereInscription");
                String dateThese = dateSoutenance;
                if (dateSoutenance == null || dateSoutenance.length()<=0) {
                    dateThese = sujDatePremiereInscription;
                }

                if (dateInsert == null || dateInsert.length()<=0) {
                    dateInsert = simpleFormat.format(new Date());
                }
                if (dateMaj == null || dateMaj.length()<=0) {
                    dateMaj = dateInsert;
                } else {
                    Date dateMajD = simpleFormat.parse(dateMaj);
                    if (dateMajD.before(simpleFormat.parse(dateInsert))) {
                        dateMaj = dateInsert;
                    }
                }

                if (dateThese == null || dateThese.length()<=0) {
                    dateThese = dateMaj;
                }

                //Si (aujourd'hui - 5 ans) < à la + récente date de soutenance ou datePremiereInscription => alors personne active
                String actif = "non";
                Calendar calDateThese = Calendar.getInstance();
                calDateThese.setTime(simpleFormat.parse(dateThese));

                actif = getCalendar(actif, calDateThese);
                cumul.append("<field name=\"dateInsert\">");
                cumul.append(dateInsert);
                cumul.append("</field>");

                cumul.append("<field name=\"dateMaj\">");
                cumul.append(dateMaj);
                cumul.append("</field>");

                cumul.append("<field name=\"actif\">");
                cumul.append(actif);
                cumul.append("</field>");

                cumul.append(mappingSolr2solrPersonne(role, doc));
            } else { //Sinon, recuperation de tous les docs associes à ce ppn
                cumul = new StringBuilder("<field name=\"id\">");
                cumul.append(ppn);
                cumul.append("</field>");

                cumul.append("<field name=\"ppn\">");
                cumul.append(ppn);
                cumul.append("</field>");

                cumul.append("<field name=\"personne\">");
                cumul.append(StringEscapeUtils.escapeXml10(nom));
                cumul.append("</field>");

                cumul.append("<field name=\"personneNP\">");
                cumul.append(StringEscapeUtils.escapeXml10(nomNP));
                cumul.append("</field>");

                //On récupère la date d'insertion (la plus vieille) et la date de maj (la plus recente) des theses liees
                URL urlDate = new URL(urlSolr.replace("update", "select/?q=") + "auteurPpn:" + ppn + "+OR+coAuteurPpn:" + ppn + "+OR+directeurThesePpn:" + ppn + "+OR+rapporteurPpn:" + ppn + "&fl=dateInsert,dateMaj,dateSoutenance,sujDatePremiereInscription&rows=1000&wt=json");
                JSONArray docs = getJsonObject(urlDate);


                Date minDateInsert = new Date();
                Date minDateMaj = simpleFormat.parse("1900-01-01T23:59:59Z");
                Date minDateThese = simpleFormat.parse("1900-01-01T23:59:59Z");
                for (int i = 0; i < docs.length(); i++) {
                    String dateInsert = docs.optJSONObject(i).optString("dateInsert");
                    String dateMaj = docs.optJSONObject(i).optString("dateMaj");
                    //On récupère, dans dateThese, la date de soutenance ou de 1ere inscription pour calculer actif/inactif
                    String dateSoutenance = docs.optJSONObject(i).optString("dateSoutenance");
                    String sujDatePremiereInscription = docs.optJSONObject(i).optString("sujDatePremiereInscription");
                    String dateThese = dateSoutenance;
                    if (dateSoutenance == null || dateSoutenance.length()<=0) {
                        dateThese = sujDatePremiereInscription;
                    }

                    if (dateInsert != null && dateInsert.length()>0) {
                        Date dateInsertD = simpleFormat.parse(dateInsert);
                        if (dateInsertD.before(minDateInsert)) {
                            minDateInsert = dateInsertD;
                        }
                    }
                    if (dateMaj != null && dateMaj.length()>0) {
                        Date dateMajD = simpleFormat.parse(dateMaj);
                        if (dateMajD.after(minDateMaj)) {
                            minDateMaj = dateMajD;
                        }
                    }
                    if (dateThese != null && dateThese.length()>0) {
                        Date dateTheseD = simpleFormat.parse(dateThese);
                        if (dateTheseD.after(minDateThese)) {
                            minDateThese = dateTheseD;
                        }
                    }
                }
                if (minDateMaj.before(minDateInsert)) {
                    minDateMaj = minDateInsert;
                }


                //Si (aujourd'hui - 5 ans) < à la + récente date de soutenance ou datePremiereInscription => alors personne active
                String actif = "non";
                Calendar calDateThese = Calendar.getInstance();
                calDateThese.setTime(minDateThese);

                actif = getCalendar(actif, calDateThese);
                cumul.append("<field name=\"dateInsert\">");
                cumul.append(simpleFormat.format(minDateInsert));
                cumul.append("</field>");

                cumul.append("<field name=\"dateMaj\">");
                cumul.append(simpleFormat.format(minDateMaj));
                cumul.append("</field>");

                cumul.append("<field name=\"actif\">");
                cumul.append(actif);
                cumul.append("</field>");

                cumul.append(cumulRolePpn(ppn));
            }
            //Envoi du doc dans solrPersonne
            StringBuilder cumulPost = new StringBuilder("<add><doc>");
            cumulPost.append(cumul);
            cumulPost.append("</doc></add>");

            if (cumulPost.toString().contains("titre_s")) //si contient au moins une these
            {
                envoieSurSolr(cumulPost.toString(), urlSolrPersonne);
            }
        }
        return indexPersonne;
    }

    private String getCalendar(String actif, Calendar calDateThese) {
        Date now = new Date();
        Calendar calNow = Calendar.getInstance();
        calNow.setTime(now);
        calNow.add(Calendar.YEAR, -5);

        if (calNow.before(calDateThese)) {
            actif = "oui";
        }
        return actif;
    }

    //Tous les resultats de tous les roles du ppn
    public String cumulRolePpn(String ppn) throws IOException, JSONException {
        StringBuilder cumulPersonne = new StringBuilder("");
        cumulPersonne.append(rolePpn(ppn, "auteur"));
        cumulPersonne.append(rolePpn(ppn, "coAuteur"));
        cumulPersonne.append(rolePpn(ppn, "directeurThese"));
        cumulPersonne.append(rolePpn(ppn, "rapporteur"));

        return cumulPersonne.toString();
    }
    //Tous les resultats pour ce role, pour ce ppn

    public String rolePpn(String ppn, String role) throws IOException, JSONException {
        StringBuilder rolePersonne = new StringBuilder("");
        URL urlPPNCumul = new URL(urlSolr.replace("update", "select/?q=") + role + "Ppn:" + ppn + "&rows=1000&wt=json");
        logger.info(urlSolr.replace("update", "select/?q=") + role + "Ppn:" + ppn + "&rows=1000&wt=json");
        JSONArray docs = getJsonObject(urlPPNCumul);
        for (int i = 0; i < docs.length(); i++) {
            if (role.contains("coAuteur"))
                role="auteur";
            rolePersonne.append(mappingSolr2solrPersonne(role, docs.optJSONObject(i)));
        }
        return rolePersonne.toString();
    }

    //Permet le mapping entre les donnees reçues de solr2, et celles a envoyer a solrPersonne
    public String mappingSolr2solrPersonne(String role, JSONObject doc) {

        StringBuilder cumul = new StringBuilder();
        cumul.append("<field name=\"role\">");
        cumul.append(role);
        cumul.append("</field>");

        Iterator<String> itr = doc.keys();

        while (itr.hasNext()) {
            String field = itr.next();
            //si on veut ce field
            if (!field.equals("id") && !field.equals("dateMaj") && !field.contains("nuage") && !field.contains("personne") && !field.contains("organisme")) {
                if (doc.optJSONArray(field) != null) {//Si c'est un tableau
                    for (int i = 0; i < doc.optJSONArray(field).length(); i++) {
                        String val = StringEscapeUtils.escapeXml10(doc.optJSONArray(field).optString(i));
                        //si le field doit etre ds la liste de resultat : _s (stocke et indexe) sinon : _z (pas stocke et pas indexe, mais sert pr les copyfields facettes et champs par defaut)
                        cumul.append(getField(field, val, role));
                    }
                } else {
                    String val = StringEscapeUtils.escapeXml10(doc.optString(field));
                    //si le field doit etre ds la liste de resultat : _s (stocke et indexe) sinon : _z (pas stocke et pas indexe, mais sert pr les copyfields facettes et champs par defaut)
                    cumul.append(getField(field, val, role));
                }
            }
        }
        return cumul.toString();
    }

    private String getField(String field, String val, String role) {
        StringBuilder cumul = new StringBuilder();
        String header = "<field name=\"";
        String footer = "</field>";
        if (field.equals("titre") || field.contains("discipline") || field.contains("etabSoutenance") || field.contains("num") || field.contains("accessible") || field.contains("status") || field.contains("dateSoutenance") || field.contains("sujDateSoutenancePrevue") || field.contains("sujDatePremiereInscription")) {
            cumul.append(header);
            cumul.append(role);
            cumul.append("-");
            cumul.append(field);
            cumul.append("_s\">");
            cumul.append(val);
            cumul.append(footer);
        } else {
            cumul.append(header);
            cumul.append(role);
            cumul.append("-");
            cumul.append(field);
            cumul.append("_z\">");
            cumul.append(val);
            cumul.append(footer);
        }
        //et tout dans text : _t

        cumul.append(header);
        cumul.append(role);
        cumul.append("-");
        cumul.append(field);
        cumul.append("_t\">");
        cumul.append(val);
        cumul.append(footer);

        return cumul.toString();
    }
    //FIN DES FONCTIONS PERSONNES

    public boolean supprimeDeSolr(int idDoc) throws IOException, JSONException, ParseException {
        logger.info("IndexationSolrDansOraclePortail supprimeDeSolr debut de la suppression de " + idDoc + " sur solr portail...");
        //Suppression des "personnes" attachées à ce doc, et recuperation des ppn/noms, pour re-générer les docs solrPersonne de ces ppns
        boolean res = false;
        HashMap<String, String> ppnPersonne = new HashMap<>(deletePersonne(idDoc));
        StringWriter sw = new StringWriter();
        postData(new StringReader("<delete><id>" + idDoc + "</id></delete>"), sw, urlSolr);
        if (sw.toString().indexOf("<int name=\"status\">0</int>") < 0) {
            logger.error("unexpected response from solr...");
        }
        postData(new StringReader("<commit/>"), sw, urlSolr);

        // solr highlight
        postData(new StringReader("<delete><query>id:" + idDoc + "</query></delete>"), sw, urlSolrHighlight);
        if (sw.toString().indexOf("<int name=\"status\">0</int>") < 0) {
            logger.info("unexpected reponse from solr...");
            logger.error("unexpected response from solr...");
        }
        postData(new StringReader("<commit/>"), sw, urlSolrHighlight);

        //Generation des docs personnes : mise a jour
        addPersonne(ppnPersonne);

        postData(new StringReader("<commit/>"), sw, urlSolrPersonne);
        logger.info(sw.toString());
        res = true;
        logger.info("commit effectue");
        return res;
    }

    /**
     * Pour la transformation XSL :
     * chargement de saxon dans oracle
     * et utilisation de net.sf.saxon.TransformerFactoryImpl
     * note : il faut utiliser un chemin de fichier sans le protocole (file://C:\\)
     * pour saxon pour charger l'xsl
     * note: il faut preciser que le tef (xmltype) est encode en utf-8 = InputStreamReader(tef.getInputStream(), "UTF-8"))
     */
    public String transfoXSL(String tef, int idDoc, String texte, String dateInsertion)
            throws TransformerException {
        InputStream stream = new ByteArrayInputStream(tef.getBytes(StandardCharsets.UTF_8));
        logger.info("transfoXsl idDoc = " + idDoc);
        logger.info("cheminXsl = " + cheminXsl);
        logger.info("idDeLaBase = " + idDoc);
        TransformerFactory tFactory = new net.sf.saxon.TransformerFactoryImpl();
        Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(cheminXsl));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.setParameter("idDeLaBase", idDoc);
        transformer.setParameter("texte", texte);
        transformer.setParameter("dateInsertion", dateInsertion);
        transformer.transform(new javax.xml.transform.stream.StreamSource(stream), new javax.xml.transform.stream.StreamResult(out));
        logger.info("sortie de indexationSolrDansOraclePortail transfoXsl" + 1);
        transformer.reset();
        return out.toString();
    }


    // ************************* SOLR HIGHLIGHT ********************************
    private void decouperIndexerSolrHighlight(String texte, int idDoc, int longueurPage)  {
        logger.info("IndexationSolrDansOraclePortail decouperIndexerSolrHighlight debut de decoupage du texte + envoi sur solrhighlight");
        logger.info("texte = " + texte);
        logger.info("iddoc = " + idDoc);
        logger.info("longueurPage = " + longueurPage);
        try {
            int numPage = 1;
            String idNumPage = idDoc + "_" + numPage;
            int borneInf = 0;
            int borneSup = longueurPage;
            while (borneInf + longueurPage < texte.length()) {
                logger.info("inf = " + borneInf);
                logger.info("sup = " + borneSup);

                String page = texte.substring(borneInf, borneSup);
                int point = page.lastIndexOf('.');
                if (point != -1) {
                    page = page.substring(0, point + 1);
                    point++;
                    borneSup = borneSup - (longueurPage - point);
                }
                idNumPage = idDoc + "_" + numPage;
                String docSolr = getDocSolrHighlight(idNumPage, idDoc, page);

                envoieSurSolr(docSolr, urlSolrHighlight);
                borneInf = borneSup;
                borneSup = borneSup + longueurPage;
                numPage++;
            }
            String dernierePage = texte.substring(borneInf);
            String docSolr = getDocSolrHighlight(idNumPage, idDoc, dernierePage);
            envoieSurSolr(docSolr, urlSolrHighlight);
            logger.info("Fin de decouperIndexerSolrHighlight iddoc="+idDoc);
        } catch (Exception e) {
            logger.error("ERREUR DANS decouperIndexer iddoc=" + idDoc + " msg:" + e.getMessage());
            //throw e; //Pr voir si ca continue...
        }
        logger.info("sortie de indexationSolrDansOraclePortail decouperIndexerSolrHighlight");
    }

    public String getDocSolrHighlight(String idNumPage, int idDoc, String extrait) {
        return "<add><doc>"
                + "<field name=\"idNumPage\">"
                + idNumPage
                + "</field>"
                + "<field name=\"texteHL\">"
                + extrait
                + "</field>"
                + "<field name=\"id\">"
                + idDoc
                + "</field>"
                + "</doc></add>";
    }
    // ************************* SOLR HIGHLIGHT FIN ********************************


    //-----------------------------------fin methodes pour Portail------------------------------------//

    public void envoieSurSolr(String docSolr, String urlSolr) throws IOException {
        final StringWriter sw = new StringWriter();
        logger.info("envoieSurSolr => urlSolr = " + urlSolr);
        postData(new StringReader(docSolr), sw, urlSolr);
        logger.info( "sw.toString() = " + sw.toString());
        logger.info( "res sw = " + sw.toString().indexOf("<int name=\"status\">0</int>"));
        if (sw.toString().indexOf("<int name=\"status\">0</int>") < 0) {
            logger.error("unexpected response from solr...");
        }
    }


    /**
     * Reads data from the data reader and posts it to solr,
     * writes to the response to output
     * @throws Exception
     */
    public void postData(Reader data, Writer output, String url) throws IOException {
        logger.info("postData");
        URL solrUrl = new URL(url);
        logger.info("url solr dans postData = " + url);
        HttpURLConnection urlc = null;
        try {
            urlc = (HttpURLConnection) solrUrl.openConnection();
            try {
                urlc.setRequestMethod("POST");
            } catch (ProtocolException e) {
                throw new ProtocolException("Shouldn't happen: HttpURLConnection doesn't support POST??");
            }
            urlc.setDoOutput(true);
            urlc.setDoInput(true);
            urlc.setUseCaches(false);
            urlc.setAllowUserInteraction(false);
            urlc.setRequestProperty("Content-type", "text/xml; charset=UTF-8");

            try (OutputStream out = urlc.getOutputStream()) {
                Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                pipe(data, writer);
                writer.close();
            } catch (IOException e) {
                throw new IOException("IOException while posting data", e);
            }

            try (InputStream in = urlc.getInputStream()) {
                Reader reader = new InputStreamReader(in);
                pipe(reader, output);
                reader.close();
            } catch (IOException e) {
                throw new IOException("IOException while reading response", e);
            }

        } catch (IOException e) {
            try {
                assert urlc != null;
                logger.info("Solr returned an error: " + urlc.getResponseMessage());
                throw new IOException("Erreur lors du post sur solr : "
                        + urlc.getResponseMessage(), e);
            } catch (IOException f) {
                logger.info("Connection error (is Solr running at " + solrUrl + " ?): " + e);
                throw new IOException("Erreur de connexion à solr", e);
            }
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
    }

    /**
     * Pipes everything from the reader to the writer via a buffer
     */
    private void pipe(Reader reader, Writer writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            writer.write(buf, 0, read);
        }
        writer.flush();

    }


}