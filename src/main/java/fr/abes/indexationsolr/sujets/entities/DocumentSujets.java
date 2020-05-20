package fr.abes.indexationsolr.sujets.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter @Setter
public class DocumentSujets  {

    private Integer iddoc;

    private String doc;

    private String codeEtab;


    public DocumentSujets(Integer idDoc, String doc, String codeEtab) {
        this.iddoc = idDoc;
        this.doc = doc;
        this.codeEtab = codeEtab;
    }
}