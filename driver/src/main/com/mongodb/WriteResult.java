/*
 * Copyright (c) 2008 - 2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb;


/**
 * This class lets you access the results of the previous write. If the write was performed with an acknowledged write concern, this just
 * stores the result of the write.
 *
 * @see WriteConcern#UNACKNOWLEDGED
 */
public class WriteResult {

    private final int n;
    private final boolean updateOfExisting;
    private final Object upsertedId;

    /**
     * Construct a new instance.
     *
     * @param n the number of existing documents affected by this operation
     * @param updateOfExisting true if the operation was an update and an existing document was updated
     * @param upsertedId the _id of a document that was upserted by this operation
     */
    public WriteResult(final int n, final boolean updateOfExisting, final Object upsertedId) {
        this.n = n;
        this.updateOfExisting = updateOfExisting;
        this.upsertedId = upsertedId;
    }

    /**
     * Gets the "n" field, which contains the number of documents affected in the write operation.
     *
     * @return the value of the "n" field
     * @throws MongoException if the write was unacknowledged
     * @see WriteConcern#UNACKNOWLEDGED
     */
    public int getN() {
        return n;
    }

    /**
     * Gets the _id value of an upserted document that resulted from this write.  Note that for MongoDB servers prior to version 2.6,
     * this method will return null unless the _id of the upserted document was of type ObjectId.
     *
     * @return the value of the _id of an upserted document
     * @since 2.12
     */
    public Object getUpsertedId() {
        return upsertedId;
    }


    /**
     * Returns true if this write resulted in an update of an existing document.
     *
     * @return whether the write resulted in an update of an existing document.
     * @since 2.12
     */
    public boolean isUpdateOfExisting() {
        return updateOfExisting;
    }

    @Override
    public String toString() {
        return "WriteResult{"
               + ", n=" + n
               + ", updateOfExisting=" + updateOfExisting
               + ", upsertedId=" + upsertedId
               + '}';
    }
}