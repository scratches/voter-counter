package demo;

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
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.monitor.IntegrationMBeanExporter;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.transaction.annotation.Transactional;

import lombok.Data;

@SpringBootApplication
@EnableBinding(Sink.class)
@MessageEndpoint
@EnableDiscoveryClient
public class CounterApplication {

	private static Logger logger = LoggerFactory.getLogger(CounterApplication.class);

	@Autowired
	private MetricExportProperties export;

	@Autowired
	private ElectionRepository repository;

	@ServiceActivator(inputChannel = Sink.INPUT)
	@Transactional
	public void accept(Vote vote) {

		logger.info("Received: " + vote);

		Election election = this.repository.findOne(vote.getElection());
		if (election == null) {
			election = this.repository.save(new Election());
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
		return new RedisMetricRepository(connectionFactory, this.export.getRedis()
				.getPrefix(), this.export.getRedis().getKey());
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

	@Bean
	public AlwaysSampler alwaysSampler() {
		return new AlwaysSampler();
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
