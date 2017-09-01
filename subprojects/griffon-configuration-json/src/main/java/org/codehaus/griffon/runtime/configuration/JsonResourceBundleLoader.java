/*
 * Copyright 2017 the original author or authors.
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
package org.codehaus.griffon.runtime.configuration;

import griffon.core.resources.ResourceHandler;
import griffon.util.AbstractMapResourceBundle;
import griffon.util.ResourceBundleReader;
import org.codehaus.griffon.runtime.util.AbstractResourceBundleLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
@Named("json")
public class JsonResourceBundleLoader extends AbstractResourceBundleLoader {
    private static final Logger LOG = LoggerFactory.getLogger(JsonResourceBundleLoader.class);
    protected static final String JSON_SUFFIX = ".json";

    protected final ResourceBundleReader resourceBundleReader;

    @Inject
    public JsonResourceBundleLoader(@Nonnull ResourceHandler resourceHandler,
                                    @Nonnull ResourceBundleReader resourceBundleReader) {
        super(resourceHandler);
        this.resourceBundleReader = requireNonNull(resourceBundleReader, "Argument 'resourceBundleReader' must not be null");
    }

    @Nonnull
    @Override
    public Collection<ResourceBundle> load(@Nonnull String name) {
        requireNonBlank(name, ERROR_FILENAME_BLANK);
        List<ResourceBundle> bundles = new ArrayList<>();
        List<URL> resources = getResources(name, JSON_SUFFIX);

        if (resources != null) {
            for (URL resource : resources) {
                if (null == resource) { continue; }
                try {
                    JSONObject json = new JSONObject(new JSONTokener(resource.openStream()));
                    final Map<String, Object> map = new LinkedHashMap<>();
                    readInto(json, map);
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

    protected void readInto(@Nonnull JSONObject json, @Nonnull Map<String, Object> map) {
        for (Object k : json.keySet()) {
            String key = String.valueOf(k);
            Object value = json.get(key);
            if (value instanceof JSONObject) {
                Map<String, Object> submap = new LinkedHashMap<>();
                map.put(key, submap);
                readInto((JSONObject) value, submap);
            } else if (value instanceof JSONArray) {
                List<Object> list = new ArrayList<>();
                map.put(key, list);
                readInto((JSONArray) value, list);
            } else if (value instanceof Number ||
                value instanceof Boolean ||
                value instanceof CharSequence) {
                map.put(key, value);
            } else {
                throw new IllegalArgumentException("Invalid value for '" + key + "' => " + value);
            }
        }
    }

    private void readInto(@Nonnull JSONArray json, @Nonnull List<Object> list) {
        int length = json.length();
        for (int i = 0; i < length; i++) {
            Object value = json.get(i);
            if (value instanceof JSONObject) {
                Map<String, Object> submap = new LinkedHashMap<>();
                list.add(submap);
                readInto((JSONObject) value, submap);
            } else if (value instanceof JSONArray) {
                List<?> sublist = new ArrayList<>();
                list.add(sublist);
                readInto((JSONArray) value, list);
            } else if (value instanceof Number ||
                value instanceof Boolean ||
                value instanceof CharSequence) {
                list.add(value);
            } else {
                throw new IllegalArgumentException("Invalid value for index '" + i + "' => " + value);
            }
        }
    }
}
