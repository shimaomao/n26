package de.florian.n26.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class TransactionResource {

    private final long timestamp;
    private final double amount;

    @JsonCreator
    public TransactionResource(@JsonProperty(value = "timestamp", required = true) long timestamp,
                               @JsonProperty(value = "amount", required = true) double amount) {
        this.timestamp = timestamp;
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getAmount() {
        return amount;
    }
}
