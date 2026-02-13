package it.ey.entity;




import it.ey.dto.TestDTO;
import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Test")
public class Test extends CampiTecnici {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rev_id_generator")
    @SequenceGenerator(name = "rev_id_generator", sequenceName = "test_seq", allocationSize = 1)
    @Column(name = "id", nullable = false, columnDefinition = "NUMBER(32,0)")
    private Long id;
    @Column(name = "testo")
    private String testo;


    //Costruttuore per mapping object api con entity
    public  Test(TestDTO test) {
        if (test != null) {
            this.testo = test.getTesto();
            this.setCreatedBy(test.getCreatedBy());
            this.setValidity(test.getValidity());
            this.setUpdatedTs(test.getUpdatedTs());
            this.setCreatedTs(test.getCreatedTs());
        }
    }




}
