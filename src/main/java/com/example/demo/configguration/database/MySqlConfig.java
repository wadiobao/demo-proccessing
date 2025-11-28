package com.example.demo.configguration.database;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.demo.sql.repository", entityManagerFactoryRef = "mysqlEntityManagerFactory", transactionManagerRef = "mysqlTransactionManager")
public class MySqlConfig {

	@Primary
	@Bean(name = "mysqlEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(EntityManagerFactoryBuilder builder,DataSource dataSource) {
		return builder.dataSource(dataSource).packages("com.example.demo.sql.entity") // Chỉ scan package này cho JPA
																						// Entity
				.build();
	}

	@Primary
	@Bean(name = "mysqlTransactionManager")
	public PlatformTransactionManager mysqlTransactionManager(
			@Qualifier("mysqlEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory.getObject());
	}
}
