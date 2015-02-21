package com.stantonj;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Joey on 2/21/15.
 */
public class ArgumentInfo {
    public Class type;
    public Object DefaultValue;
    public String QueryParam;
    public boolean BeanParam = false;
    public String CookieParam;
    public String FormParam;
    public String HeaderParam;
    public String PathParam;

    public static List<ArgumentInfo> ExtractArgumentInfo(Method m){
        List<ArgumentInfo> ret = new ArrayList<ArgumentInfo>();


        Class[] argumentType = m.getParameterTypes();
        Annotation[][] annotations = m.getParameterAnnotations();

        for(int i = 0; i < argumentType.length;++i){
            ArgumentInfo tmp = new ArgumentInfo();

            tmp.type = argumentType[i];
            if(tmp.type == List.class){
                tmp.DefaultValue = new ArrayList();
            }else if(tmp.type == SortedSet.class){
                tmp.DefaultValue = new TreeSet();
            }else if(tmp.type == Set.class){
                tmp.DefaultValue = new HashSet();
            }else if(tmp.type == Object.class){
                tmp.DefaultValue = null;
            }

            for(int j = 0; j < annotations[i].length;++j){
                if(annotations[i][j] instanceof DefaultValue){
                    if(tmp.DefaultValue  instanceof Collection){
                        ((Collection) tmp.DefaultValue).add(((DefaultValue)annotations[i][j]).value());
                    }
                    else {
                        try {
                            tmp.DefaultValue = tmp.type.getConstructor(String.class).newInstance(((DefaultValue) annotations[i][j]).value());
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                } else if(annotations[i][j] instanceof QueryParam){
                    tmp.QueryParam = ((QueryParam)annotations[i][j]).value();
                } else if(annotations[i][j] instanceof BeanParam){
                    tmp.BeanParam = true;
                } else if(annotations[i][j] instanceof CookieParam){
                    tmp.CookieParam = ((CookieParam)annotations[i][j]).value();
                }  else if(annotations[i][j] instanceof FormParam){
                    tmp.FormParam = ((FormParam)annotations[i][j]).value();
                }  else if(annotations[i][j] instanceof HeaderParam){
                    tmp.HeaderParam = ((HeaderParam)annotations[i][j]).value();
                }
                //TODO:MatrixParam
                else if(annotations[i][j] instanceof PathParam){
                    tmp.PathParam = ((PathParam)annotations[i][j]).value();
                }

            }

            ret.add(tmp);
        }

        return ret;
    }
}
