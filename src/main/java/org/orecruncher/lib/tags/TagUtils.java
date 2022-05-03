/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
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

package org.orecruncher.lib.tags;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public final class TagUtils {

    private TagUtils() {

    }

    private static TagContainer supplier;

    public static void setTagManager(@Nonnull final TagContainer manager) {
        supplier = manager;
    }

    public static void clearTagManager() {
        supplier = null;
    }

    @Nullable
    public static Tag<Block> getBlockTag(@Nonnull final String name) {
        return getBlockTag(new ResourceLocation(name));
    }

    @Nullable
    public static Tag<Block> getBlockTag(@Nonnull final ResourceLocation res) {

        return BlockTags.getAllTags().getTag(res);
    }

    public static Stream<String> dumpBlockTags() {

        final TagCollection<Block> collection = BlockTags.getAllTags();

        return collection.getAvailableTags().stream().map(loc -> {
            final StringBuilder builder = new StringBuilder();
            builder.append(loc.toString()).append(" -> ");
            final Tag<Block> tag = collection.getTag(loc);
            final String text;
            if (tag == null) {
                text = "<NULL>";
            } else {
                text = tag.getValues().stream().map(l -> l.getRegistryName().toString()).collect(Collectors.joining(","));
            }
            builder.append(text);
            return builder.toString();
        }).sorted();
    }
}
