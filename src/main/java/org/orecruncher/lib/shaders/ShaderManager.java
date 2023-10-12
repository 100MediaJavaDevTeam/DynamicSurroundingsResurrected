/*
 *  Dynamic Surroundings
 *  Copyright (C) 2020  OreCruncher
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.lib.shaders;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.orecruncher.lib.GameUtils;
import org.orecruncher.lib.Lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class ShaderManager<T extends Enum<T> & IShaderResourceProvider> {

	private final Class<T> clazz;
	private final EnumMap<T, ShaderProgram> programs;
	private final Supplier<Boolean> supportCheck;

	public ShaderManager(@Nonnull final Class<T> clazz) {
		this(clazz, () -> true);
	}

	public ShaderManager(@Nonnull final Class<T> clazz, @Nonnull final Supplier<Boolean> supportCheck) {
		Objects.requireNonNull(clazz);
		this.clazz = clazz;
		this.programs = new EnumMap<>(clazz);
		this.supportCheck = supportCheck;

		// Validate the entries provide sane info
		for (final T shader : clazz.getEnumConstants()) {
			final String shaderName = shader.name();
			Objects.requireNonNull(shader.getVertex(), String.format("%s provided null for vertex shader", shaderName));
			Objects.requireNonNull(shader.getFragment(), String.format("%s provided null for fragment shader", shaderName));
		}
	}

	public boolean supported() {
		return this.supportCheck.get();
	}

	public void useShader(@Nonnull final T shader, @Nullable final Consumer<ShaderCallContext> callback) {
		Objects.requireNonNull(shader);

		if (!supported())
			return;

		final ShaderProgram program = this.programs.get(shader);

		if (program == null)
			return;

		final int programId = program.getId();
		ProgramManager.glUseProgram(programId);

		if (callback != null) {
			callback.accept(new ShaderCallContext(program));
		}
	}

	@SuppressWarnings("unused")
	public void useShader(@Nonnull final T shader) {
		useShader(shader, null);
	}

	public void releaseShader() {
		if (supported())
			ProgramManager.glUseProgram(0);
	}

	@SuppressWarnings("deprecation")
	public void initShaders() {
		if (!supported())
			return;

		if (GameUtils.getMC().getResourceManager() instanceof ReloadableResourceManager) {
			((ReloadableResourceManager) GameUtils.getMC().getResourceManager()).registerReloadListener(
					(ResourceManagerReloadListener) manager -> {
						this.programs.values().forEach(ProgramManager::releaseProgram);
						this.programs.clear();
						loadShaders(manager);
					});
		}
	}

	private void loadShaders(@Nonnull final ResourceManager manager) {
		for (final T shader : this.clazz.getEnumConstants()) {
			final ShaderProgram program = createProgram(manager, shader);
			if (program != null)
				this.programs.put(shader, createProgram(manager, shader));
		}
	}

	@Nullable
	private ShaderProgram createProgram(@Nonnull final ResourceManager manager, @Nonnull final T shader) {
		try {
			final Program vert = createShader(manager, shader.getVertex(), Program.Type.VERTEX);
			if(vert == null)
				return null;
			final Program frag = createShader(manager, shader.getFragment(), Program.Type.FRAGMENT);
			if(frag == null)
				return null;
			final int programId = ProgramManager.createProgram();
			final ShaderProgram program = new ShaderProgram(shader.getShaderName(), programId, vert, frag);
			ProgramManager.linkShader(program);
			program.setUniforms(shader.getUniforms());
			return program;
		} catch (IOException ex) {
			Lib.LOGGER.error(ex, "Failed to load program %s", shader.getShaderName());
		}
		return null;
	}

	private static Program createShader(@Nonnull final ResourceManager manager, @Nonnull final ResourceLocation loc, @Nonnull final Program.Type shaderType) throws IOException {
		Optional<Resource> stream = manager.getResource(loc);
		if(stream.isEmpty())return null;
		try (InputStream is = new BufferedInputStream(stream.get().open())) {
			return Program.compileShader(shaderType, loc.toString(), is, shaderType.name().toLowerCase(Locale.ROOT),new GlslPreprocessor() {
				private final Set<String> importedPaths = Sets.newHashSet();

				public String applyImport(boolean p_173374_, String p_173375_) {

					String s = "environs:shaders/" + loc.getPath() + shaderType.getExtension();
					final String s1 = FileUtil.getFullResourcePath(s);
					p_173375_ = FileUtil.normalizeResourcePath((p_173374_ ? s1 : "environs:shaders/") + p_173375_);
					if (!this.importedPaths.add(p_173375_)) {
						return null;
					} else {
						ResourceLocation resourcelocation1 = new ResourceLocation(p_173375_);

						try {
							Resource resource1 = manager.getResource(resourcelocation1).get();

							String s2;
							try {
								s2 = IOUtils.toString(resource1.open(), StandardCharsets.UTF_8);
							} catch (Throwable throwable1) {
								if (resource1 != null) {
									try {
										//todo: figure out
//										resource1.close();
									} catch (Throwable throwable) {
										throwable1.addSuppressed(throwable);
									}
								}

								throw throwable1;
							}

							if (resource1 != null) {
								//todo: figure out
//								resource1.close();
							}

							return s2;
						} catch (IOException ioexception) {
//							ShaderInstance.LOGGER.error("Could not open GLSL import {}: {}", p_173375_, ioexception.getMessage());
							return "#error " + ioexception.getMessage();
						}
					}
				}
			});
		}
	}
}
