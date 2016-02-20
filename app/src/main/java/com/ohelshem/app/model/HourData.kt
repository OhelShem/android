/*
 * Copyright 2016 Yoav Sternberg.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ohelshem.app.model

/**
 * Represent the "current data" - current hour, next hour and progress.
 *
 * Currently used for the widget and for dashboard.
 */
data class HourData(val hour: NumberedHour, val nextHour: NumberedHour, val timeToHour: Int, val progress: Int, val isBefore: Boolean = true) {
    companion object {
        /**
         * A constant that indicate that the next lesson is tomorrow or later. should be set for [HourData.timeToHour] .
         */
        var DayBefore: Int = -1
    }
}
