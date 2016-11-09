[![Kotlin](https://img.shields.io/badge/kotlin-1.0.4-blue.svg)](http://kotlinlang.org) ![Version 6.0](https://img.shields.io/badge/Version-6.0-yellow.svg) [![License Apache](https://img.shields.io/badge/License-Apache%202.0-red.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Ohel Shem for Android
===========

[![Get it on Google Play](http://i.imgur.com/NIZaIXm.png?1)](https://play.google.com/store/apps/details?id=com.yoavst.changesystemohelshem)

Ohel Shem (Hebrew: אהל שם‎) is an Israeli high school located in the city of Ramat Gan. 
It has about 1,550 students studying in 45 classes, from ninth to twelfth grade, and about 160 teachers and 40 workers.

<img src="http://imgur.com/Ymo1qoh.jpg" width="250">

## Ohel Shem :heart: OSS
This project is part of 'Ohel Shem OSS', our attempt at providing open standard for modern school.

# App

This app is for students, allowing them to be updated with the most important data:

* Timetable and changes
<br>
<img src="http://imgur.com/wZt4JKU.jpg" width="350">
<img src="http://imgur.com/HU0PnXQ.jpg" width="350">
* Tests and holidays
<br>
<img src="http://imgur.com/ozhbftx.jpg" width="350">
<img src="http://imgur.com/kTPJ9Kr.jpg" width="350">


# Technology behind

This application is based on [Api-JVM](http://ohelshem.github.io/api-jvm/), the official Ohel-Shem API for JVM platform (Java, Kotlin, Scala, Groovy...) 

Here are some of most cutting edge technologies available in 2016 used in the app:

## Kotlin
Statically typed programming language for the JVM, Android and the browser. Kotlin to Java is like C++ to C.  
Kotlin libraries used in this project:

* Anko - A DSL for layout and extension methods for Android development, Written  by JetBrains.
* Kodein - Dependency Injection library, based on Kotlin's delegation support.
* Fuel - The easiest HTTP networking library in Kotlin for Android.
* KotPref - Android SharedPreference delegation for Kotlin.

## Vector images
Most of the app's images are vector graphics.  
> Vector graphics is the use of geometrical primitives such as points, lines, curves, and shapes or polygons—all of which are based on mathematical expressions—to represent images in computer graphics 

Vector graphics allow the app to look perfect on any screen resolution, including QHD and 4K. (Tested on G4 with QHD screen).

Some of the icons were taken from those icon sets. Credit for them:

https://www.iconfinder.com/iconsets/kameleon-free-pack  
https://www.iconfinder.com/iconsets/flat-color-icons
https://www.iconfinder.com/iconsets/little-boy

## Modularity
The project functionality is very modular. Every feature got his own controller, including:

* `Storage` - responsible of storing the data of the application.
* `OffsetDataController` - responsible on reading an `offset data` - The way timetable and tests are saved.
* `TimetableController` - responsible on managing the timetable and tests.
* `ApiController` - responsible on querying the api

Each of those controller are interfaces, make them easy to test and pass by. 

## Offset Data
The app has to store the timetable and the tests for the whole school. It is a lot of data.
The data is a list of strings.
Every time the app starts, it has to load the user's specific data.
The best solution will be loading only the user's data to the memory.  
That is why we dropped JSON, since `GSON` and `Jackson` load all the data to the memory first.  
We could go with XML, but it would increase the file size.  

The result was custom format we created - Offset data.  
It is based on 2 files. The first include the data itself, separated by a delimiter. 
The second file store the offset of each class of the school from the beginning of the file.

### Example
Let's say we count offset in bytes, and there are only 3 classes at school, each of them having 3 hours only.
The first file will be:
```
first_lesson_for_first_class|second_lesson_for_first_class|third_lesson_for_first_class|first_lesson_for_second_class|second_lesson_for_second_class|third_lesson_for_second_class|first_lesson_for_third_class|second_lesson_for_third_class|third_lesson_for_third_class
```

and the second file will be (in the actual file the data is stored in binary, but here a space between each number):

```
0 89 180 267
```

In order to read the data of the second class, we read the second and third numbers from the second file. Then we read only the data from offset 89 to 180.
Now, we parse this data by splitting it over the delimiter.

### performance
We tested the performance of reading from timetable from JSON and data offset.
for 10,000 runs, it took GSON 9 seconds to read the data of a class. But, it took Offset data reader 1 second to read the data of a class.

## Unit Testing
How can some release a product without testing?  
Since the project functionality (unlike the UI with is android specific) is fully modular, it was possible to test every component.  
Available tests:

* Offset data read
* Timetable functionality

# Becoming a part in the project
Want to become a part in the project that makes the life of an Ohel Shem student better?  
Contact me at [Yoavst.com](http://yoavst.com).

Also, I would merge any useful pull request.

# License

    Copyright 2016 Yoav Sternberg

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
