import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) load(keystorePropertiesFile.inputStream())
}

android {
    namespace = "com.example.expense"
    compileSdk = 34
    base.archivesName.set("Kaasu")

    signingConfigs {
        create("shared") {
            storeFile = keystoreProperties["storeFile"]?.let { file(it as String) }
            storePassword = keystoreProperties["storePassword"] as String?
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
        }
    }

    defaultConfig {
        applicationId = "com.example.expense"
        minSdk = 26
        targetSdk = 34
        versionCode = (System.currentTimeMillis() / 1000).toInt()
        versionName = "1.6.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("shared")
        }
        release {
            signingConfig = signingConfigs.getByName("shared")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.12")
}

// ── Documentation sync check ──────────────────────────────────────────────────
tasks.register("checkDocs") {
    group = "verification"
    description = "Verify README.md and AGENTS.md are in sync with code"

    doLast {
        val rootDir = project.rootDir
        var failed = false

        // Check 1: versionName matches README.md
        val readmeFile = File(rootDir, "README.md")
        if (readmeFile.exists()) {
            val readmeContent = readmeFile.readText()
            val readmeVer = Regex("\\*\\*(\\d+\\.\\d+\\.\\d+)\\*\\*")
                .find(readmeContent)?.groupValues?.get(1)
            val gradleVer = android.defaultConfig.versionName
            if (readmeVer != gradleVer) {
                logger.warn("versionName mismatch: build.gradle.kts=$gradleVer, README.md=$readmeVer")
                failed = true
            }
        }

        // Check 2: AGENTS.md lists all .kt source directories
        val agentsFile = File(rootDir, "AGENTS.md")
        val srcRoot = File(projectDir, "src/main/java/com/example/expense")
        if (agentsFile.exists() && srcRoot.exists()) {
            val agentsContent = agentsFile.readText()
            srcRoot.walkTopDown()
                .filter { it.isDirectory && it.listFiles()?.any { f -> f.extension == "kt" } == true }
                .forEach { dir ->
                    val leafName = dir.name + "/"
                    // AGENTS.md lists directories like " db/" or " mapper/" with trailing slash
                    if (!agentsContent.contains(leafName)) {
                        logger.warn("Source directory not in AGENTS.md: ${dir.relativeTo(srcRoot).path.replace("\\", "/")}")
                        failed = true
                    }
                }
        }

        if (failed) {
            logger.warn("\u001B[33m[docs-check]\u001B[0m Documentation may be out of sync. Run './gradlew checkDocs' for details.")
            // Advisory only — does not fail the build.
        }
    }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn("checkDocs")
}
