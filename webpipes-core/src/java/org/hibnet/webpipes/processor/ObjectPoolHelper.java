/*
 *  Copyright 2014-2015 WebPipes contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hibnet.webpipes.processor;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic aware object pool wrapper. Probably not the best name, but it can be changed later. It helps you to avoid the cast and hides the
 * exception handling by throwing {@link RuntimeException} when borrowing or returning object to the pool fails.
 */
public class ObjectPoolHelper<T> {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectPoolHelper.class);

    private static final int MAX_IDLE = 2;
    private static final long MAX_WAIT = 5L * 1000L;
    private static final long EVICTABLE_IDLE_TIME = 30 * 1000L;

    public interface ObjectFactory<T> {

        T create();

    }

    // Allows using the objects from the pool in a thread-safe fashion.
    private GenericObjectPool<T> objectPool;

    public ObjectPoolHelper(final ObjectFactory<T> objectFactory) {
        objectPool = createObjectPool(objectFactory);
    }

    /**
     * Ensure that a not null pool will be created.
     */
    private GenericObjectPool<T> createObjectPool(final ObjectFactory<T> objectFactory) {
        final GenericObjectPool<T> pool = newObjectPool(objectFactory);
        return pool;
    }

    /**
     * Creates a {@link GenericObjectPool}. Override this method to set custom objectPool configurations.
     */
    protected GenericObjectPool<T> newObjectPool(final ObjectFactory<T> objectFactory) {
        final int maxActive = Math.max(2, Runtime.getRuntime().availableProcessors());
        final GenericObjectPool<T> pool = new GenericObjectPool<T>(new BasePoolableObjectFactory<T>() {
            @Override
            public T makeObject() throws Exception {
                return objectFactory.create();
            }
        });
        pool.setMaxActive(maxActive);
        pool.setMaxIdle(MAX_IDLE);
        pool.setMaxWait(MAX_WAIT);
        /**
         * Use WHEN_EXHAUSTED_GROW strategy, otherwise the pool object retrieval can fail. More details here:
         * <a>http://code.google.com/p/wro4j/issues/detail?id=364</a>
         */
        pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
        // make object eligible for eviction after a predefined amount of time.
        pool.setSoftMinEvictableIdleTimeMillis(EVICTABLE_IDLE_TIME);
        pool.setTimeBetweenEvictionRunsMillis(EVICTABLE_IDLE_TIME);
        return pool;
    }

    /**
     * @return object from the pool.
     */
    public T getObject() {
        try {
            return objectPool.borrowObject();
        } catch (final Exception e) {
            // should never happen
            throw new RuntimeException("Cannot get object from the pool", e);
        }
    }

    public void returnObject(final T engine) {
        try {
            objectPool.returnObject(engine);
        } catch (final Exception e) {
            // should never happen
            throw new RuntimeException("Cannot get object from the pool", e);
        }
    }

    /**
     * Use a custom {@link GenericObjectPool}.
     *
     * @param objectPool to use.
     */
    public final void setObjectPool(final GenericObjectPool<T> objectPool) {
        this.objectPool = objectPool;
    }

    /**
     * Close the object pool to avoid the memory leak.
     *
     * @throws Exception if the close operation failed.
     */
    public void destroy() throws Exception {
        LOG.debug("closing objectPool");
        objectPool.close();
    }
}
