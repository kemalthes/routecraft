plugins {
    java
    id("org.springframework.boot") version "3.5.14"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.4.0"
}

group = "io.kemalthes"
version = "0.0.1-SNAPSHOT"
description = "search-service"

val jacksonNullableVersion = "0.2.6"
val springAiVersion = "1.1.6"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated-openapi/src/main/java"))
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("org.springframework.ai:spring-ai-starter-vector-store-qdrant")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.openapitools:jackson-databind-nullable:${jacksonNullableVersion}")
    implementation("io.minio:minio:9.0.0")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}
openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$projectDir/src/main/resources/openapi/api-search-v1.yml")
    outputDir.set(layout.buildDirectory.dir("generated-openapi").get().asFile.path)
    apiPackage.set("io.kemalthes.search.api")
    modelPackage.set("io.kemalthes.search.dto")
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
