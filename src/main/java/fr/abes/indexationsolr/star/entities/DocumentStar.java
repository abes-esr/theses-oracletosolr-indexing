package fr.abes.indexationsolr.star.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class DocumentStar {

    private Integer iddoc;

    private String doc;

    private String texte;

    private String codeEtab;

    private Integer envoiSolr;

    public DocumentStar(Integer idDoc, String doc, String texte, String codeEtab, Integer envoiSolr) {
        this.iddoc = idDoc;
        this.doc = doc;
        this.texte = texte;
        this.codeEtab = codeEtab;
        this.envoiSolr = envoiSolr;
    }
}

