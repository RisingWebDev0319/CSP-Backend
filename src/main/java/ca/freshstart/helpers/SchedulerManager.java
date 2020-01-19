package ca.freshstart.helpers;

import ca.freshstart.data.remoteServerSettings.entity.RemoteServerSettings;
import ca.freshstart.helpers.remote.RemoteSettingsManager;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
@RequiredArgsConstructor
public class SchedulerManager implements InitializingBean, Job {

    private final RemoteSettingsManager remoteManager;

    private final Scheduler scheduler;

    public void updateDataFromRemote() {
        remoteManager.updateDataFromRemote();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        JobDetail job = newJob(SchedulerManager.class)
                .withIdentity("job1", "group1")
                .build();

        CronTrigger trigger = newTrigger()
                .withIdentity("trigger1", "group1")
                .withSchedule(cronSchedule("0 0 1 * * ?"))
//                .withSchedule(cronSchedule(remoteManager.getRemoteSettings().getCronExpression()))
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        this.updateDataFromRemote();
    }

    public void rescheduleUpdate(RemoteServerSettings serverSettings) {

        CronTriggerImpl cronTrigger = null;

        try {
            cronTrigger = (CronTriggerImpl) scheduler.getTrigger(new TriggerKey("trigger1", "group1"));

            cronTrigger.setCronExpression(serverSettings.getCronExpression());
            cronTrigger.setTimeZone(TimeZone.getTimeZone(serverSettings.getCronTimezone()));
            System.out.println("@!!@!@@!@!@!@  serverSettings.getCronTimezone() ^%^%^^%^%^" + serverSettings.getCronTimezone());
            System.out.println("$#$#$#$#$#$#$##$ TimeZone.getTimeZone(serverSettings.getCronTimezone())  4343343434343" + TimeZone.getTimeZone(serverSettings.getCronTimezone()));

            scheduler.rescheduleJob(cronTrigger.getKey(), cronTrigger);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
