package com.example.demo.config.database;

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
@EnableJpaRepositories(
		basePackages = {
				// Identity module
				"com.example.demo.modules.identity.infrastructure.persistence.repository",
				// Community module
				"com.example.demo.modules.community.forum.infrastructure.persistence.repository",
				"com.example.demo.modules.community.reputation.infrastructure.persistence.repository",
				// Quiz module
				"com.example.demo.modules.quiz.graph.infrastructure.persistence.repository",
				// Document module
				"com.example.demo.modules.document.shared.domain.repository",
				"com.example.demo.modules.document.shared.infrastructure.persistence"
		},
		entityManagerFactoryRef = "mysqlEntityManagerFactory",
		transactionManagerRef = "mysqlTransactionManager")
public class MySqlConfig {

	@Primary
	@Bean(name = "mysqlEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(EntityManagerFactoryBuilder builder, DataSource dataSource) {
		return builder.dataSource(dataSource)
				.packages(
						// Identity module JPA entities
						"com.example.demo.modules.identity.infrastructure.persistence.entity",
						// Community module JPA entities
						"com.example.demo.modules.community.forum.infrastructure.persistence.entity",
						"com.example.demo.modules.community.reputation.infrastructure.persistence.entity",
						// Quiz module JPA entities
						"com.example.demo.modules.quiz.graph.infrastructure.persistence.entity",
						// Document module
						"com.example.demo.modules.document.shared.domain.model"
				)
				.build();
	}

	@Primary
	@Bean(name = "mysqlTransactionManager")
	public PlatformTransactionManager mysqlTransactionManager(
			@Qualifier("mysqlEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory.getObject());
	}
}
