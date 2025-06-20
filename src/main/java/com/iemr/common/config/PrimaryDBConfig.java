/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.config;



import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.iemr.common.utils.config.ConfigProperties;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory", basePackages = { "com.iemr.common.repository",
		"com.iemr.common.repo", "com.iemr.common.notification.agent", "com.iemr.common.covidVaccination", "com.iemr.common.repository.everwell.*", "com.iemr.common.data.grievance", "com.iemr.common.repository.users" })
public class PrimaryDBConfig {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());


	@Primary
	@Bean(name = "dataSource")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		PoolConfiguration p = new PoolProperties();
		p.setMaxActive(30);
		p.setMaxIdle(15);
		p.setMinIdle(5);
		p.setInitialSize(5);
		p.setMaxWait(10000);
		p.setMinEvictableIdleTimeMillis(15000);
		p.setRemoveAbandoned(true);
		p.setLogAbandoned(true);
		p.setRemoveAbandonedTimeout(600);
		p.setTestOnBorrow(true);
		p.setValidationQuery("SELECT 1");
		org.apache.tomcat.jdbc.pool.DataSource datasource = new org.apache.tomcat.jdbc.pool.DataSource();
		datasource.setPoolProperties(p);

		datasource.setUsername(ConfigProperties.getPropertyByName("spring.datasource.username"));
		datasource.setPassword(ConfigProperties.getPropertyByName("spring.datasource.password"));

		return datasource;
	}

	@Primary
	@Bean(name = "entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier("dataSource") DataSource dataSource) {
		return builder.dataSource(dataSource).packages("com.iemr.common.data", "com.iemr.common.notification",
				"com.iemr.common.model", "com.iemr.common.covidVaccination", "com.iemr.common.data.everwell", "com.iemr.common.data.grievance", "com.iemr.common.data.users").persistenceUnit("db_iemr").build();
	}

	@Primary
	@Bean(name = "transactionManager")
	public PlatformTransactionManager transactionManager(
			@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager((jakarta.persistence.EntityManagerFactory) entityManagerFactory);
	}
}
