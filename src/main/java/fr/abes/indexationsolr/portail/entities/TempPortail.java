package fr.abes.indexationsolr.portail.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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

    @Column(name = "DATEINSERTION")
    private Date dateinsertion;


    public TempPortail(Integer iddoc, Date dateinsertion) {this.iddoc = iddoc; this.dateinsertion = dateinsertion;
    }
}