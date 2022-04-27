plugins {
    application
    checkstyle
}

repositories {
    jcenter()
    maven {  url = uri("https://jitpack.io") }
}

dependencies {
    implementation("org.javacord:javacord:3.1.1")
    implementation("de.btobastian.sdcf4j:sdcf4j-javacord:v1.0.10")
    implementation("de.btobastian.sdcf4j:sdcf4j-core:v1.0.10")
    implementation("com.squareup.okhttp3:okhttp:3.9.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.3")
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("org.apache.logging.log4j:log4j-jul:2.17.1")

    runtimeOnly("org.apache.logging.log4j:log4j-core:2.17.1")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")
}

application {
    mainClass.set("org.javacord.bot.Main")
}

configure<CheckstyleExtension> {
    toolVersion = "8.14"
    maxWarnings = 0
}