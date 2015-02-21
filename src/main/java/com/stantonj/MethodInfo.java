package com.stantonj;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Joey on 2/20/15.
 */
public class MethodInfo {
    public String Path;
    public String HttpMethod;
    public Method method;
    public Object instance;
    public Set<String> Produces;
    public List<ArgumentInfo> argumentInfo;

    public static MethodInfo[] GetServletMethodInfo(Class<? extends Object> servlet) {
        Method[] methods = servlet.getDeclaredMethods();
        LinkedList<MethodInfo> Endpoints = new LinkedList<MethodInfo>();

        for (Method m : methods) {
            if (!Modifier.isPublic(m.getModifiers())) {
                continue;
            }
            Annotation[] annotations = m.getDeclaredAnnotations();
            MethodInfo info = new MethodInfo();
            info.method = m;
            //TODO: get mediainfo
            //TODO: add query string parameters
            for (Annotation an : annotations) {
                if (an instanceof javax.ws.rs.Path) {
                    info.Path = ((javax.ws.rs.Path) an).value();
                } else if (an instanceof javax.ws.rs.HttpMethod) {
                    info.HttpMethod = ((HttpMethod) an).value();
                } else if (an instanceof GET) {
                    info.HttpMethod = "GET";
                } else if (an instanceof POST) {
                    info.HttpMethod = "POST";
                } else if (an instanceof PUT) {
                    info.HttpMethod = "PUT";
                } else if (an instanceof DELETE) {
                    info.HttpMethod = "DELETE";
                } else if (an instanceof HEAD) {
                    info.HttpMethod = "HEAD";
                } else if (an instanceof Produces){
                    info.Produces = new HashSet<String>();
                    for(String type: ((Produces) an).value()){
                        info.Produces.add(type);
                    }
                }
            }

            //Argument Annotations
            info.argumentInfo = ArgumentInfo.ExtractArgumentInfo(m);

            if (info.Path == null)
                continue;
            if(!info.Path.startsWith("/"))
                info.Path = "/" + info.Path;

            if(info.Produces == null){
                info.Produces = new HashSet<String>();
                info.Produces.add("*/*");
            }

            Endpoints.add(info);

        }
        //TODO: check for class Path annotation

        String PathPrefix = null;
        Annotation[] annotations = servlet.getDeclaredAnnotations();

        for (Annotation an : annotations) {
            if (an instanceof javax.ws.rs.Path) {
                PathPrefix = ((Path) an).value();
                break;
            }
        }

        if(!PathPrefix.startsWith("/")){
            PathPrefix = "/" + PathPrefix;
        }
        if(PathPrefix.endsWith("/")){
            PathPrefix = PathPrefix.substring(0, PathPrefix.length() - 1);
        }
        for(MethodInfo info : Endpoints){
            info.Path = PathPrefix + info.Path;
        }

        return Endpoints.toArray(new MethodInfo[Endpoints.size()]);
    }
}
