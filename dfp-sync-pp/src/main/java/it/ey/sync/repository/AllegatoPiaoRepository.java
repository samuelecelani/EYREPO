package it.ey.sync.repository;

import it.ey.sync.entity.AllegatoPiao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllegatoPiaoRepository extends JpaRepository<AllegatoPiao, Integer> {

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AllegatoPiao a WHERE a.documentoPiao.id = :idPiao AND a.nomeFile = :nomeFile")
    boolean existsByIdPiaoAndNomeFile(@Param("idPiao") String idPiao, @Param("nomeFile") String nomeFile);

    @Query("SELECT a.nomeFile FROM AllegatoPiao a WHERE a.documentoPiao.id = :idPiao")
    List<String> findNomeFilesByDocumentoPiaoId(@Param("idPiao") String idPiao);

    @Modifying
    @Query("DELETE FROM AllegatoPiao a WHERE a.documentoPiao.id = :idPiao AND a.nomeFile NOT IN :nomiFile")
    void deleteByDocumentoPiaoIdAndNomeFileNotIn(@Param("idPiao") String idPiao, @Param("nomiFile") List<String> nomiFile);

    @Modifying
    @Query("DELETE FROM AllegatoPiao a WHERE a.documentoPiao.id = :idPiao")
    void deleteByDocumentoPiaoId(@Param("idPiao") String idPiao);
}

