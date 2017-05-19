apply {
    plugin("com.android.application")
    plugin("kotlin-android")
    plugin("kotlin-android-extensions")
    plugin("com.google.gms.google-services")
}

android {
    buildToolsVersion("26.0.0-rc2")
    compileSdkVersion(25)

    defaultConfig {
        minSdkVersion(15)
        targetSdkVersion(25)

        applicationId = "com.yoavst.changesystemohelshem"
        versionCode = 92
        versionName = "7.1.6"

        resConfigs("he")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
        }
    }

    aaptOptions {
        cruncherEnabled = false
    }
}

val androidSupportLibrary by project
val firebase by project
val anko by project
val kotlinVersion by project

dependencies {
    compile("com.ohelshem:api:0.5.1")
    // Android support libraries
    compile("com.android.support:support-fragment:$androidSupportLibrary")
    compile("com.android.support:appcompat-v7:$androidSupportLibrary")
    compile("com.android.support:cardview-v7:$androidSupportLibrary")
    compile("com.android.support:recyclerview-v7:$androidSupportLibrary")
    compile("com.android.support:design:$androidSupportLibrary")
    compile("com.android.support:percent:$androidSupportLibrary")
    // Firebase
    compile("com.google.firebase:firebase-core:$firebase")
    compile("com.google.firebase:firebase-messaging:$firebase")
    compile("com.google.firebase:firebase-crash:$firebase")
    // Kotlin libraries
    compile(kotlinModule(module ="stdlib", version = "$kotlinVersion"))
    compile("com.github.salomonbrys.kodein:kodein-erased:4.0.0-beta2")
    compile("com.github.salomonbrys.kodein:kodein-android:4.0.0-beta2")
    compile("com.chibatching.kotpref:kotpref:2.1.1")
    compile("com.github.kittinunf.fuel:fuel-android:1.5.0")
    // Util
    compile("io.github.microutils:kotlin-logging:1.4.4")
    compile("org.slf4j:slf4j-android:1.7.21")
    compile("com.jakewharton:process-phoenix:1.1.0")
    // Anko modules
    compile("org.jetbrains.anko:anko-sdk15:$anko")
    compile("org.jetbrains.anko:anko-support-v4:$anko")
    compile("org.jetbrains.anko:anko-recyclerview-v7:$anko")
    compile("org.jetbrains.anko:anko-design:$anko")
    compile("org.jetbrains.anko:anko-sdk15-listeners:$anko")
    // MVP
    compile("com.hannesdorfmann.mosby3:mvp:3.0.3")
    // UI
    compile("com.nbsp:library:1.09") // material file picker
    compile("com.readystatesoftware.systembartint:systembartint:1.0.4")
    compile("com.prolificinteractive:material-calendarview:1.4.3")
    compile("com.github.javiersantos:MaterialStyledDialogs:2.0")
    compile("uk.co.samuelwall:material-tap-target-prompt:1.4.3")
    compile("com.github.gabrielemariotti.changeloglib:changelog:2.1.0")
    compile("com.vlonjatg.android:progress-activity:1.1.1")
    compile("com.roughike:bottom-bar:2.2.0")
    compile("com.timehop.fragmentswitcher:library:1.1.2")
    compile("com.sloydev:preferator:1.1.0")
    compile("de.hdodenhof:circleimageview:2.1.0")
    compile("com.plattysoft.leonids:LeonidsLib:1.3.2")
    // debug
    compile("io.palaima.debugdrawer:debugdrawer:0.7.1-SNAPSHOT")
    compile("io.palaima.debugdrawer:debugdrawer-commons:0.7.1-SNAPSHOT")
    compile("io.palaima.debugdrawer:debugdrawer-actions:0.7.1-SNAPSHOT")
    //Test
    testCompile("junit:junit:4.12")
    testCompile(kotlinModule(module = "test", version = "$kotlinVersion"))

}
