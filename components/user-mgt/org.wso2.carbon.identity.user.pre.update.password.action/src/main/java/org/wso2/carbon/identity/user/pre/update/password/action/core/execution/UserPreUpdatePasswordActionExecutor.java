/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.pre.update.password.action.core.execution;

import org.wso2.carbon.identity.action.execution.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.user.action.service.UserActionExecutor;
import org.wso2.carbon.identity.user.action.service.model.UserActionContext;
import org.wso2.carbon.identity.user.pre.update.password.action.core.constant.PreUpdatePasswordActionConstants;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.PreUpdatePasswordActionServiceComponentHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * User Pre Update Password Action Executor.
 */
public class UserPreUpdatePasswordActionExecutor implements UserActionExecutor {

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.PRE_UPDATE_PASSWORD;
    }

    @Override
    public ActionExecutionStatus<?> execute(UserActionContext userActionContext, String tenantDomain)
            throws ActionExecutionException {

        Map<String, Object> eventContext = new HashMap<>();
        eventContext.put(PreUpdatePasswordActionConstants.USER_ACTION_CONTEXT, userActionContext);

        return PreUpdatePasswordActionServiceComponentHolder.getInstance().getActionExecutorService()
                .execute(getSupportedActionType(), eventContext, tenantDomain);
    }
}
