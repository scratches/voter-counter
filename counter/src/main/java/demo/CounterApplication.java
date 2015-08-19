package demo;

import lombok.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ExportMetricReader;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.metrics.export.MetricExportProperties;
import org.springframework.boot.actuate.metrics.integration.SpringIntegrationMetricReader;
import org.springframework.boot.actuate.metrics.jmx.JmxMetricWriter;
import org.springframework.boot.actuate.metrics.repository.redis.RedisMetricRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.bus.runner.EnableMessageBus;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.monitor.IntegrationMBeanExporter;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@EnableMessageBus
@MessageEndpoint
@EnableDiscoveryClient
public class CounterApplication {

	private static Logger logger = LoggerFactory.getLogger(CounterApplication.class);

	@Autowired
	private MetricExportProperties export;

	@Autowired
	private ElectionRepository repository;

	@Bean
	public MessageChannel input() {
		return new DirectChannel();
	}

	@ServiceActivator(inputChannel = "input")
	@Transactional
	public void accept(Vote vote) {
		logger.info("Received: " + vote);
		Election election = repository.findOne(vote.getElection());
		if (election == null) {
			election = repository.save(new Election());
		}
		Candidate candidate = election.getCandidate(vote.getCandidate());
		if (candidate == null) {
			candidate = new Candidate();
			election.getCandidates().add(candidate);
		}
		candidate.setScore(candidate.getScore() + vote.getScore());
	}

	@Bean
	@ExportMetricWriter
	public RedisMetricRepository redisMetricWriter(
			RedisConnectionFactory connectionFactory) {
		return new RedisMetricRepository(connectionFactory, this.export.getRedis().getPrefix(),
				this.export.getRedis().getKey());
	}

	@Bean
	@ExportMetricWriter
	public JmxMetricWriter jmxMetricWriter(
			@Qualifier("mbeanExporter") MBeanExporter exporter) {
		return new JmxMetricWriter(exporter);
	}

	@Bean
	@ExportMetricReader
	public SpringIntegrationMetricReader springIntegrationMetricReader(
			IntegrationMBeanExporter exporter) {
		return new SpringIntegrationMetricReader(exporter);
	}

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(CounterApplication.class, args);
	}

}

@Data
class Vote {
	private long election;
	private long candidate;
	private int score;
}
