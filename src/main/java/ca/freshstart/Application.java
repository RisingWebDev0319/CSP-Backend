package ca.freshstart;

import ca.freshstart.applications.initialization.AppDbInitializer;
import ca.freshstart.applications.initialization.HealthTableInitializer;
import ca.freshstart.applications.initialization.MockData;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class Application {

	@PostConstruct
	void started() { TimeZone.setDefault(TimeZone.getTimeZone("UTC")); }

//	private static final Logger log = LoggerFactory.getLogger(Application.class);
	private static Logger log = Logger.getLogger(Application.class);

	public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(Application.class);

        springApplication.addListeners(new ApplicationPidFileWriter());
        springApplication.run(args);
	}

    @Bean
    public SchedulerFactoryBean quartzScheduler() {
        return new SchedulerFactoryBean();
    }


	// for production
	@Autowired
	private AppDbInitializer appDbInitializer;

	// for test
	@Autowired
	private MockData mockData;

	@Autowired
	private HealthTableInitializer healthTableInitializer;

	@Bean
	public CommandLineRunner demo() {
		return (args) -> {
			// for production
			appDbInitializer.init();
			healthTableInitializer.init();

			// for test
			mockData.mock();
        };
	}
}