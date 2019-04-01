package wang.jason.lib;

import com.google.auto.service.AutoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.ElementKind.INTERFACE;


@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

    private Types mTypesUtil;
    private Elements mElementsUtil;
    private Filer mFiler;
    private Messager mMessager;
    private GenerateCodeTool generateCodeTool;

    private HashMap<String,FactoryGroupAnnotationClass> factoryGroupAnnotationClassHashMap = new HashMap<>();


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<String>();
        annotations.add(FactoryAnnotation.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mTypesUtil = processingEnvironment.getTypeUtils();
        mElementsUtil = processingEnvironment.getElementUtils();
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        generateCodeTool= new GenerateCodeTool(mElementsUtil,mMessager,mFiler);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            //log(null,"process start");
            if(CollectionUtils.isEmpty(roundEnvironment.getElementsAnnotatedWith(FactoryAnnotation.class))){
                return true;
            }
            //log(null,"process start 1");

            for (Element element : roundEnvironment.getElementsAnnotatedWith(FactoryAnnotation.class)) {
                //log(null,"process start  2");
                if(!validAnnotationCheck(element,element.getAnnotation(FactoryAnnotation.class))){
                    return true;
                }
                //log(null,"process start  3");
                if(!validClassCheck(element)){
                    return true;
                }
                TypeElement typeElement = (TypeElement)element;
                FactoryAnnotationClass factoryAnnotationClass = new FactoryAnnotationClass(mElementsUtil,typeElement);
                //log(element,"package name:"+factoryAnnotationClass.getPackageName());



                FactoryGroupAnnotationClass factoryGroupAnnotationClass = factoryGroupAnnotationClassHashMap.get(
                        factoryAnnotationClass.getQualifiedSuperClassName()
                );
                if (factoryGroupAnnotationClass == null) {
                    factoryGroupAnnotationClass = new FactoryGroupAnnotationClass(mFiler
                    ,factoryAnnotationClass.getQualifiedSuperClassName());
                }

                factoryGroupAnnotationClass.insert(factoryAnnotationClass.getRoute(), factoryAnnotationClass);
                factoryGroupAnnotationClassHashMap.put(factoryAnnotationClass.getQualifiedSuperClassName()
                        , factoryGroupAnnotationClass);



            }
            //log(null,"start generate code");
            for (FactoryGroupAnnotationClass factoryGroupAnnotationClass : factoryGroupAnnotationClassHashMap.values()) {


                generateCodeTool.generateCode(factoryGroupAnnotationClass.getFactoryAnnotationClassHashMap());
                //generateCodeTool.generateStoreImplCodeCache(factoryGroupAnnotationClass.getFactoryAnnotationClassHashMap());
            }
            factoryGroupAnnotationClassHashMap.clear();
            //log(null,"end generate code");
        }catch (Exception e){
            error(null," process error");
        }

        return true;
    }
    private boolean validAnnotationCheck(Element element,FactoryAnnotation factoryAnnotation){



        if(StringUtils.isEmpty(factoryAnnotation.route())){
            error(element, "class %s annotation route is empty ", element.getSimpleName());
            return false;
        }

        return true;
    }
    private boolean validClassCheck(Element rootElement){
        if(rootElement.getKind()!= CLASS){
            error(rootElement, "%s is not class ", rootElement.getSimpleName());
            return false;
        }
        if (!rootElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(rootElement, "class %s is not public ", rootElement.getSimpleName());
            return false;
        }
        boolean construct = false;
        for(Element element:rootElement.getEnclosedElements()){
            if(element.getKind() == CONSTRUCTOR){

                ExecutableElement executableElement = (ExecutableElement)element;


                if(!executableElement.getModifiers().contains(Modifier.PUBLIC)){
                    continue;
                }
                if(executableElement.getParameters()==null
                    ||executableElement.getParameters().size() == 0){
                    construct = true;
                    break;
                }

            }

        }
        if(!construct){
            error(rootElement, "class %s doesn't have public non-parameter constructor",
                    rootElement.getSimpleName());
            return false;
        }

        boolean findValidParentInterface = false;


        FactoryAnnotation factoryAnnotation = rootElement.getAnnotation(FactoryAnnotation.class);
        Class<?> type ;
        String qualifiedSuperClassName;

        TypeMirror typeMirror;
        try{
            type = factoryAnnotation.type();
            qualifiedSuperClassName = type.getCanonicalName();

        }catch(MirroredTypeException e) {
            typeMirror = e.getTypeMirror();
            DeclaredType classTypeMirror = (DeclaredType) e.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();


        }



        TypeElement superClassTypeElement = mElementsUtil.getTypeElement(qualifiedSuperClassName);
        if(superClassTypeElement.getKind() != INTERFACE){
            error(rootElement, "class %s annotation type %s is not interface",
                    rootElement.getSimpleName(),superClassTypeElement.getSimpleName());
            return false;
        }
        TypeElement rootTypeElement = (TypeElement) rootElement;


        for(TypeMirror superTypeMirror:rootTypeElement.getInterfaces()){

            if(superTypeMirror.toString().equals(qualifiedSuperClassName)){
                findValidParentInterface = true;
                break;
            }

        }

        TypeElement superElement = (TypeElement) rootElement;
        while(true) {
            TypeMirror superClassType = superElement.getSuperclass();

            if(superClassType.getKind() == TypeKind.NONE){
                break;
            }

            for(TypeMirror superTypeMirror:superElement.getInterfaces()){

                if(superTypeMirror.toString().equals(qualifiedSuperClassName)){
                    findValidParentInterface = true;
                    break;
                }

            }
            if(findValidParentInterface){
                break;
            }
            if(superClassType.toString().equals(qualifiedSuperClassName)){
                findValidParentInterface = true;
                break;
            }
            superElement = (TypeElement) mTypesUtil.asElement(superClassType);
        }

        if(!findValidParentInterface){
            error(rootElement, "class %s does not implement "+qualifiedSuperClassName,
                    rootElement.getSimpleName());
            return false;
        }

        return true;

    }

    private void error(Element e, String msg, Object... args) {
        mMessager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    private void log(Element e,String msg,Object... args){
        mMessager.printMessage(Diagnostic.Kind.NOTE
        ,String.format(msg,args)
        );
    }
}
