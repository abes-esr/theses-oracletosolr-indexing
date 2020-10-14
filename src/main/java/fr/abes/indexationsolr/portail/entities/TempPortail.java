package fr.abes.indexationsolr.portail.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "DOCINDEXATIONSOLR")
@NoArgsConstructor
@Getter
@Setter
public class TempPortail implements Serializable  {

    @Id
    @Column(name = "IDDOC")
    private Integer iddoc;

    @ColumnTransformer(read = "NVL2(DOC, (DOC).getClobVal(), NULL)", write = "NULLSAFE_XMLTYPE(?)")
    @Lob
    @Column(name = "DOC")
    private String doc;

    @Column(name = "DATEINSERTION")
    private Date dateinsertion;

    public TempPortail(Integer iddoc, String doc, Date dateinsertion) {this.iddoc = iddoc; this.doc = doc;this.dateinsertion = dateinsertion;
    }
}