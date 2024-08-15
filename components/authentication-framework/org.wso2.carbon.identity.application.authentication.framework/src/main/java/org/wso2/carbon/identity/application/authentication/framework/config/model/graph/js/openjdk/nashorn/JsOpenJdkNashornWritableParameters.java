/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsWrapperFactoryProvider;

import java.util.Map;

/**
 * Parameters that can be modified from the authentication script.
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing OpenJDK Nashorn engine.
 */
public class JsOpenJdkNashornWritableParameters extends JsOpenJdkNashornParameters
        implements AbstractOpenJdkNashornJsObject {

    public JsOpenJdkNashornWritableParameters(Map wrapped) {

        super(wrapped);
    }

    public Object getMember(String name) {

        Object member = getWrapped().get(name);
        if (member instanceof Map) {
            return JsWrapperFactoryProvider.getInstance().getWrapperFactory()
                    .createJsWritableParameters((Map) member);
        }
        return member;
    }

    public void removeMember(String name) {

        super.removeMemberObject(name);
    }

    public void setMember(String name, Object value) {

        getWrapped().put(name, value);
    }
}
