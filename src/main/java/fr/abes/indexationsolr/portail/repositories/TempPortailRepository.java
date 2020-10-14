
package fr.abes.indexationsolr.portail.repositories;

import fr.abes.indexationsolr.portail.entities.TempPortail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TempPortailRepository extends JpaRepository<TempPortail, Integer> {

    @Query("select t.iddoc from TempPortail t")
    public List<Integer> iddocsFromTemp();

    @Query("select t.doc from TempPortail t where t.iddoc like :x")
    public String getTefByIddoc(@Param("x") int iddoc);

    @Query("select t.dateinsertion from TempPortail t where t.iddoc like :x")
    public String getDateInsertionByIddoc(@Param("x") int iddoc);

}
