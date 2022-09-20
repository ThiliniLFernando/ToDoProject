package com.smart.planner.Classes;

import android.util.Patterns;

import java.util.regex.Pattern;

public class PatternUtils {

    public static boolean isValidEmailAddress(CharSequence email){
        if (email == null){
            return false ;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidDate(CharSequence date){
        if (date == null){
            return false ;
        }

        if (Pattern.matches("([0-9]{4}/[0-9]{2}/[0-9]{2})",date)){
            return true ;
        }else {
            return false ;
        }
    }
}
