package it.ey.sync.repository;

import it.ey.sync.entity.DocumentoPiao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoPiaoRepository extends JpaRepository<DocumentoPiao, String> {

    @Modifying
    @Query("DELETE FROM DocumentoPiao d WHERE d.id NOT IN :ids")
    void deleteByIdNotIn(@Param("ids") List<String> ids);
}

