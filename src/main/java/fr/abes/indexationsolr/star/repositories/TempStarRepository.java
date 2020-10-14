
package fr.abes.indexationsolr.star.repositories;

import fr.abes.indexationsolr.star.entities.TempStar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TempStarRepository extends JpaRepository<TempStar, Integer> {

    @Query("select t.iddoc from TempStar t")
    public List<Integer> iddocsFromTemp();

    @Query("select t.doc from TempStar t where t.iddoc like :x")
    public String getTefByIddoc(@Param("x") int iddoc);

}
