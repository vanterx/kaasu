plugins {
    id("kaasu.android.library")
}

android {
    namespace = "com.example.expense.core.data"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(libs.androidx.datastore.preferences)
}
