package com.zerobudget.bookito.utils;

import android.util.Log;

import com.zerobudget.bookito.Flag;

public class UserFlag {

    private static final Long MIN_FEEDBACKS_FLAG = 8l;

    public static Flag getFlagFromUser(Number points, Number feedbacks) {

        if (feedbacks.longValue() > MIN_FEEDBACKS_FLAG) {
            //per ora facciamo che "tanti feedback" equivalgono a 8
            Double points_mean = points.doubleValue()/feedbacks.longValue();

            if (points_mean < 0.2) return Flag.RED_FLAG;
            else if (points_mean >= 0.47) return Flag.GREEN_FLAG;
        }
        return Flag.NORMAL_FLAG;

    }
}
