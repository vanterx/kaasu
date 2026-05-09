plugins {
    id("kaasu.android.compose")
}

dependencies {
    add("implementation", project(":core:domain"))
    add("implementation", project(":core:ui"))
    add("implementation", "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    add("implementation", "androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
}
