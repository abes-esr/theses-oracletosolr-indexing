package fr.abes.indexationsolr.portail.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Clob;
import java.util.Date;

@Entity
@Table(name = "DOCUMENT")
@NoArgsConstructor
@Getter
@Setter
public class DocumentPortail implements Serializable{

    @Id
    @Column(name = "IDDOC")
    private Integer iddoc;

    @Column(name = "NNT")
    private String nnt;

    @ColumnTransformer(read = "NVL2(DOC, (DOC).getClobVal(), NULL)", write = "NULLSAFE_XMLTYPE(?)")
    @Lob
    @Column(name = "DOC")
    private String doc;

    @Column(name = "TEXTE")
    private String texte;

    @Column(name = "CODEETAB")
    private String codeEtab;

    @Column(name = "DATEINSERTION")
    private Date dateinsertion;

    @Column(name = "DATEDIFFUSION")
    private Date datediffusion;

    @Column(name = "DROITS")
    private String droits;

    @Column(name = "NUMSUJET")
    private String numsujet;

    @Column(name = "ENVOISOLR")
    private Integer envoiSolr;


    public DocumentPortail(Integer iddoc, String nnt, String doc, String texte, String codeEtab, Date dateinsertion, Date datediffusion, String droits, String numsujet, Integer envoiSolr) {
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

