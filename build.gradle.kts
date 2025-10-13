import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.2.10"
    java
    id("com.gradleup.shadow") version "8.3.0"
    kotlin("plugin.serialization") version "2.2.10"
    id("com.diffplug.spotless") version "6.25.0" apply false
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc-repo"
        }
    }

    dependencies {
        // compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

        implementation("org.jetbrains.exposed:exposed-core:0.52.0")
        implementation("org.jetbrains.exposed:exposed-dao:0.52.0")
        implementation("org.jetbrains.exposed:exposed-jdbc:0.52.0")
        implementation("org.jetbrains.exposed:exposed-java-time:0.52.0")

        implementation("com.zaxxer:HikariCP:5.1.0")
        implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")
        // implementation("io.javalin:javalin:6.7.0")
        // implementation("org.slf4j:slf4j-simple:2.0.16")
        // implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            ktlint()
                .setEditorConfigPath("$rootDir/.editorconfig") // 루트의 .editorconfig 파일을 사용하도록 지정
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
        }
    }
}

group = "kr.doka.lab"
version = "1.0-SNAPSHOT"

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    /* dependencies {
        include(dependency("net.dv8tion:JDA:5.6.1"))
    }*/
    /* dependsOn(":discord-sync-core:jar", ":discord-sync-api:jar", ":discord-sync-plugin:jar", ":discord-sync-auth:jar", ":discord-sync-bot:jar")
    from(project(":discord-sync-core").extensions.getByType<SourceSetContainer>()["main"].output)
    from(project(":discord-sync-api").extensions.getByType<SourceSetContainer>()["main"].output)
    from(project(":discord-sync-plugin").extensions.getByType<SourceSetContainer>()["main"].output)
    from(project(":discord-sync-auth").extensions.getByType<SourceSetContainer>()["main"].output)
    from(project(":discord-sync-bot").extensions.getByType<SourceSetContainer>()["main"].output)*/
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
