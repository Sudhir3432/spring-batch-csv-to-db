package com.sipl.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.sipl.batch.model.User;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfig {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public FlatFileItemReader<User> reader() {
		try {
			log.info("<<start>> In reader method <<start>>");
			FlatFileItemReader<User> reader = new FlatFileItemReader<>();
			reader.setResource(new ClassPathResource("brecords.csv"));
			reader.setLineMapper(getLineMapper());
			reader.setLinesToSkip(1);
			
			log.info("<<end>> In reader method <<end>>");
			return reader;
		} catch (Exception e) {
			log.error("FlatFileItemReader Exception " , e);
		}
		return null;
	}

	private LineMapper<User> getLineMapper() {
		log.info("<<start>> In getLineMapper method <<start>>");
		DefaultLineMapper<User> lineMapper = new DefaultLineMapper<>();

		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		//lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames(new String[] { "userId", "namePrefix", "firstName", "lastName" });
		lineTokenizer.setIncludedFields(new int[] {0,1,2,3});
		BeanWrapperFieldSetMapper<User> fieldSetter = new BeanWrapperFieldSetMapper<>();
		fieldSetter.setTargetType(User.class);
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetter);
		log.info("<<end>> In getLineMapper method <<end>>");
		return lineMapper;
	}

	@Bean
	public UserItemProcessor processor() {
		log.info("Calling processor method");
		return new UserItemProcessor();
	}

	@Bean
	public JdbcBatchItemWriter<User> writer() {
		log.info("<<start>> In writer method <<start>>");
		JdbcBatchItemWriter<User> writer = new JdbcBatchItemWriter<User>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<User>());

		writer.setSql(
				" insert into [user] (userId,namePrefix,firstName,lastName) values (:userId,:namePrefix,:firstName,:lastName)");
		writer.setDataSource(this.dataSource);
		log.info("<<end>> In writer method <<end>>");
		return writer;
	}

	@Bean
	public Job importUserJob() {

		log.info("calling importUserJob method");
		return this.jobBuilderFactory.get("USER-IMPORT-JOB").incrementer(new RunIdIncrementer()).flow(step1()).end()
				.build();
	}

	@Bean
	public Step step1() {
		log.info("calling step1 method");
		return this.stepBuilderFactory.get("step1").<User, User>chunk(5).reader(reader()).processor(processor())
				.writer(writer())
				.faultTolerant().skipLimit(10)
                .skip(RuntimeException.class)
                .build();
	}
	
/*	@Bean
	public Flow splitFlow() {
	    return new FlowBuilder<SimpleFlow>("splitFlow")
	        .split(taskExecutor())
	        .add(flow1(), flow2())
	        .build();
	}
	@Bean
	public TaskExecutor taskExecutor() {
	    return new SimpleAsyncTaskExecutor("spring_batch");
	}
	
	@Bean
	public Flow flow1() {
	    return new FlowBuilder<SimpleFlow>("flow1")
	        .start(step1())
	        .build();
	}

	@Bean
	public Flow flow2() {
	    return new FlowBuilder<SimpleFlow>("flow2")
	        .start(step2())
	        .build();
	}
	
	@Bean
	public Step step2() {
		log.info("calling step1 method");
		return this.stepBuilderFactory.get("step2").<User, User>chunk(5).reader(reader()).processor(processor())
				.writer(writer2())
				.faultTolerant().skipLimit(10)
                .skip(RuntimeException.class)
                .build();
	}
	
	@Bean
	public JdbcBatchItemWriter<User> writer2() {
		log.info("<<start>> In writer method <<start>>");
		JdbcBatchItemWriter<User> writer = new JdbcBatchItemWriter<User>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<User>());

		writer.setSql(
				" insert into [user] (userId,namePrefix,firstName,lastName) values (:userId,:namePrefix,:firstName,:lastName)");
		writer.setDataSource(this.dataSource);
		log.info("<<end>> In writer method <<end>>");
		return writer;
	} */
}
