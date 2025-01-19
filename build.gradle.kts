plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.kiralyzoltan"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
	create("mockitoAgent")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	// https://mvnrepository.com/artifact/junit/junit
	testImplementation("junit:junit:4.13.2")
	// https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3")
	// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa
	implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.1")
	// https://mvnrepository.com/artifact/org.mapstruct/mapstruct
	implementation("org.mapstruct:mapstruct:1.6.3")
	// https://mvnrepository.com/artifact/org.mapstruct/mapstruct-processor
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
	// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
	implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
	// https://mvnrepository.com/artifact/com.h2database/h2
	testImplementation("com.h2database:h2:2.3.232")
	// https://mvnrepository.com/artifact/org.mockito/mockito-junit-jupiter
	testImplementation("org.mockito:mockito-junit-jupiter:5.15.2")
	// Add Mockito agent configuration
	"mockitoAgent"("org.mockito:mockito-core:5.15.2") {
		isTransitive = false
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs("-javaagent:${configurations["mockitoAgent"].singleFile.absolutePath}")
}