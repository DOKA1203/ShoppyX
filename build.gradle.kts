import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.shadow) apply true
    alias(libs.plugins.spotless) apply false
}

group = "kr.doka.lab"
version = "1.0-SNAPSHOT"

subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        repositories {
            mavenCentral()
            maven("https://repo.papermc.io/repository/maven-public/") {
                name = "papermc-repo"
            }
            maven("https://jitpack.io")
        }

        dependencies {
            "implementation"(libs.kotlin.stdlib)
            "implementation"(libs.kotlinx.coroutines.core)
            "implementation"(libs.kotlinx.serialization.json)

            "implementation"(libs.exposed.core)
            "implementation"(libs.exposed.dao)
            "implementation"(libs.exposed.jdbc)
            "implementation"(libs.exposed.java.time)
        }
    }

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

