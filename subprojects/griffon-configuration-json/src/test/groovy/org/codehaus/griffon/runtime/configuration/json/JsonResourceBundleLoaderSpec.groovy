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
package org.codehaus.griffon.runtime.configuration.json

import griffon.test.core.GriffonUnitRule

import griffon.util.ResourceBundleLoader
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject
import javax.inject.Named

@Unroll
class JsonResourceBundleLoaderSpec extends Specification {
    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Inject @Named('json') private ResourceBundleLoader resourceBundleLoader

    def 'Load bundle and check #key = #value'() {
        when:
        Collection<ResourceBundle> bundles = resourceBundleLoader.load('org/codehaus/griffon/runtime/configuration/json/JsonBundle')

        then:
        bundles.size() == 1
        bundles[0].getObject(key) == value

        where:
        key << ['string', 'integer', 'keys.bar', 'foo']
        value << ['string', 42, 'bar', 'test']
    }
}
