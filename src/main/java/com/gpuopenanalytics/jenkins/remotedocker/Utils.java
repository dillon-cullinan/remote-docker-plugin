/*
 * Copyright (c) 2019, NVIDIA CORPORATION.
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

package com.gpuopenanalytics.jenkins.remotedocker;

import hudson.model.AbstractBuild;
import hudson.util.VariableResolver;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern VAR_REGEX = Pattern.compile(
            "\\$(\\w+)|\\$\\{([^}]+)}");

    private Utils(){

    }


    /**
     * Finds <code>$VAR</code> or <code>${VAR}</code> in the specified string
     * that exist in the build's resolver and resolves them. If the variable is
     * not resolved, it is left unchanged.
     *
     * @param build
     * @param s
     * @return
     */
    public static String resolveVariables(AbstractBuild build, String s) {
        return resolveVariables(build.getBuildVariableResolver(), s);
    }

    /**
     * Finds <code>$VAR</code> or <code>${VAR}</code> in the specified string
     * that exist in the resolver and resolves them. If the variable is not
     * resolved, it is left unchanged.
     *
     * @param resolver
     * @param s
     * @return
     */
    public static String resolveVariables(VariableResolver<String> resolver,
                                          String s) {
        //Matcher requires StringBuffer :(
        StringBuffer sb = new StringBuffer();
        Matcher m = VAR_REGEX.matcher(s);
        while (m.find()) {
            String varName = java.util.Optional.ofNullable(m.group(1))
                    .orElseGet(() -> m.group(2));
            Optional<String> newValue = Optional.ofNullable(
                    resolver.resolve(varName));
            m.appendReplacement(sb, newValue.orElseGet(() -> "\\$" + varName));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static boolean hasVariablesToResolve(String s) {
        Matcher m = VAR_REGEX.matcher(s);
        return m.find();
    }
}
