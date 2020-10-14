package fr.abes.indexationsolr.star.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "DOCINDEXATIONSOLR")
@NoArgsConstructor
@Getter
@Setter
public class TempStar implements Serializable  {

    @Id
    @Column(name = "IDDOC")
    private Integer iddoc;


    public TempStar(Integer iddoc) {
        this.iddoc = iddoc;
    }
}