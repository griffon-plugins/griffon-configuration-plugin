/*
 * Copyright 2014-2015 the original author or authors.
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
package griffon.plugins.configuration.editors;

import griffon.core.editors.AbstractPropertyEditor;
import griffon.metadata.PropertyEditorFor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
@PropertyEditorFor(Map.class)
public class MapPropertyEditor extends AbstractPropertyEditor {
    @Override
    protected void setValueInternal(Object value) {
        if (null == value) {
            super.setValueInternal(null);
        } else if (value instanceof CharSequence) {
            handleAsString(String.valueOf(value));
        } else if (value instanceof Map) {
            super.setValueInternal(value);
        } else {
            throw illegalValue(value, Map.class);
        }
    }

    private void handleAsString(String value) {
        try {
            Map map = new LinkedHashMap();
            if (!isBlank(value)) {
                JSONObject jsonObject = new JSONObject(new JSONTokener(value));
                parseInto(jsonObject, map);
            }
            super.setValueInternal(map);
        } catch (Exception e) {
            throw illegalValue(value, Map.class, e);
        }
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private void parseInto(@Nonnull JSONObject json, @Nonnull Map map) {
        for (Object k : json.keySet()) {
            String key = String.valueOf(k);
            Object value = json.get(key);
            if (value instanceof JSONObject) {
                Map inner = new LinkedHashMap();
                map.put(key, inner);
                parseInto((JSONObject) value, inner);
            } else if (value instanceof JSONArray) {
                map.put(key, expand((JSONArray) value));
            } else {
                map.put(key, value);
            }
        }
    }

    @Nonnull
    private Collection expand(@Nonnull JSONArray array) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object element = array.get(i);
            if (element instanceof JSONObject) {
                Map map = new LinkedHashMap();
                parseInto((JSONObject) element, map);
                list.add(map);
            }
            if (element instanceof JSONArray) {
                list.add(expand((JSONArray) element));
            } else {
                list.add(element);
            }
        }

        return list;
    }
}
