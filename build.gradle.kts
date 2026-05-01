plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.4.0"
}

group = "io.kemalthes"
version = "0.0.1-SNAPSHOT"
description = "semester-work-3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated-openapi/src/main/java"))
        }
    }
}

val openapiVersion = "2.6.0"
val jacksonNullableVersion = "0.2.6"
val mapstructVersion = "1.5.5.Final"
val minioVersion = "9.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
//    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.liquibase:liquibase-core")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$openapiVersion")
    implementation("org.openapitools:jackson-databind-nullable:${jacksonNullableVersion}")
    implementation("io.minio:minio:${minioVersion}")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$projectDir/src/main/resources/openapi/api-core-v1.yml")
    outputDir.set(layout.buildDirectory.dir("generated-openapi").get().asFile.path)
    apiPackage.set("io.kemalthes.core.api")
    modelPackage.set("io.kemalthes.core.dto")
    typeMappings.set(mapOf("URI" to "String"))
    globalProperties.set(mapOf(
        "apis" to "",
        "models" to "",
        "apiTests" to "false",
        "modelTests" to "false",
        "supportingFiles" to "ApiUtil.java"
    ))
    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "useTags" to "true",
        "useSpringBoot3" to "true",
        "generateBuilders" to "true",
        "useBeanValidation" to "true",
        "performBeanValidation" to "true",
        "useJakartaEe" to "true"
    ))
}

tasks.compileJava {
    dependsOn(tasks.openApiGenerate)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
