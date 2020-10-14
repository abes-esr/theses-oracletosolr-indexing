package fr.abes.indexationsolr.sujets.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "DOCINDEXATIONSOLR")
@NoArgsConstructor
@Getter
@Setter
public class TempSujets implements Serializable  {

    @Id
    @Column(name = "IDDOC")
    private Integer iddoc;

    @ColumnTransformer(read = "NVL2(DOC, (DOC).getClobVal(), NULL)", write = "NULLSAFE_XMLTYPE(?)")
    @Lob
    @Column(name = "DOC")
    private String doc;

    @Column(name = "CODEETAB")
    private String codeEtab;


    public TempSujets(Integer idDoc, String doc, String codeEtab) {
        this.iddoc = idDoc;
        this.doc = doc;
        this.codeEtab = codeEtab;
    }
}