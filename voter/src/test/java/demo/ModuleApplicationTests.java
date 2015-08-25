package demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.monitor.IntegrationMBeanExporter;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ModuleApplicationTests.TestConfig.class,
	VoterApplication.class })
@WebAppConfiguration
public class ModuleApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Configuration
	static class TestConfig {
		@Bean
		public MBeanExporter mbeanExporter() {
			return new MBeanExporter();
		}

		@Bean
		public IntegrationMBeanExporter integrationMBeanExporter() {
			return new IntegrationMBeanExporter();
		}
	}

}
