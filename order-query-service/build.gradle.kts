import org.springframework.boot.buildpack.platform.build.PullPolicy

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.hibernate.orm")
    id("org.graalvm.buildtools.native")
}

dependencies {
    implementation(project(":shared"))

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-flyway")

    // Kotlin support
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")

    // OpenAPI Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.11")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.rest-assured:rest-assured:5.5.6")
    testImplementation("io.rest-assured:kotlin-extensions:5.5.6")
    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql:1.21.3")
    testImplementation("org.testcontainers:rabbitmq")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

hibernate {
    enhancement {
        enableAssociationManagement = true
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootBuildImage {
    builder.set("paketobuildpacks/builder-jammy-buildpackless-tiny:latest")
    if (project.hasProperty("native")) {
        buildpacks.set(listOf("paketobuildpacks/java-native-image"))
    }

    environment.set(
        mutableMapOf<String, String>("BP_JVM_VERSION" to "21", "BP_JVM_TYPE" to "JRE").apply {
            if (project.hasProperty("native")) {
                put("BP_NATIVE_IMAGE", "true")
                put(
                    "BP_NATIVE_IMAGE_BUILD_ARGUMENTS",
                    """
                    --no-fallback
                    --enable-url-protocols=http,https
                    -H:+ReportExceptionStackTraces
                    """.trimIndent()
                        .replace("\n", " "),
                )
            }
        },
    )

    pullPolicy.set(PullPolicy.IF_NOT_PRESENT)

    imageName.set("${project.group}/${project.name}:${project.version}")

    tags.set(
        listOf(
            "${project.group}/${project.name}:latest",
            "${project.group}/${project.name}:${project.version}",
        ),
    )
}

graalvmNative { binaries { named("main") { imageName.set("${project.group}.${project.name}") } } }
