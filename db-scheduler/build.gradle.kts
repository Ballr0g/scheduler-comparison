plugins {
	kotlin("jvm") version "2.0.21"
	kotlin("plugin.spring") version "2.0.21"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "io.scheduler.comparison.dbscheduler"
version = "0.0.1-SNAPSHOT"

val liquibaseVersion: String by project
val postgresVersion: String by project
val kotlinLoggingVersion: String by project
val dbSchedulerVersion: String by project
val arrowKtVersion: String by project

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// --> Implementation Dependencies <--
	// Spring Boot starter JDBC
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	// db-scheduler Spring Boot starter
	implementation("com.github.kagkarlsson:db-scheduler-spring-boot-starter:${dbSchedulerVersion}")
	// Spring Kafka.
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	// Liquibase.
	implementation("org.liquibase:liquibase-core:${liquibaseVersion}")
	// PostgreSQL.
	implementation("org.postgresql:postgresql:${postgresVersion}")
	// Kotlin logging
	implementation("io.github.oshai:kotlin-logging:${kotlinLoggingVersion}")
	implementation("org.hibernate.validator:hibernate-validator")
	// Jackson
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	// Arrow (Either)
	implementation("io.arrow-kt:arrow-core:${arrowKtVersion}")


	// --> Test Implementation Dependencies <--
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

	// --> Test Runtime-Only Dependencies <--
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// --> Development-Only Dependencies <--
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
