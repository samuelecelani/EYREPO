package it.ey.piao.api.repository;

import it.ey.entity.OrganoPolitico;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IOrganoPoliticoRepository extends BaseRepository<OrganoPolitico, Long> {


    @Modifying
    @Query("""
        UPDATE OrganoPolitico op
        SET op.active = false,
            op.deactivationTime = :deactivationTime
        WHERE op.id = :id
    """)
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);

}
