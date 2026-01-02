package com.relyon.metasmart.config.subscription;

import com.relyon.metasmart.entity.subscription.SubscriptionTier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresSubscription {

    SubscriptionTier minTier() default SubscriptionTier.PREMIUM;

    String[] features() default {};
}
