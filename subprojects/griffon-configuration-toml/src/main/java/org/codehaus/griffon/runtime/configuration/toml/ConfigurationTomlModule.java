/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2017-2020 The author and/or original authors.
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

import griffon.annotations.inject.DependsOn;
import griffon.core.injection.Module;
import griffon.util.ResourceBundleLoader;
import org.codehaus.griffon.runtime.core.injection.AbstractModule;
import org.kordamp.jipsy.ServiceProviderFor;

import javax.inject.Named;

/**
 * @author Andres Almiray
 */
@DependsOn("core")
@Named("configuration-toml")
@ServiceProviderFor(Module.class)
public class ConfigurationTomlModule extends AbstractModule {
    @Override
    protected void doConfigure() {
        // tag::bindings[]
        bind(ResourceBundleLoader.class)
            .to(TomlResourceBundleLoader.class)
            .asSingleton();
        // end::bindings[]
    }
}
