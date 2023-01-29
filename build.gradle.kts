import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.7.22"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("maven-publish")
    id("io.papermc.paperweight.userdev") version "1.4.1"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = "dev.igalaxy"
version = "1.0.1"
description = "Load existing maps from your Multiverse worlds"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.onarandombox.com/content/groups/public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    paperDevBundle("1.19.3-R0.1-SNAPSHOT")
    implementation("net.axay:kspigot:1.19.1")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    compileOnly("com.onarandombox.multiversecore:Multiverse-Core:4.3.1")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

tasks {

    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    shadowJar { }
}

bukkit {
    name = "multimap"
    description = description
    main = "dev.igalaxy.multimap.Multimap"
    version = version
    apiVersion = "1.19"
    depend = listOf("ProtocolLib", "Multiverse-Core")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "multimap"
            from(components["java"])
        }
    }
}