/*
 * Copyright (c) 2018 "Neo4j, Inc." [https://neo4j.com]
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
package org.opencypher.gremlin.translation.translator;

import org.opencypher.gremlin.translation.GremlinSteps;

final class CosmosDbGremlinStepsDecorator<T, P> extends AbstractGremlinStepsDecorator<T, P> {

    private final GremlinSteps<T, P> delegate;

    CosmosDbGremlinStepsDecorator(GremlinSteps<T, P> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected GremlinSteps<T, P> delegate() {
        return delegate;
    }

    @Override
    public GremlinSteps<T, P> values(String... propertyKeys) {
        return properties()
            .hasKey(propertyKeys)
            .value();
    }

    @Override
    public GremlinSteps<T, P> injectRange(long low, long high, String label) {
        return delegate().injectRangeInline(low, high, 1);
    }
}