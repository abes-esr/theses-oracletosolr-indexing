package fr.abes.indexationsolr.sujets.repositories;

import fr.abes.indexationsolr.sujets.entities.DocIndexation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DocIndexationRepository extends JpaRepository<DocIndexation, Integer> {

    @Query("select s.doc from DocumentSujets s where s.iddoc like :x")
    public String getTefByIddoc(@Param("x") int iddoc);

    @Transactional(transactionManager="sujetsTransactionManager")
    @Modifying
    @Query("delete from DocumentSujets s where s.iddoc like :x")
    public void deleteByIddoc(@Param("x") int iddoc);

}