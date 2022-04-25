plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

apply<MavenPublishPlugin>()

group = "com.openosrs"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

configure<PublishingExtension> {
    repositories {
        maven {
            url = uri("$buildDir/repo")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}

val oprsVersion = "4.25.0"

dependencies {
    compileOnly(gradleApi())
    implementation("com.openosrs:cache:$oprsVersion")
    implementation("com.openosrs:runelite-api:$oprsVersion")
    implementation(group = "com.google.guava", name = "guava", version = "30.1.1-jre")
    implementation(group = "org.slf4j", name = "slf4j-nop", version = "1.7.32")
}

tasks.getByName("build").dependsOn("shadowJar")

tasks.shadowJar {
    minimize()
}