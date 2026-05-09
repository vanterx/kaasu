plugins {
    id("kaasu.android.feature")
}

android {
    namespace = "com.example.expense.feature.export"
}

dependencies {
    implementation(libs.androidx.activity.compose)
}
