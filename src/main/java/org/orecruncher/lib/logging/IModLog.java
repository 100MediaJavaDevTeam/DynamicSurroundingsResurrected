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

package org.orecruncher.lib.logging;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface IModLog {

    default void info(@Nonnull final String msg, @Nullable final Object... parms) { }

    default void info(@Nonnull final Supplier<String> message) {
        info(message.get());
    }

    default void warn(@Nonnull final String msg, @Nullable final Object... parms) { }

    default void warn(@Nonnull final Supplier<String> message) {
        warn(message.get());
    }

    default void debug(@Nonnull final String msg, @Nullable final Object... parms) { }

    default void debug(@Nonnull final Supplier<String> message) {
        debug(message.get());
    }

    default void debug(final int mask, @Nonnull final String msg, @Nullable final Object... parms) { }

    default void debug(final int mask, @Nonnull final Supplier<String> message) {
        debug(message.get());
    }

    default void error(@Nonnull final Throwable e, @Nonnull final String msg, @Nullable final Object... parms) { }

    default void error(@Nonnull final Throwable e, @Nonnull final Supplier<String> message) {
        error(e, message.get());
    }
}
