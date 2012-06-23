package org.playutils;

import org.joda.time.DateTime;

import java.io.Serializable;

public class ItemWithExpirationDate<T> implements Serializable {
    public ItemWithExpirationDate(){}
    public ItemWithExpirationDate(T item, DateTime expiration) {
        this.item = item;
        this.expiration = expiration;
    }

    public T item;
    public DateTime expiration;
}
