plugins {
    application
    checkstyle
    alias(libs.plugins.utf8)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.command.framework)
    implementation(libs.cdi.api)
    implementation(libs.inject.api)
    implementation(libs.javacord)
    implementation(libs.okhttp)
    implementation(libs.jackson.databind)
    implementation(libs.log4j.api)
    implementation(libs.log4j.jul)

    runtimeOnly(libs.antlr.runtime) { because("Optional dependency for parameter parser") }
    runtimeOnly(libs.weld.se) { because("CDI implementation") }
    runtimeOnly(libs.jandex) { because("faster CDI bean scanning") }
    runtimeOnly(libs.log4j.core)
    runtimeOnly(libs.log4j.slf4j.impl)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

application {
    mainClass.set("org.javacord.bot.Main")
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    maxWarnings = 0
}
