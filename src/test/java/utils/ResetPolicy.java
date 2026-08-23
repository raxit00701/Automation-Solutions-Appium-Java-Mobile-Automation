package utils;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface ResetPolicy {
    Mode value() default Mode.INHERIT;

    enum Mode {
        INHERIT,     // use suite default (from XML or BaseTest default)
        NO_RESET,    // keep app + data
        RESET_DATA,  // clear app data only
        FAST_RESET,  // clear app data quickly (Appium pm clear, no reinstall)
        FULL_RESET   // uninstall + reinstall (slowest)
    }
}
