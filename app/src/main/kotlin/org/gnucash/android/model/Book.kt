/*
 * Copyright (c) 2015 Ngewi Fet <ngewif@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gnucash.android.model

import android.net.Uri
import java.sql.Timestamp

/**
 * Represents a GnuCash book which is made up of accounts and transactions
 *
 * @author Ngewi Fet <ngewif></ngewif>@gmail.com>
 */
class Book : BaseModel {
    /**
     * Return the Uri of the XML file from which the book was imported.
     *
     * In API level 16 and above, this is the Uri from the storage access framework which will
     * be used for synchronization of the book
     *
     * @return Uri of the book source XML
     */
    /**
     * Set the Uri of the XML source for the book
     *
     * This Uri will be used for sync where applicable
     *
     * @param uri Uri of the GnuCash XML source file
     */
    var sourceUri: Uri? = null
    /**
     * Returns a name for the book
     *
     * This is the user readable string which is used in UI unlike the root account GUID which
     * is used for uniquely identifying each book
     *
     * @return Name of the book
     */
    /**
     * Set a name for the book
     *
     * @param name Name of the book
     */
    var displayName: String? = null
    /**
     * Return the root account GUID of this book
     *
     * @return GUID of the book root account
     */
    /**
     * Sets the GUID of the root account of this book.
     *
     * Each book has only one root account
     *
     * @param rootAccountUID GUID of the book root account
     */
    var rootAccountUID: String? = null
    /**
     * Return GUID of the template root account
     *
     * @return GUID of template root acount
     */
    /**
     * Set the GUID of the root template account
     *
     * @param rootTemplateUID GUID of the root template account
     */
    var rootTemplateUID: String? = null
    /**
     * Check if this book is the currently active book in the app
     *
     * An active book is one whose data is currently displayed in the UI
     *
     * @return `true` if this is the currently active book, `false` otherwise
     */
    /**
     * Sets this book as the currently active one in the application
     *
     * @param active Flag for activating/deactivating the book
     */
    var isActive = false
    /**
     * Get the time of last synchronization of the book
     *
     * @return Timestamp of last synchronization
     */
    /**
     * Set the time of last synchronization of the book
     *
     * @param lastSync Timestamp of last synchronization
     */
    var lastSync: Timestamp? = null

    /**
     * Default constructor
     */
    constructor() {
        init()
    }

    /**
     * Create a new book instance
     *
     * @param rootAccountUID GUID of root account
     */
    constructor(rootAccountUID: String?) {
        this.rootAccountUID = rootAccountUID
        init()
    }

    /**
     * Initialize default values for the book
     */
    private fun init() {
        rootTemplateUID = generateUID()
        lastSync = Timestamp(System.currentTimeMillis())
    }
}
