package xyz.aspectowl.tptp.util;

/**
 * @author ralph
 */
public class Counter<T extends Number> {

    public T value;

    public Counter(T initialValue) {
        this.value = initialValue;
    }
}
