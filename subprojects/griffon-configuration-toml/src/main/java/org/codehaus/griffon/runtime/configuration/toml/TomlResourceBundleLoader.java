/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2017-2021 The author and/or original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.configuration.toml;

import com.github.jezza.Toml;
import com.github.jezza.TomlTable;
import griffon.annotations.core.Nonnull;
import griffon.core.resources.ResourceHandler;
import griffon.util.AbstractMapResourceBundle;
import griffon.util.ResourceBundleReader;
import org.codehaus.griffon.runtime.util.AbstractResourceBundleLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
@Named("toml")
public class TomlResourceBundleLoader extends AbstractResourceBundleLoader {
    protected static final String TOML_SUFFIX = ".toml";

    protected final ResourceBundleReader resourceBundleReader;

    @Inject
    public TomlResourceBundleLoader(@Nonnull ResourceHandler resourceHandler,
                                    @Nonnull ResourceBundleReader resourceBundleReader) {
        super(resourceHandler);
        this.resourceBundleReader = requireNonNull(resourceBundleReader, "Argument 'resourceBundleReader' must not be null");
    }

    @Nonnull
    @Override
    public Collection<ResourceBundle> load(@Nonnull String name) {
        requireNonBlank(name, ERROR_FILENAME_BLANK);
        List<ResourceBundle> bundles = new ArrayList<>();
        List<URL> resources = getResources(name, TOML_SUFFIX);

        if (resources != null) {
            for (URL resource : resources) {
                if (null == resource) {
                    continue;
                }
                try {
                    final Map<String, Object> map = Toml.from(resource.openStream()).asMap();
                    ResourceBundle bundle = resourceBundleReader.read(new AbstractMapResourceBundle() {
                        @Override
                        protected void initialize(@Nonnull Map<String, Object> entries) {
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                if (entry.getValue() instanceof TomlTable) {
                                    entries.put(entry.getKey(), convertToMap((TomlTable) entry.getValue()));
                                } else {
                                    entries.put(entry.getKey(), entry.getValue());
                                }
                            }
                        }

                        private Map<String, Object> convertToMap(TomlTable value) {
                            Map<String, Object> map = new LinkedHashMap<>();
                            for (Map.Entry<String, Object> entry : value.asMap().entrySet()) {
                                if (entry.getValue() instanceof TomlTable) {
                                    map.put(entry.getKey(), convertToMap((TomlTable) entry.getValue()));
                                } else {
                                    map.put(entry.getKey(), entry.getValue());
                                }
                            }
                            return map;
                        }
                    });
                    bundles.add(bundle);
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return bundles;
    }
}
