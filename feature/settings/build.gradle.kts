plugins {
    id("kaasu.android.feature")
}

android {
    namespace = "com.example.expense.feature.settings"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":feature:category"))
}
