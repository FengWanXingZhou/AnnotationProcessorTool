package wang.jason.lib;

import com.squareup.javapoet.*;


import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenerateCodeTool {

    private Elements elementUtils;
    private Messager messager;
    private Filer mFiler;
    public static final String STORE_SUFFIX = "DataStore";
    public static final String STORE_IMPL_SUFFIX = "DataStoreImpl";
    public static final String FACTORY_SUFFIX = "Factory";
    public GenerateCodeTool(Elements elementUtils, Messager messager, Filer mFiler) {
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.mFiler = mFiler;
    }

    public void generateCode(HashMap<String, FactoryAnnotationClass> factoryAnnotationClassHashMap){



        for(FactoryAnnotationClass factoryAnnotationClass:factoryAnnotationClassHashMap.values()){


            generateStoreInterfaceCode(factoryAnnotationClass);
            //generateStoreImplCode(factoryAnnotationClass);
            generateFactoryCode(factoryAnnotationClass);

        }

    }

    private void generateStoreInterfaceCode(FactoryAnnotationClass factoryAnnotationClass){
        String className = factoryAnnotationClass.getSimpleTypeName();
       /* messager.printMessage(Diagnostic.Kind.NOTE
                ,String.format("className:"+className));*/

        MethodSpec create = null;
        if(factoryAnnotationClass.getType() == null){
            create = MethodSpec.methodBuilder("create"+className)
                    .returns(TypeName.get(factoryAnnotationClass.getTypeMirror()))
                    .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                    .addParameter(String.class,"route")
                    .build();
        }else {

            create = MethodSpec.methodBuilder("create" + className)
                    .returns(factoryAnnotationClass.getType())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(String.class, "route")
                    .build();
        }
        //Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC};
        /*messager.printMessage(Diagnostic.Kind.NOTE
                ,String.format("start generate interface"));*/
        TypeSpec storeInterface = TypeSpec.interfaceBuilder(className
                +STORE_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(create)
                .build();

        /*messager.printMessage(Diagnostic.Kind.NOTE
                ,String.format("start generate file"));
        messager.printMessage(Diagnostic.Kind.NOTE
                ,String.format("start generate package:"+factoryAnnotationClass.getPackageName()));*/
        JavaFile javaFile = JavaFile.builder(factoryAnnotationClass.getPackageName(), storeInterface)
                .build();
        try {
            javaFile.writeTo(mFiler);
        }catch (Exception e){
            //error(element," generate code error");
            e.printStackTrace();
            //throw new Exception("generate code error");
        }
    }

    public void generateStoreImplCodeCache(HashMap<String,FactoryAnnotationClass> factoryAnnotationClassHashMap){

        if(factoryAnnotationClassHashMap.size()== 0){
            return;
        }
        List<String> routeList = new ArrayList<>();
        boolean firstIn = true;
        MethodSpec.Builder create = null;
        String packageName = null;
        String className = null;
        for(FactoryAnnotationClass factoryAnnotationClass:factoryAnnotationClassHashMap.values()){
            routeList.add(factoryAnnotationClass.getRoute());
            className = factoryAnnotationClass.getSimpleTypeName();



            ClassName productSuperClassName =
                    ClassName.get(factoryAnnotationClass.getPackageName()
                            ,factoryAnnotationClass.getQualifiedSuperClassName());
            if(firstIn){

                packageName = factoryAnnotationClass.getPackageName();
                create = MethodSpec.methodBuilder("create"+className)
                        .returns(productSuperClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String.class,"route");
            }


            create.beginControlFlow("if(route.equals($S))",factoryAnnotationClass.getRoute())
                    .addStatement("return new $T()",TypeName.get(factoryAnnotationClass.getTypeElement().asType()))
                    .endControlFlow();



            firstIn = false;

        }
        create.addStatement("return null")
        ;






        ClassName superClassName = ClassName.get(packageName
                ,className+STORE_SUFFIX);


        TypeSpec storeInterface = TypeSpec.classBuilder(className
                +STORE_IMPL_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(superClassName)
                .addMethod(create.build())
                .build();


        JavaFile javaFile = JavaFile.builder(packageName, storeInterface)
                .build();
        try {
            javaFile.writeTo(mFiler);
        }catch (Exception e){
            //error(element," generate code error");
            e.printStackTrace();
            //throw new Exception("generate code error");
        }
    }

    private void generateStoreImplCode(FactoryAnnotationClass factoryAnnotationClass){
        String className = factoryAnnotationClass.getSimpleTypeName();
        /*messager.printMessage(Diagnostic.Kind.NOTE
                ,String.format("className:"+className));*/

        MethodSpec create = null;
        ClassName productSuperClassName =
                ClassName.get(factoryAnnotationClass.getPackageName()
                        ,factoryAnnotationClass.getQualifiedSuperClassName());


        /*create = MethodSpec.methodBuilder("create"+className)
                .returns(productSuperClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class,"route")
                .addStatement("return new $T()",TypeName.get(factoryAnnotationClass.getTypeElement().asType()))
                .build();*/
        create = MethodSpec.methodBuilder("create"+className)
                .returns(productSuperClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class,"route")
                .beginControlFlow("if(route.equals($S))",factoryAnnotationClass.getRoute())
                .addStatement("return new $T()",TypeName.get(factoryAnnotationClass.getTypeElement().asType()))
                .nextControlFlow("else")
                .addStatement("return null")
                .endControlFlow()
                .build();


        ClassName superClassName = ClassName.get(factoryAnnotationClass.getPackageName()
                ,factoryAnnotationClass.getSimpleTypeName()+STORE_SUFFIX);


        TypeSpec storeInterface = TypeSpec.classBuilder(className
                +STORE_IMPL_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(superClassName)
                .addMethod(create)
                .build();


        JavaFile javaFile = JavaFile.builder(factoryAnnotationClass.getPackageName(), storeInterface)
                .build();
        try {
            javaFile.writeTo(mFiler);
        }catch (Exception e){
            //error(element," generate code error");
            e.printStackTrace();
            //throw new Exception("generate code error");
        }
    }

    private void generateFactoryCode(FactoryAnnotationClass factoryAnnotationClass){

        ClassName storeClassName = ClassName
                .get(factoryAnnotationClass.getPackageName(),
                        factoryAnnotationClass.getSimpleTypeName()+STORE_SUFFIX);
        ClassName storeImplClassName = ClassName
                .get(factoryAnnotationClass.getPackageName(),
                        factoryAnnotationClass.getSimpleTypeName()+STORE_IMPL_SUFFIX);
        MethodSpec create = MethodSpec.methodBuilder("create"+factoryAnnotationClass.getSimpleTypeName()
                    +STORE_SUFFIX)
                .returns(storeClassName)
                .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                .addStatement("return new $T()",storeImplClassName)
                .build();
        ClassName testClassName = ClassName.get("io.reactivex","Observable");
        MethodSpec test = MethodSpec.methodBuilder("create"
                +STORE_SUFFIX)
                .returns(testClassName)
                .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                .addStatement("return new $T()",testClassName)
                .build();
        TypeSpec factoryClass = TypeSpec.classBuilder(factoryAnnotationClass.getSimpleTypeName()+STORE_SUFFIX
                +FACTORY_SUFFIX)
                .addModifiers(Modifier.PUBLIC)

                .addMethod(create)
                //.addMethod(test)
                .build();
        JavaFile javaFile = JavaFile.builder(factoryAnnotationClass.getPackageName(), factoryClass)
                .build();
        try {
            javaFile.writeTo(mFiler);
        }catch (Exception e){
            //error(element," generate code error");
            e.printStackTrace();
            //throw new Exception("generate code error");
        }
    }
}
