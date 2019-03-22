package wang.jason.lib;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FactoryAnnotation {

    String route();

    Class type();

}
