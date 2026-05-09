pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Kaasu"

include(":app")
include(":core:common")
include(":core:domain")
include(":core:database")
include(":core:data")
include(":core:ui")
include(":feature:expense")
include(":feature:chart")
include(":feature:category")
include(":feature:settings")
include(":feature:export")
