package fr.abes.indexationsolr.portail.repositories;

import fr.abes.indexationsolr.portail.entities.DocumentPortail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.sql.Clob;

@Repository
public interface PortailRepository extends JpaRepository<DocumentPortail, Integer> {

    @Query("select p.doc from DocumentPortail p where p.iddoc like :x")
    public String getTefByIddoc(@Param("x") int iddoc);

    @Query("select p.texte from DocumentPortail p where p.iddoc like :x")
    public Clob getTexteByIddoc(@Param("x") int iddoc);

}
