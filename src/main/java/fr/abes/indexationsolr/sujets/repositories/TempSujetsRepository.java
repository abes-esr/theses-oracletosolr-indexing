package fr.abes.indexationsolr.sujets.repositories;

import fr.abes.indexationsolr.sujets.entities.TempSujets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface TempSujetsRepository extends JpaRepository<TempSujets, Integer> {

    @Query("select t.iddoc from TempSujets t")
    public List<Integer> iddocsFromTemp();

    @Query("select s.doc from TempSujets s where s.iddoc like :x")
    public String getTefByIddoc(@Param("x") int iddoc);

    String getDateInsertionByIddoc(int iddoc);
}