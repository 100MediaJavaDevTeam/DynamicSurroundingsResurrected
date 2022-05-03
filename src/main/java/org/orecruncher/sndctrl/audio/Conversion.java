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

package org.orecruncher.sndctrl.audio;

import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.sounds.AudioStream;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;

@OnlyIn(Dist.CLIENT)
public final class Conversion {

    /**
     * Handles the conversion of the incoming IAudioStream into mono format as needed.
     *
     * @param inputStream The audio stream that is to be played
     * @return An IAudioStream that is in mono format
     */
    public static AudioStream convert(@Nonnull final AudioStream inputStream) {
        final AudioFormat format = inputStream.getFormat();
        if (format.getChannels() == 1)
            return inputStream;

        return new MonoStream(inputStream);
    }

    /**
     * Converts the AudioStreamBuffer into mono if needed.
     *
     * @param buffer Audio stream buffer to convert
     * @return Converted audio buffer
     */
    public static SoundBuffer convert(@Nonnull final SoundBuffer buffer) {

        final AudioFormat format = buffer.format;

        // If it is already mono return original buffer
        if (format.getChannels() == 1)
            return buffer;

        // If the sample size is not 8 or 16 bits just return the original
        int bits = format.getSampleSizeInBits();
        if (bits != 8 && bits != 16)
            return buffer;

        // Do the conversion.  Essentially it averages the values in the source buffer based on the sample size.
        boolean bigendian = format.isBigEndian();
        final AudioFormat monoformat = new AudioFormat(
                format.getEncoding(),
                format.getSampleRate(),
                bits,
                1, // Mono - single channel
                format.getFrameSize() >> 1,
                format.getFrameRate(),
                bigendian);

        final ByteBuffer source = buffer.data;
        if (source == null) {
            return buffer;
        }

        final int sourceLength = source.limit();
        final int skip = format.getFrameSize();
        for (int i = 0; i < sourceLength; i += skip) {
            final int targetIdx = i >> 1;
            if (bits == 8) {
                final int c1 = source.get(i) >> 1;
                final int c2 = source.get(i + 1) >> 1;
                final int v = c1 + c2;
                source.put(targetIdx, (byte) v);
            } else {
                final int c1 = source.getShort(i) >> 1;
                final int c2 = source.getShort(i + 2) >> 1;
                final int v = c1 + c2;
                source.putShort(targetIdx, (short) v);
            }
        }
        // Patch up the old object
        buffer.format = monoformat;
        buffer.data.rewind();
        buffer.data.limit(sourceLength >> 1);
        return buffer;
    }

    private static class MonoStream implements AudioStream {

        private final AudioStream source;

        public MonoStream(@Nonnull final AudioStream source) {
            this.source = source;
        }

        @Override
        public AudioFormat getFormat() {
            return this.source.getFormat();
        }

        @Override
        public ByteBuffer read(int size) throws IOException {
            return this.source.read(size);
        }

        @Override
        public void close() throws IOException {
            this.source.close();
        }
    }
}
