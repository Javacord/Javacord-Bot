plugins {
    application
    checkstyle
    alias(libs.plugins.utf8)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.javacord)
    implementation(libs.bundles.sdcf4j)
    implementation(libs.okhttp)
    implementation(libs.jackson.databind)
    implementation(libs.log4j.api)
    implementation(libs.log4j.jul)

    runtimeOnly(libs.log4j.core)
    runtimeOnly(libs.log4j.slf4j.impl)
}

application {
    mainClass.set("org.javacord.bot.Main")
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    maxWarnings = 0
}
