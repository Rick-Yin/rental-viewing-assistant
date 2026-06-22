pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RentalViewingAssistant"
include(":app")
include(":core")
include(":domain")
include(":data")
include(":feature:profile")
include(":feature:property")
include(":feature:viewing")
include(":feature:compare")
include(":feature:signing")
