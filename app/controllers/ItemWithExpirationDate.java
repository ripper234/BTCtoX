package controllers;

import org.joda.time.DateTime;

public class ItemWithExpirationDate<T> {
    public ItemWithExpirationDate(){}
    public ItemWithExpirationDate(T item, DateTime expiration) {
        this.item = item;
        this.expiration = expiration;
    }

    public T item;
    public DateTime expiration;
}
