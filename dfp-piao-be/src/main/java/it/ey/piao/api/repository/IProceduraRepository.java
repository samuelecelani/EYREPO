package it.ey.piao.api.repository;

import it.ey.entity.Procedura;
import it.ey.entity.Sezione21;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

    @Repository
    public interface IProceduraRepository extends BaseRepository<Procedura, Long> {

        @Query("""
                    SELECT p
                    FROM Procedura p
                    WHERE p.sezione21 = :sezione21
                 """)
        List<Procedura> getProcedureBySezione21(@Param("sezione21") Sezione21 sezione21);




        @Modifying
        @Query("""
                UPDATE Procedura p
                SET p.active = false,
                    p.deactivationTime = :deactivationTime
                WHERE p.id = :id
                 """)
        void softDeleteById(@Param("id") Long id, @Param("deactivationTime") LocalDateTime deactivationTime);



    }
