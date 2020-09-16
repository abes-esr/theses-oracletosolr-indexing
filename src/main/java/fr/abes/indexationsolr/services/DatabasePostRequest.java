package fr.abes.indexationsolr.services;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DatabasePostRequest {

    String contexte;
    String iddoc;

    // define all other variables needed.

    public DatabasePostRequest(String contexte,String iddoc){
        this.contexte = contexte ;
        this.iddoc = iddoc;
    }

    public DatabasePostRequest() {}

}
