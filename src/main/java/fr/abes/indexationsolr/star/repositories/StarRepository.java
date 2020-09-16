package fr.abes.indexationsolr.star.repositories;

import fr.abes.indexationsolr.star.entities.DocumentStar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StarRepository extends JpaRepository<DocumentStar, Integer> {


    @Query("select s.doc from DocumentStar s where s.iddoc like :x")
    public String getTefByIddoc(@Param("x") int iddoc);

}