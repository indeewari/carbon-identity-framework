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

package org.wso2.carbon.identity.input.validation.mgt.model;

import org.wso2.carbon.identity.input.validation.mgt.exceptions.InputValidationMgtClientException;

import java.util.List;

/**
 * API of the Application Authenticators.
 */
public interface Validator {

    /**
     * Check whether the validation can be handled by the validator.
     *
     * @param validatorName Name of the validator.
     * @return boolean
     */
    boolean canHandle(String validatorName);

    /**
     * Validate the string against the validation criteria.
     *
     * @param context Validation Context.
     * @return  boolean
     */
    boolean validate(ValidationContext context) throws InputValidationMgtClientException;

    /**
     * Validate the configuration properties.
     *
     * @param context Validation Context.
     * @return  boolean
     */
    boolean validateProps(ValidationContext context) throws InputValidationMgtClientException;

    /**
     * Get list of supported properties.
     *
     * @return  List of validator properties.
     */
    List<Property> getConfigurationProperties();
}
