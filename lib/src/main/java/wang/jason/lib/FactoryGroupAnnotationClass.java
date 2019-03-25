package wang.jason.lib;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;


import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.HashMap;

public class FactoryGroupAnnotationClass {


    private HashMap<String,FactoryAnnotationClass> factoryAnnotationClassHashMap = new HashMap<>();
    private Filer mFiler;


    private static final String SUFFIX = "Factory";

    private String qualifiedClassName;


    public FactoryGroupAnnotationClass(Filer filer,String qualifiedClassName){
        this.mFiler = filer;
        this.qualifiedClassName = qualifiedClassName;
    }


    public void insert(String route, FactoryAnnotationClass factoryAnnotationClass){
        factoryAnnotationClassHashMap.put(route,factoryAnnotationClass);
    }

    public FactoryAnnotationClass getFactoryAnnotationClass(String route){
        return factoryAnnotationClassHashMap.get(route);
    }

    public HashMap<String, FactoryAnnotationClass> getFactoryAnnotationClassHashMap() {
        return factoryAnnotationClassHashMap;
    }







}
