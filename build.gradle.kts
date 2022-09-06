import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.6.21"
    id("io.papermc.paperweight.userdev") version "1.3.7"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

val baseAuthor: String by project
val baseName: String by project
val minimumApiVersion: String by project
val baseVersion: String by project
val mcVersion: String by project
val author = baseAuthor.toLowerCase()
val name = baseName.toLowerCase()

group = "nl.$author.$name"
version = "$baseVersion-$mcVersion"

bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    name = baseName
    main = "$group.Main"
    apiVersion = minimumApiVersion
    authors = listOf(baseAuthor)
    depend = listOf("KotlinLib")
    commands {
        register("cea") {
            aliases = listOf("ceapi")
            description = "CustomEntityAPI command"
        }
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {

    // Paperweight UserDev https://github.com/PaperMC/paperweight
    paperDevBundle("${mcVersion}-R0.1-SNAPSHOT")

    // Netty dependency for channel injecting https://mvnrepository.com/artifact/io.netty/netty-all
    compileOnly("io.netty:netty-transport:4.1.77.Final")

    // WorldGuard integration https://github.com/EngineHub/WorldGuard
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")

}

tasks {

    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/xanderwander/customentityapi")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}