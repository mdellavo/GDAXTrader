package org.quuux.gdax.events;


public class AccountsLoadError {
    public final APIError error;

    public AccountsLoadError(final APIError error) {
        this.error = error;
    }
}
