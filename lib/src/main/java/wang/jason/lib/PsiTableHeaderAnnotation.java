package wang.jason.lib;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Author: wj
 * @Date: 2018/3/8
 * @Description:
 **/



@Retention(RetentionPolicy.RUNTIME)
public @interface PsiTableHeaderAnnotation {

    public static int DECIMALISM = 0;
    public static int HEXADECIMAL = 1;

    String name() default "";
    int codeForamt() default DECIMALISM;
}
