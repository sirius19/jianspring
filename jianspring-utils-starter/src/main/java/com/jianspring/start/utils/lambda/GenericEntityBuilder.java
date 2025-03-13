package com.jianspring.start.utils.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericEntityBuilder<T> {

    private final Supplier<T> instance;

    private final List<Consumer<T>> attributes = new ArrayList<>();

    private GenericEntityBuilder(Supplier<T> instance) {
        this.instance = instance;
    }

    public static <T> GenericEntityBuilder<T> of(Supplier<T> instance) {
        return new GenericEntityBuilder<>(instance);
    }

    public <R> GenericEntityBuilder<T> with(BiConsumer<T, R> consumer, R value) {
        attributes.add(consumer1 -> consumer.accept(consumer1, value));
        return this;
    }

    public <R> GenericEntityBuilder<T> withSupplier(BiConsumer<T, R> consumer, Supplier<R> supplier) {
        attributes.add(consumer1 -> consumer.accept(consumer1, supplier.get()));
        return this;
    }

    public <R> GenericEntityBuilder<T> with(boolean flag, BiConsumer<T, R> consumer, R value) {
        if (flag) {
            attributes.add(consumer1 -> consumer.accept(consumer1, value));
        }
        return this;
    }

    public <R> GenericEntityBuilder<T> withSupplier(boolean flag, BiConsumer<T, R> consumer, Supplier<R> supplier) {
        if (flag) {
            attributes.add(consumer1 -> consumer.accept(consumer1, supplier.get()));
        }
        return this;
    }

    public <R> GenericEntityBuilder<T> with(boolean flag, BiConsumer<T, R> consumer, R value, R defaultValue) {
        if (flag) {
            attributes.add(consumer1 -> consumer.accept(consumer1, value));
        } else {
            attributes.add(consumer1 -> consumer.accept(consumer1, defaultValue));
        }
        return this;
    }

    public <R> GenericEntityBuilder<T> withSupplier(boolean flag, BiConsumer<T, R> consumer, Supplier<R> supplier, R defaultValue) {
        if (flag) {
            attributes.add(consumer1 -> consumer.accept(consumer1, supplier.get()));
        } else {
            attributes.add(consumer1 -> consumer.accept(consumer1, defaultValue));
        }
        return this;
    }

    public T build() {
        T t = instance.get();

        attributes.forEach(consumer -> consumer.accept(t));

        attributes.clear();

        return t;
    }

}