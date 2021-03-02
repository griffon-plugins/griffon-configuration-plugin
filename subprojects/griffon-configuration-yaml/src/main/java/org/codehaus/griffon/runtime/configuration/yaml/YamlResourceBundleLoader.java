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
package org.codehaus.griffon.runtime.configuration.yaml;

import griffon.annotations.core.Nonnull;
import griffon.core.resources.ResourceHandler;
import griffon.util.AbstractMapResourceBundle;
import griffon.util.ResourceBundleReader;
import org.codehaus.griffon.runtime.util.AbstractResourceBundleLoader;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
@Named("yaml")
public class YamlResourceBundleLoader extends AbstractResourceBundleLoader {
    protected static final String YAML_SUFFIX = ".yml";

    protected final ResourceBundleReader resourceBundleReader;

    @Inject
    public YamlResourceBundleLoader(@Nonnull ResourceHandler resourceHandler,
                                    @Nonnull ResourceBundleReader resourceBundleReader) {
        super(resourceHandler);
        this.resourceBundleReader = requireNonNull(resourceBundleReader, "Argument 'resourceBundleReader' must not be null");
    }

    @Nonnull
    @Override
    public Collection<ResourceBundle> load(@Nonnull String name) {
        requireNonBlank(name, ERROR_FILENAME_BLANK);
        List<ResourceBundle> bundles = new ArrayList<>();
        List<URL> resources = getResources(name, YAML_SUFFIX);

        if (resources != null) {
            Yaml yaml = new Yaml();
            for (URL resource : resources) {
                if (null == resource) {
                    continue;
                }
                try {
                    final Map map = yaml.loadAs(resource.openStream(), Map.class);
                    ResourceBundle bundle = resourceBundleReader.read(new AbstractMapResourceBundle() {
                        @Override
                        protected void initialize(@Nonnull Map<String, Object> entries) {
                            entries.putAll(map);
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
