package wang.jason.lib;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface FactoryAnnotation {

    String route();

    Class type();

    Class[] params() default {Void.class};

}
