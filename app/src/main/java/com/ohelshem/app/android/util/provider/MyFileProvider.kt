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

package com.ohelshem.app.android.util.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor

import java.io.File
import java.io.FileNotFoundException

class MyFileProvider : ContentProvider() {
    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val privateFile = File(context.filesDir, uri.path)
        return ParcelFileDescriptor.open(privateFile, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun delete(arg0: Uri, arg1: String?, arg2: Array<String>?): Int = 0

    override fun getType(arg0: Uri): String? = null

    override fun insert(arg0: Uri, arg1: ContentValues?): Uri? = null

    override fun onCreate(): Boolean = false

    override fun query(arg0: Uri, arg1: Array<String>?, arg2: String?, arg3: Array<String>?,
                       arg4: String?): Cursor? = null

    override fun update(arg0: Uri, arg1: ContentValues?, arg2: String?, arg3: Array<String>?): Int = 0
}