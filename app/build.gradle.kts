import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.itos.xplanforhyper"
    compileSdk = 35
    applicationVariants.all {
        outputs.all {
            val ver = defaultConfig.versionName + "(${defaultConfig.versionCode})"

            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "XHpyer_$ver.apk"
        }
    }
    defaultConfig {
        applicationId = "com.itos.xplanforhyper"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "1.1"

//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildTypes {
            release {
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        kotlinOptions {
            jvmTarget = "1.8"
        }
        buildFeatures {
            compose = true
            aidl = true
            buildConfig = true
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.github.kongzue.DialogX:DialogX:0.0.50.beta30")
    implementation("com.github.kongzue.DialogX:DialogXMIUIStyle:0.0.50.beta30")
    implementation("com.github.kongzue.DialogX:DialogXMaterialYou:0.0.50.beta30")

    implementation("dev.rikka.shizuku:api:13.1.0")
    implementation("dev.rikka.shizuku:provider:13.1.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("androidx.compose.material:material:1.8.2")
    implementation("com.google.accompanist:accompanist-drawablepainter:0.27.0")
    implementation(files("libs/fastjson-1.2.9.jar"))
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")
//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
//    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
