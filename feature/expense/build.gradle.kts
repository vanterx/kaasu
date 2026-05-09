plugins {
    id("kaasu.android.feature")
}

android {
    namespace = "com.example.expense.feature.expense"
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
}
