package com.best.cvapp.shared.ratelimit;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    int requests() default 5;       // max requests
    int seconds() default 60;       // per time window
}