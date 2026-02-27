package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.RichiestaApprovazione;
import it.ey.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IRichiestaApprovazioneRepository extends BaseRepository<RichiestaApprovazione,Long> {

    public RichiestaApprovazione findByPiao(Piao piao);

    @Query("SELECT ra FROM RichiestaApprovazione ra WHERE ra.piao.id = :idPiao")
    RichiestaApprovazione findByPiaoId(Long idPiao);

}
