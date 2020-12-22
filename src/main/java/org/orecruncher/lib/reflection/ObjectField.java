/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.lib.reflection;

import com.google.common.base.Preconditions;
import org.orecruncher.lib.Lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.function.*;

@SuppressWarnings("unused")
public class ObjectField<T, R> {

    @Nullable
    private final Field field;
    @Nonnull
    private final String className;
    @Nonnull
    private final String fieldName;
    @Nonnull
    private final Supplier<R> defaultValue;
    @Nonnull
    private final Function<T, R> getter;
    @Nonnull
    private final BiConsumer<T, R> setter;

    public ObjectField(@Nonnull final String className, @Nonnull final Supplier<R> defaultValue, @Nonnull final String... fieldName) {
        Preconditions.checkNotNull(defaultValue);
        Preconditions.checkNotNull(className);
        Preconditions.checkArgument(fieldName.length > 0, "Field name cannot be empty");
        this.defaultValue = defaultValue;
        this.className = className;
        this.fieldName = fieldName[0];
        this.field = ReflectionHelper.resolveField(className, fieldName);

        if (isNotAvailable()) {
            Lib.LOGGER.warn("Unable to locate field [%s::%s]", this.className, this.fieldName);
            this.getter = obj -> defaultValue.get();
            this.setter = (obj, v) -> {};
        } else {
            this.getter = this::getImpl;
            this.setter = this::setImpl;
        }
    }

    public ObjectField(@Nonnull final Class<T> clazz, @Nonnull final Supplier<R> defaultValue, @Nonnull final String... fieldName) {
        Preconditions.checkNotNull(defaultValue);
        Preconditions.checkNotNull(clazz);
        Preconditions.checkArgument(fieldName.length > 0, "Field name cannot be empty");
        this.defaultValue = defaultValue;
        this.className = clazz.getName();
        this.fieldName = fieldName[0];
        this.field = ReflectionHelper.resolveField(clazz, fieldName);

        if (isNotAvailable()) {
            Lib.LOGGER.warn("Unable to locate field [%s::%s]", this.className, this.fieldName);
            this.getter = obj -> defaultValue.get();
            this.setter = (obj, v) -> {};
        } else {
            this.getter = this::getImpl;
            this.setter = this::setImpl;
        }
    }

    public boolean isNotAvailable() {
        return this.field == null;
    }

    public R get(@Nonnull T obj) {
        return this.getter.apply(obj);
    }

    @SuppressWarnings("unchecked")
    private R getImpl(@Nonnull T obj) {
        try {
            return (R) this.field.get(obj);
        } catch (@Nonnull final Throwable ignored) {
        }
        return this.defaultValue.get();
    }

    public void set(@Nonnull T obj, @Nullable R value) {
        this.setter.accept(obj, value);
    }

    private void setImpl(@Nonnull T obj, @Nullable R value) {
        try {
            this.field.set(obj, value);
        } catch (@Nonnull final Throwable ignored) {
        }
    }

    protected void check() {
        if (isNotAvailable()) {
            final String msg = String.format("Uninitialized field [%s::%s]", this.className, this.fieldName);
            throw new IllegalStateException(msg);
        }
    }

}
