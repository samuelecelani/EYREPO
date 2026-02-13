package it.ey.piao.api.health;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "live")
public class LiveCheck {

    @ReadOperation
    public Health call() {

		/* get mem resources */
		MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
		long memUsed = memBean.getHeapMemoryUsage().getUsed();
		long memMax = memBean.getHeapMemoryUsage().getMax();

        Health.Builder status = Health.up();
        if (!testMem(memUsed, memMax)) {
            status = Health.down();
        }

        Map<String, Object> details = new HashMap<>();
        details.put("memory used", memUsed);
        details.put("memory max", memMax);
                System.out.println(details);


        return status.withDetails(details).build();
    }

	private boolean testMem(long memUsed, long memMax) {
		/* check if memory is suitable to liveness */
		return memUsed < memMax * 0.9;
	}
}
