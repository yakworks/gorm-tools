package restify

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled

class AgingJob {

    @Scheduled(cron = "*/30 * * * * *")
    @SchedulerLock(name = "orgCalcAging", lockAtMostFor = "1m", lockAtLeastFor = "1m")
    public void scheduledTask() {
        println("Hello from job")
    }
}
