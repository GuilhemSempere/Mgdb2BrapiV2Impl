/*******************************************************************************
 * MGDB - Mongo Genotype DataBase
 * Copyright (C) 2016 - 2019, <CIRAD> <IRD>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License, version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * See <http://www.gnu.org/licenses/agpl.html> for details about GNU General
 * Public License V3.
 *******************************************************************************/
package org.brapi.v2.api.cache;

import java.util.List;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;

import fr.cirad.mgdb.model.mongo.maintypes.CachedCount;

@Document(collection = "brapiCachedCounts")
@TypeAlias("BCC")
public class BrapiCachedCount extends CachedCount {

	public BrapiCachedCount(String id, List<Long> chunkCounts) {
		super(id, chunkCounts);
	}
	
	static public void saveCachedCount(MongoTemplate mongoTemplate, String queryKey, List<Long> counts) {
		// make sure collection exists (we want it capped!)
		try {
			mongoTemplate.createCollection(mongoTemplate.getCollectionName(BrapiCachedCount.class), CollectionOptions.empty().capped().size(10000 /* seems enough for over 100 documents */ ));
		}
		catch (UncategorizedMongoDbException ignored)
		{}	// already exists
		mongoTemplate.save(new BrapiCachedCount(queryKey, counts));
	}
}