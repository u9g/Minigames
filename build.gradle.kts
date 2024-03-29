import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm") version "1.7.0"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.u9g"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://maven.u9g.dev")
    mavenLocal()
}

dependencies {
//    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("dev.u9g:sliver-api:1.18.2-R0.1-SNAPSHOT")

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.2")

    implementation("dev.u9g:U9GUtils:1.1.1")

    implementation("com.github.Redempt:RedLib:6.5.2")

    implementation("net.megavex.scoreboardlibrary:implementation:1.0.0")
    implementation("net.megavex.scoreboardlibrary:v1_18_R2:1.0.0")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.4.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.4.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    shadowJar {
        archiveClassifier.set("")
    }
    runServer {
        minecraftVersion.set("1.18.2")
        serverJar.set(File("server/sliver-bundler-1.18.2-R0.1-SNAPSHOT-reobf.jar"))
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
//val compileTestKotlin: KotlinCompile by tasks
//compileTestKotlin.kotlinOptions {
//    jvmTarget = "1.8"
//}
