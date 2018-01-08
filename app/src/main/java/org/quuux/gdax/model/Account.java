package org.quuux.gdax.model;


import java.io.Serializable;

public class Account implements Serializable {
    public String id;
    public String currency;
    public String balance;
    public String available;
    public String hold;
    public String profile_id;
}
