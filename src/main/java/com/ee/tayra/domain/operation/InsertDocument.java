/*******************************************************************************
 * Copyright (c) 2013, Equal Experts Ltd
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * � �this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * � �notice, this list of conditions and the following disclaimer in the
 * � �documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the Tayra Project.
 ******************************************************************************/
package com.ee.tayra.domain.operation;

import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

class InsertDocument extends Operation {

  private static final String INDEX_NAMESPACE = "system.indexes";
  private final Mongo mongo;
  private String dbName;
  private String collectionName;

  public InsertDocument(final Mongo mongo) {
    this.mongo = mongo;
  }

  @Override
  protected final void doExecute(final DBObject document) {
    final String ns = (String) document.get("ns");
    int index = ns.indexOf(".");
    DBObject spec = (DBObject) JSON.parse(document.get("o").toString());

    if (index != -1) {
      if (ns.contains(INDEX_NAMESPACE)) {
      String indexNamespace = spec.get("ns").toString();
      extractParametersFrom(indexNamespace);
      DBObject key = (DBObject) spec.get("key");
      try {
        mongo.getDB(dbName).getCollection(collectionName)
                           .ensureIndex(key, spec);
      } catch (Exception problem) {
        throw new InsertFailed(problem.getMessage());
      }
    } else {
      extractParametersFrom(ns);
      try {
        mongo.getDB(dbName).getCollection(collectionName).insert(spec);
      } catch (Exception problem) {
        throw new InsertFailed(problem.getMessage());
      }
    }
  }
}

  private void extractParametersFrom(final String namespace) {
    int index = namespace.indexOf(".");
    dbName = namespace.substring(0, index);
    collectionName = namespace.substring(index + 1, namespace.length());
  }
}
