package kr.doka.lab.shoppy.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ShoppyPluginLoader implements PluginLoader {

    // 1. 버전 정보를 담을 Properties 객체를 준비합니다.
    private static final Properties versions = new Properties();

    // 2. 클래스가 로드될 때 'version.properties' 파일을 읽어서 객체에 저장합니다.
    static {
        try (InputStream input = ShoppyPluginLoader.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                // 파일이 없는 경우를 대비한 예외 처리
                throw new RuntimeException("Could not find version.properties in the classpath");
            }
            versions.load(input);
        } catch (IOException ex) {
            // 파일을 읽다가 에러가 발생한 경우
            throw new RuntimeException("Failed to load version.properties", ex);
        }
    }

    // 3. 반복을 줄이기 위한 헬퍼 메서드
    private Dependency createDependency(String artifactId) {
        return new Dependency(new DefaultArtifact(artifactId), null);
    }

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());

        // 4. 하드코딩된 버전을 Properties 객체에서 읽어오도록 수정합니다.
        String exposedVersion = versions.getProperty("exposed");
        resolver.addDependency(createDependency("org.jetbrains.exposed:exposed-core:" + exposedVersion));
        resolver.addDependency(createDependency("org.jetbrains.exposed:exposed-dao:" + exposedVersion));
        resolver.addDependency(createDependency("org.jetbrains.exposed:exposed-jdbc:" + exposedVersion));
        resolver.addDependency(createDependency("org.jetbrains.exposed:exposed-java-time:" + exposedVersion));

        resolver.addDependency(createDependency("org.jetbrains.kotlinx:kotlinx-serialization-json:" + versions.getProperty("serialization")));
        resolver.addDependency(createDependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:" + versions.getProperty("coroutines")));
        resolver.addDependency(createDependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8:" + versions.getProperty("kotlin"))); // JDK8이 맞는지 확인 필요

        resolver.addDependency(createDependency("com.zaxxer:HikariCP:" + versions.getProperty("hikaricp")));
        resolver.addDependency(createDependency("org.mariadb.jdbc:mariadb-java-client:" + versions.getProperty("mariadb")));

        resolver.addDependency(createDependency("org.xerial:sqlite-jdbc:" + versions.getProperty("sqlite")));

        classpathBuilder.addLibrary(resolver);
    }
}
