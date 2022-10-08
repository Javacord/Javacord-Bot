rootProject.name = "javacord-bot"

dependencyResolutionManagement {
    components {
        all {
            // align log4j module versions until log4j eventually publishes proper alignment
            if (id.group == "org.apache.logging.log4j") {
                belongsTo("org.apache.logging.log4j:log4j-bom:${id.version}", false)
            }
        }
    }
}
