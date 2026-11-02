package com.iemr.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import com.iemr.common.CommonApplication;
import com.iemr.common.config.firebase.FirebaseMessagingConfig;
import com.iemr.common.utils.CommonMain;
import com.iemr.common.utils.IEMRApplBeans;
import com.iemr.common.utils.http.HTTPRequestInterceptor;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Exercises the Spring configuration classes: each one is instantiated directly and its
 * bean factory methods are asserted to produce a usable, correctly wired object.
 */
class ConfigurationBeansTest {

	@Test
	@DisplayName("CORS mapping splits and trims the configured origin list")
	void corsConfigRegistersAllowedOrigins() {
		CorsConfig config = new CorsConfig();
		ReflectionTestUtils.setField(config, "allowedOrigins", "http://a.test , http://b.test");
		CorsRegistry registry = new CorsRegistry();

		config.addCorsMappings(registry);

		java.util.Map<String, org.springframework.web.cors.CorsConfiguration> mapping = ReflectionTestUtils
				.invokeMethod(registry, "getCorsConfigurations");
		assertThat(mapping).containsKey("/**");
		org.springframework.web.cors.CorsConfiguration cors = mapping.get("/**");
		assertThat(cors.getAllowedOriginPatterns()).containsExactly("http://a.test", "http://b.test");
		assertThat(cors.getAllowedMethods()).contains("GET", "POST", "OPTIONS");
		assertThat(cors.getExposedHeaders()).contains("Authorization", "Jwttoken");
		assertThat(cors.getAllowCredentials()).isTrue();
		assertThat(cors.getMaxAge()).isEqualTo(3600L);
	}

	@Test
	@DisplayName("the OpenAPI bean takes its server URLs from the environment")
	void swaggerConfigUsesConfiguredServerUrls() {
		MockEnvironment env = new MockEnvironment().withProperty("api.dev.url", "https://dev.test")
				.withProperty("api.uat.url", "https://uat.test");

		OpenAPI api = new SwaggerConfig().customOpenAPI(env);

		assertThat(api.getInfo().getTitle()).isEqualTo("Common API");
		assertThat(api.getServers()).extracting("url").containsExactly("https://dev.test", "https://uat.test",
				"http://localhost:9090");
		assertThat(api.getComponents().getSecuritySchemes()).containsKey("my security");
	}

	@Test
	@DisplayName("an environment without URLs falls back to the local default for every server")
	void swaggerConfigFallsBackToLocalhost() {
		OpenAPI api = new SwaggerConfig().customOpenAPI(new MockEnvironment());

		assertThat(api.getServers()).extracting("url").containsOnly("http://localhost:9090");
	}

	@Test
	@DisplayName("Redis session configuration is disabled and both templates are bound to the factory")
	void redisConfigBuildsTemplates() {
		RedisConfig config = new RedisConfig();
		RedisConnectionFactory factory = mock(RedisConnectionFactory.class);

		assertThat(config.configureRedisAction()).isSameAs(ConfigureRedisAction.NO_OP);

		RedisTemplate<String, Object> template = config.redisTemplate(factory);
		assertThat(template.getConnectionFactory()).isSameAs(factory);
		assertThat(template.getValueSerializer()).isNotNull();

		StringRedisTemplate stringTemplate = config.stringRedisTemplate(factory);
		assertThat(stringTemplate.getConnectionFactory()).isSameAs(factory);
	}

	@Test
	@DisplayName("the HTTP interceptor is registered but skips the video-consultation resolve endpoints")
	void interceptorConfigExcludesResolvePaths() {
		InterceptorConfig config = new InterceptorConfig();
		config.requestInterceptor = mock(HTTPRequestInterceptor.class);
		InterceptorRegistry registry = new InterceptorRegistry();

		config.addInterceptors(registry);

		List<?> registrations = (List<?>) ReflectionTestUtils.getField(registry, "registrations");
		assertThat(registrations).hasSize(1);
		@SuppressWarnings("unchecked")
		List<String> excluded = (List<String>) ReflectionTestUtils.getField(registrations.get(0), "excludePatterns");
		assertThat(excluded).contains("/video-consultation/resolve", "**/video-consultation/resolve");
	}

	@Test
	@DisplayName("the primary datasource is pooled with the configured credentials")
	void primaryDbConfigBuildsPooledDataSource() {
		org.apache.tomcat.jdbc.pool.DataSource dataSource = new PrimaryDBConfig().dataSource();

		assertThat(dataSource.getPoolProperties().getMaxActive()).isEqualTo(30);
		assertThat(dataSource.getPoolProperties().getMinIdle()).isEqualTo(5);
		assertThat(dataSource.getPoolProperties().getValidationQuery()).isEqualTo("SELECT 1");
		assertThat(dataSource.getPoolProperties().isTestOnBorrow()).isTrue();
	}

