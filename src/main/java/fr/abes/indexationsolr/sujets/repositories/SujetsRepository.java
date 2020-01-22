package fr.abes.indexationsolr.sujets.repositories;

import fr.abes.indexationsolr.sujets.entities.DocumentSujets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SujetsRepository extends JpaRepository<DocumentSujets, Integer> {

    @Query("select s.doc from DocumentSujets s where s.iddoc like :x")
    public String getTefByIddoc(@Param("x") int iddoc);

}