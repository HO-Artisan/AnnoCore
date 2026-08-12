/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.datagen.v1.provider;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.ApiStatus;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.class_1291;
import net.minecraft.class_1299;
import net.minecraft.class_1320;
import net.minecraft.class_156;
import net.minecraft.class_1761;
import net.minecraft.class_1792;
import net.minecraft.class_1887;
import net.minecraft.class_2248;
import net.minecraft.class_2405;
import net.minecraft.class_2588;
import net.minecraft.class_2960;
import net.minecraft.class_3448;
import net.minecraft.class_5321;
import net.minecraft.class_6862;
import net.minecraft.class_6880;
import net.minecraft.class_7225;
import net.minecraft.class_7403;
import net.minecraft.class_7417;
import net.minecraft.class_7784;
import net.minecraft.class_7923;

/**
 * Extend this class and implement {@link FabricLanguageProvider#generateTranslations}.
 * Make sure to use {@link FabricLanguageProvider#FabricLanguageProvider(FabricDataOutput, String, CompletableFuture) FabricLanguageProvider} to declare what language code is being generated if it isn't {@code en_us}.
 *
 * <p>Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 */
public abstract class FabricLanguageProvider implements class_2405 {
	protected final FabricDataOutput dataOutput;
	private final String languageCode;
	private final CompletableFuture<class_7225.class_7874> registryLookup;

	protected FabricLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<class_7225.class_7874> registryLookup) {
		this(dataOutput, "en_us", registryLookup);
	}

	protected FabricLanguageProvider(FabricDataOutput dataOutput, String languageCode, CompletableFuture<class_7225.class_7874> registryLookup) {
		this.dataOutput = dataOutput;
		this.languageCode = languageCode;
		this.registryLookup = registryLookup;
	}

	/**
	 * Implement this method to register languages.
	 *
	 * <p>Call {@link TranslationBuilder#add(String, String)} to add a translation.
	 */
	public abstract void generateTranslations(class_7225.class_7874 registryLookup, TranslationBuilder translationBuilder);

	@Override
	public CompletableFuture<?> method_10319(class_7403 writer) {
		TreeMap<String, String> translationEntries = new TreeMap<>();

		return this.registryLookup.thenCompose(lookup -> {
			generateTranslations(lookup, (String key, String value) -> {
				Objects.requireNonNull(key);
				Objects.requireNonNull(value);

				if (translationEntries.containsKey(key)) {
					throw new RuntimeException("Existing translation key found - " + key + " - Duplicate will be ignored.");
				}

				translationEntries.put(key, value);
			});

			JsonObject langEntryJson = new JsonObject();

			for (Map.Entry<String, String> entry : translationEntries.entrySet()) {
				langEntryJson.addProperty(entry.getKey(), entry.getValue());
			}

			return class_2405.method_10320(writer, langEntryJson, getLangFilePath(this.languageCode));
		});
	}

	private Path getLangFilePath(String code) {
		return dataOutput
				.method_45973(class_7784.class_7490.field_39368, "lang")
				.method_44107(class_2960.method_60655(dataOutput.getModId(), code));
	}

	@Override
	public String method_10321() {
		return "Language (%s)".formatted(languageCode);
	}

	/**
	 * A consumer used by {@link FabricLanguageProvider#generateTranslations}.
	 */
	@ApiStatus.NonExtendable
	@FunctionalInterface
	public interface TranslationBuilder {
		/**
		 * Adds a translation.
		 *
		 * @param translationKey The key of the translation.
		 * @param value          The value of the entry.
		 */
		void add(String translationKey, String value);

		/**
		 * Adds a translation for an {@link class_1792}.
		 *
		 * @param item  The {@link class_1792} to get the translation key from.
		 * @param value The value of the entry.
		 */
		default void add(class_1792 item, String value) {
			add(item.method_7876(), value);
		}

		/**
		 * Adds a translation for a {@link class_2248}.
		 *
		 * @param block The {@link class_2248} to get the translation key from.
		 * @param value The value of the entry.
		 */
		default void add(class_2248 block, String value) {
			add(block.method_9539(), value);
		}

		/**
		 * Adds a translation for an {@link class_1761}.
		 *
		 * @param registryKey The {@link class_5321} to get the translation key from.
		 * @param value The value of the entry.
		 */
		default void add(class_5321<class_1761> registryKey, String value) {
			final class_1761 group = class_7923.field_44687.method_31140(registryKey);
			final class_7417 content = group.method_7737().method_10851();

			if (content instanceof class_2588 translatableTextContent) {
				add(translatableTextContent.method_11022(), value);
				return;
			}

			throw new UnsupportedOperationException("Cannot add language entry for ItemGroup (%s) as the display name is not translatable.".formatted(group.method_7737().getString()));
		}

		/**
		 * Adds a translation for an {@link class_1299}.
		 *
		 * @param entityType The {@link class_1299} to get the translation key from.
		 * @param value      The value of the entry.
		 */
		default void add(class_1299<?> entityType, String value) {
			add(entityType.method_5882(), value);
		}

		/**
		 * Adds a translation for an {@link class_1887}.
		 *
		 * @param enchantment The {@link class_1887} to get the translation key from.
		 * @param value       The value of the entry.
		 */
		default void addEnchantment(class_5321<class_1887> enchantment, String value) {
			add(class_156.method_646("enchantment", enchantment.method_29177()), value);
		}

		/**
		 * Adds a translation for an {@link class_1320}.
		 *
		 * @param entityAttribute The {@link class_1320} to get the translation key from.
		 * @param value           The value of the entry.
		 */
		default void add(class_6880<class_1320> entityAttribute, String value) {
			add(entityAttribute.comp_349().method_26830(), value);
		}

		/**
		 * Adds a translation for a {@link class_3448}.
		 *
		 * @param statType The {@link class_3448} to get the translation key from.
		 * @param value    The value of the entry.
		 */
		default void add(class_3448<?> statType, String value) {
			add("stat_type." + class_7923.field_41193.method_10221(statType).toString().replace(':', '.'), value);
		}

		/**
		 * Adds a translation for a {@link class_1291}.
		 *
		 * @param statusEffect The {@link class_1291} to get the translation key from.
		 * @param value        The value of the entry.
		 */
		default void add(class_1291 statusEffect, String value) {
			add(statusEffect.method_5567(), value);
		}

		/**
		 * Adds a translation for an {@link class_2960}.
		 *
		 * @param identifier The {@link class_2960} to get the translation key from.
		 * @param value      The value of the entry.
		 */
		default void add(class_2960 identifier, String value) {
			add(identifier.method_42094(), value);
		}

		/**
		 * Adds a translation for a {@link class_6862}.
		 *
		 * @param tagKey the {@link class_6862} to get the translation key from
		 * @param value  the value of the entry
		 */
		default void add(class_6862<?> tagKey, String value) {
			add(tagKey.getTranslationKey(), value);
		}

		/**
		 * Merges an existing language file into the generated language file.
		 *
		 * @param existingLanguageFile The path to the existing language file.
		 * @throws IOException If loading the language file failed.
		 */
		default void add(Path existingLanguageFile) throws IOException {
			try (Reader reader = Files.newBufferedReader(existingLanguageFile)) {
				JsonObject translations = JsonParser.parseReader(reader).getAsJsonObject();

				for (String key : translations.keySet()) {
					add(key, translations.get(key).getAsString());
				}
			}
		}
	}
}
