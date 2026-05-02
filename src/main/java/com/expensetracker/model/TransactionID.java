package com.expensetracker.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Immutable ADT representing a unique identifier for a Transaction.
 *
 * <p>Representation Invariant (RI):
 * - value > 0 (must be a positive integer)
 * - value is unique across all TransactionID instances created in this session
 *
 * <p>Abstraction Function (AF):
 * - AF(this) = a unique positive integer identifier that permanently identifies
 *   one and only one Transaction object throughout the application lifecycle.
 */
public final class TransactionID {

    /** Auto-incrementing counter to ensure uniqueness */
    private static final AtomicInteger counter = new AtomicInteger(1);

    /** The underlying integer value of this ID */
    private final int value;

    /**
     * Creates a new unique TransactionID.
     * The value is automatically assigned and guaranteed to be positive and unique.
     */
    public TransactionID() {
        this.value = counter.getAndIncrement();
        checkRep();
    }

    /**
     * Creates a TransactionID with a specific value (used when loading from storage).
     *
     * @param value the integer value; must be > 0
     * @throws IllegalArgumentException if value <= 0
     */
    public TransactionID(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("TransactionID value must be positive, got: " + value);
        }
        this.value = value;
        // Ensure counter stays ahead of loaded IDs to avoid future collisions
        counter.updateAndGet(current -> Math.max(current, value + 1));
        checkRep();
    }

    /**
     * Returns the integer value of this ID.
     *
     * @return the positive integer identifier
     */
    public int getValue() {
        return value;
    }

    /**
     * Checks the representation invariant.
     * Called at the end of every constructor.
     */
    private void checkRep() {
        assert value > 0 : "RI violated: TransactionID value must be > 0, got " + value;
    }

    /**
     * Two TransactionIDs are equal if and only if their values are equal.
     * This enables TransactionID to be used as a reliable HashMap key.
     *
     * @param o the object to compare
     * @return true if o is a TransactionID with the same value
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionID)) return false;
        TransactionID other = (TransactionID) o;
        return this.value == other.value;
    }

    /**
     * Hash code consistent with equals().
     * Required so TransactionID works correctly as a HashMap key.
     *
     * @return hash code based on the integer value
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "TXN-" + value;
    }
}
