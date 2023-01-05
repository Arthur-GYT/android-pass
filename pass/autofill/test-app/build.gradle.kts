plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "me.proton.pass.autofill.sample"

    defaultConfig {
        applicationId = "me.proton.pass.core.autofill.sample"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = Config.versionCode
        versionName = Config.versionName
        testInstrumentationRunner = Config.testInstrumentationRunner
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            isTestCoverageEnabled = false
        }
        release {
            isMinifyEnabled = true
            //isShrinkResources = true // should be replaced by useResourceShrinker
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    packagingOptions {
        resources.excludes.add("META-INF/licenses/**")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.foundationLayout)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.core.presentation.compose)
    implementation(libs.material)
}
