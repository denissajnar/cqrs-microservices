plugins {
    kotlin("jvm") version "2.2.0" apply false
    kotlin("plugin.spring") version "2.2.0" apply false
    id("org.springframework.boot") version "4.0.0-SNAPSHOT" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.hibernate.orm") version "7.1.0.Final" apply false
    id("com.google.protobuf") version "0.9.4" apply false
    kotlin("plugin.jpa") version "2.2.0" apply false
}

allprojects {
    group = "dev.denissajnar"
    version = "0.0.1-SNAPSHOT"

    repositories {
        maven { url = uri("https://repo.spring.io/snapshot") }
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    apply(plugin = "io.spring.dependency-management")

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.0-SNAPSHOT")
            mavenBom("io.grpc:grpc-bom:1.75.0")
        }
        dependencies {
            dependency("com.google.protobuf:protobuf-kotlin:4.32.0")
            dependency("io.grpc:grpc-kotlin-stub:1.4.3")
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

