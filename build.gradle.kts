buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:2.3.1")
        classpath("com.google.gms:google-services:3.0.0")
        classpath(kotlinModule("gradle-plugin"))
    }
    repositories {
        jcenter()
    }
}

allprojects {
    repositories {
        jcenter()
        maven { url = uri("https://maven.google.com") }
        maven { url = uri("http://dl.bintray.com/ohelshem/maven") }
        maven { url = uri("http://dl.bintray.com/lukaville/maven") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://jitpack.io") }
    }
}

