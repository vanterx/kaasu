plugins {
    id("kaasu.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.expense.core.database"
}

dependencies {
    api(project(":core:domain"))
    api(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