	@Test
	@DisplayName("the secondary datasource is pooled the same way as the primary one")
	void secondaryDbConfigBuildsPooledDataSource() {
		org.apache.tomcat.jdbc.pool.DataSource dataSource = new SecondaryDBConfig().dataSource();

		assertThat(dataSource.getPoolProperties().getMaxIdle()).isEqualTo(15);
		assertThat(dataSource.getPoolProperties().getMaxWait()).isEqualTo(10000);
		assertThat(dataSource.getPoolProperties().isRemoveAbandoned()).isTrue();
		assertThat(dataSource.getPoolProperties().getRemoveAbandonedTimeout()).isEqualTo(600);
	}

	@Test
	@DisplayName("Firebase messaging is not built when the feature is switched off")
	void firebaseMessagingDisabledReturnsNull() throws Exception {
		FirebaseMessagingConfig config = new FirebaseMessagingConfig();
		ReflectionTestUtils.setField(config, "firebaseEnabled", false);

		assertThat(config.firebaseMessaging()).isNull();
	}

	@Test
	@DisplayName("Firebase messaging is not built when no credential file is configured")
	void firebaseMessagingWithoutCredentialsReturnsNull() throws Exception {
		FirebaseMessagingConfig config = new FirebaseMessagingConfig();
		ReflectionTestUtils.setField(config, "firebaseEnabled", true);
		ReflectionTestUtils.setField(config, "firebaseCredentialFile", "  ");

		assertThat(config.firebaseMessaging()).isNull();
	}

	@Test
	@DisplayName("a Firebase credential file that cannot be read leaves the application running")
	void firebaseMessagingSurvivesUnreadableCredentials() throws Exception {
		FirebaseMessagingConfig config = new FirebaseMessagingConfig();
		ReflectionTestUtils.setField(config, "firebaseEnabled", true);
		ReflectionTestUtils.setField(config, "firebaseCredentialFile", "does-not-exist.json");

		assertThat(config.firebaseMessaging()).isNull();
	}

	@Test
	@DisplayName("the shared application beans are all instantiable")
	void iemrApplBeansProducesEveryBean() {
		IEMRApplBeans beans = new IEMRApplBeans();
		ReflectionTestUtils.setField(beans, "redisHost", "localhost");
		ReflectionTestUtils.setField(beans, "redisPort", 6379);

		assertThat(beans.getVaidator()).isNotNull();
		assertThat(beans.getEmailService()).isNotNull();
		assertThat(beans.getJavaMailSender()).isNotNull();
		assertThat(beans.configProperties()).isNotNull();
		assertThat(beans.sessionObject()).isNotNull();
		assertThat(beans.redisStorage()).isNotNull();
		assertThat(beans.connectionFactory().getHostName()).isEqualTo("localhost");
	}

	@Test
	@DisplayName("the WAR entry point exposes its beans and its servlet configuration")
	void commonApplicationBeans() {
		CommonApplication application = new CommonApplication();

		assertThat(application.instantiateBeans()).isNotNull();
		assertThat(application.restTemplate()).isNotNull();

		RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
		assertThat(application.redisTemplate(factory).getConnectionFactory()).isSameAs(factory);

		org.springframework.boot.builder.SpringApplicationBuilder builder = new org.springframework.boot.builder.SpringApplicationBuilder();
		Object configured = ReflectionTestUtils.invokeMethod(application, "configure", builder);
		assertThat(configured).isSameAs(builder);
	}

	@Test
	@DisplayName("the legacy main configuration builds its executor and supporting beans")
	void commonMainBeans() {
		CommonMain main = new CommonMain();

		assertThat(main.configProperties()).isNotNull();
		assertThat(main.redisSession()).isNotNull();
		assertThat(main.getOpenKMService()).isNotNull();
		assertThat(main.redisStorage()).isNotNull();

		Executor executor = main.asyncExecutor();
		assertThat(executor).isInstanceOf(org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor.class);
		org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor pool = (org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor) executor;
		assertThat(pool.getCorePoolSize()).isEqualTo(2);
		assertThat(pool.getMaxPoolSize()).isEqualTo(2);
		assertThat(pool.getThreadNamePrefix()).isEqualTo("IEMRLookup-");
		pool.shutdown();
	}
}
