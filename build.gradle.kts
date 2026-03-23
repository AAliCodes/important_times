// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.1.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    alias(libs.plugins.kotlin.compose) apply false
}
val defaultTargetSdkVersion by extra(36)
