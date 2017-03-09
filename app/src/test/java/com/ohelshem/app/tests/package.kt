package com.ohelshem.app.tests

import java.io.File

object Accessor
fun String.getTestResource(): File = File(Accessor::class.java.getResource("/$this").file)

fun String.getData() = getTestResource().readText()