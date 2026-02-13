package it.ey.externaldto;
import it.ey.externaldto.externalenum.Privilege;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {

    private String id;

    private String name;

    private List<Privilege> privileges;
}
