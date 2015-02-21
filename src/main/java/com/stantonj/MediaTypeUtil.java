package com.stantonj;

/**
 * Created by Joey on 2/20/15.
 */
public class MediaTypeUtil {

    static boolean isValidType(String Standard, String test) {
        String[] theirParts = test.split("/");
        String[] myParts = Standard.split("/");
        if(!myParts[0].equals("*") && !myParts[0].equals(theirParts[0]))
            return false;
        if(!myParts[1].equals("*") && !myParts[1].equals(theirParts[1]))
            return false;

        return true;
    }
}
