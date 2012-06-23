package org.playutils;

import org.joda.time.DateTime;
import org.joda.time.Period;
import play.cache.Cache;

import java.util.concurrent.Callable;

public class CacheUtils {
    private CacheUtils(){}

    @SuppressWarnings("unchecked")
    public static <T> T getCachedOrFresh(String key, Period expiration, Callable<T> promise) throws Exception {
        ItemWithExpirationDate<T> result = (ItemWithExpirationDate<T>) Cache.get(key);
        if (result == null || new DateTime().isAfter(result.expiration)) {
            T newResult = promise.call();
            result = new ItemWithExpirationDate<T>(newResult, new DateTime().plus(expiration));
            Cache.set(key, result);
        }
        return result.item;
    }
}
