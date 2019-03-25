package wang.jason.lib;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;



@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

    private Types mTypes;
    private Elements mElements;
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
        mTypes = processingEnvironment.getTypeUtils();
        mElements = processingEnvironment.getElementUtils();
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        generateCodeTool= new GenerateCodeTool(mElements,mMessager,mFiler);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            log(null,"process start");
            for (Element element : roundEnvironment.getElementsAnnotatedWith(FactoryAnnotation.class)) {

                if (element.getKind() != ElementKind.CLASS) {
                    error(element, "only field can be annotated");
                    return true;
                }

                TypeElement typeElement = (TypeElement) element;

                if (!typeElement.getModifiers().contains(Modifier.PUBLIC)) {
                    error(element, "class %s is not public ", typeElement.getSimpleName());
                    return true;
                }

                FactoryAnnotationClass factoryAnnotationClass = new FactoryAnnotationClass(mElements,typeElement);
                log(element,"package name:"+factoryAnnotationClass.getPackageName());

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

                //generateCode(element);

            }
            log(null,"start generate code");
            for (FactoryGroupAnnotationClass factoryGroupAnnotationClass : factoryGroupAnnotationClassHashMap.values()) {

                //factoryGroupAnnotationClass.generateCode(mElements,mMessager);
                generateCodeTool.generateCode(factoryGroupAnnotationClass.getFactoryAnnotationClassHashMap());
                generateCodeTool.generateStoreImplCodeCache(factoryGroupAnnotationClass.getFactoryAnnotationClassHashMap());
            }
            factoryGroupAnnotationClassHashMap.clear();
            log(null,"end generate code");
        }catch (Exception e){
            error(null," process error");
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
