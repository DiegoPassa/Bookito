package com.zerobudget.bookito.utils;

import android.util.Log;

import com.zerobudget.bookito.Flag;

public class UserFlag {

    private static final Long MIN_FEEDBACKS_FLAG = 8l;

    public static Flag getFlagFromUser(Long points, Long feedbacks) {
        /*
        TODO fare in modo che se un utente ha tanti punti e pochi feedbak, o viceversa
        allora ritorna una NORMAL FLAG

        TODO se un utente ha tanti feedback e pochi punti allora ritorna una RED FLAG

        TODO se un utente ha tanti feedback e tanti punti allora è una GREEN FLAG
        TODO se un utente è "bilanciato" ritorna una normal flag
         */
        if (feedbacks > MIN_FEEDBACKS_FLAG) {
            //per ora facciamo che "tanti feedback" equivalgono a 8
            Long total_points = points / feedbacks;

            if (total_points < 0.5) return Flag.RED_FLAG;
            else if (total_points >= 0.8) return Flag.GREEN_FLAG;
            return Flag.NORMAL_FLAG;

        }
        return Flag.NORMAL_FLAG;

    }
}
