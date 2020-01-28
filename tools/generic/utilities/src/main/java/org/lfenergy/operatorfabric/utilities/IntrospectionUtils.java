/* Copyright (c) 2020, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.lfenergy.operatorfabric.utilities;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class IntrospectionUtils {

    public static Object extractPath(Object current, String path) {
        String[] pathElements = path.split("\\.");
        for(int i = 0;current!=null && i < pathElements.length;i++){
            current = extractData(current, pathElements[i]);
        }
        return current;
    }

    public static Object extractData(Object current, String name) {
        if(current instanceof Map)
            return ((Map)current).get(name);
        Method method = null;
        String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
        try {
            method = current.getClass().getDeclaredMethod(methodName);
        }catch (NoSuchMethodException nsm){
            log.info("No method \""+methodName+"\" found for object of class \""+current.getClass().getSimpleName()+"\"");
        }

        if(method!=null)
            return invokeSafe(current, method);

        try {
            method = current.getClass().getDeclaredMethod(name);
        }catch (NoSuchMethodException nsm){
            log.info("No method \""+name+"\" found for object of class \""+current.getClass().getSimpleName()+"\"");
        }

        if(method!=null)
            return invokeSafe(current, method);
        return null;
    }

    /**
     * invoke method returning null in case of underlying exception
     * @param current
     * @param method
     * @return
     */
    public static Object invokeSafe(Object current, Method method) {
        try {
            return method.invoke(current);
        } catch (IllegalAccessException e) {
            log.info("Unnable to access method\""+method.getName()+"\" of class \""+current.getClass().getSimpleName()+"\"");
        } catch (InvocationTargetException e) {
            log.info("No method \""+method.getName()+"\" found for object of class \""+current.getClass().getSimpleName()+"\"");
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage(),e);

        }
        return null;
    }
}
