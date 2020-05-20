package fr.abes.indexationsolr.portail.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Clob;
import java.util.Date;


@NoArgsConstructor
@Getter @Setter
public class DocumentPortail {


    private Integer iddoc;

    private String nnt;

    private String doc;

    private Clob texte;

    private String codeEtab;

    private Date dateinsertion;

    private Date datediffusion;

    private String droits;

    private String numsujet;

    private Integer envoiSolr;


    public DocumentPortail(Integer iddoc, String nnt, String doc, Clob texte, String codeEtab, Date dateinsertion, Date datediffusion, String droits, String numsujet, Integer envoiSolr) {
        this.iddoc = iddoc;
        this.nnt = nnt;
        this.doc = doc;
        this.texte = texte;
        this.codeEtab = codeEtab;
        this.dateinsertion = dateinsertion;
        this.datediffusion = datediffusion;
        this.droits = droits;
        this.numsujet = numsujet;
        this.envoiSolr = envoiSolr;
    }
}

