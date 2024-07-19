import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.inputStream

plugins {
    eclipse
    idea
    java
    `java-gradle-plugin`
    `maven-publish`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

eclipse {
    classpath {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

val projectPath: Path = project.projectDir.toPath()
val buildConfig: Properties = Properties().apply {
    Path("build.properties").inputStream(StandardOpenOption.READ).use(::load)
}
val license: String = buildConfig["license"] as String
val buildNumber: Int = System.getenv("CI_PIPELINE_IID")?.toIntOrNull() ?: 0
val buildTime: Instant = Instant.now()

version = "${libs.versions.gradleCMake.get()}.$buildNumber"
group = buildConfig["group"] as String
base.archivesName = "gradle-cmake"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(gradleApi())
    implementation(libs.annotations)
}

gradlePlugin {
    plugins {
        val cmakePlugin by creating {
            id = "${base.archivesName.get()}.${project.group}"
            implementationClass = "${project.group}.CMakePlugin"
        }
    }
}

tasks {
    val classes by getting

    val sourcesJar = create<Jar>("sourcesJar") {
        from(sourceSets.main.get().allSource)
        dependsOn(classes)
        archiveClassifier = "sources"
    }

    System.getenv("CI_API_V4_URL")?.let { apiUrl ->
        publishing {
            repositories {
                maven {
                    url = uri("${apiUrl.replace("http://", "https://")}/projects/177/packages/maven")
                    name = "GitLab"
                    credentials(HttpHeaderCredentials::class) {
                        name = "Job-Token"
                        value = System.getenv("CI_JOB_TOKEN")
                    }
                    authentication {
                        create("header", HttpHeaderAuthentication::class)
                    }
                }
            }

            publications {
                create<MavenPublication>("gradleCMake") {
                    groupId = project.group as String
                    artifactId = project.base.archivesName.get()
                    version = project.version as String
                    
                    artifact(sourcesJar)

                    pom {
                        name = artifactId
                        url = "https://git.karmakrafts.dev/kk/${project.name}"
                        scm {
                            url = this@pom.url
                        }
                        issueManagement {
                            system = "gitlab"
                            url = "https://git.karmakrafts.dev/kk/${project.name}/issues"
                        }
                        licenses {
                            license {
                                name = license
                                distribution = "repo"
                            }
                        }
                        developers {
                            developer {
                                id = "freudi74"
                                name = "freudi74"
                                url = "https://github.com/freudi74"
                            }
                            developer {
                                id = "kitsunealex"
                                name = "KitsuneAlex"
                                url = "https://git.karmakrafts.dev/KitsuneAlex"
                            }
                        }
                    }
                }
            }
        }
    }
}