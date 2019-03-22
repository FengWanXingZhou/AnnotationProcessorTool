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



/*import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;*/
import java.util.LinkedHashSet;
import java.util.Set;
@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {

    private Types mTypes;
    private Elements mElements;
    private Filer mFiler;
    private Messager mMessager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<String>();
        annotations.add(PsiTableHeaderAnnotation.class.getCanonicalName());
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
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        for (Element element : roundEnvironment.getElementsAnnotatedWith(PsiTableHeaderAnnotation.class)){

            if(element.getKind()!= ElementKind.FIELD){
                error(element,"only field can be annotated");
                return true;
            }

            VariableElement typeElement = (VariableElement)element;

            if(!typeElement.getModifiers().contains(Modifier.PUBLIC)) {
                error(element,"field %s is not public ",typeElement.getSimpleName());
                return true;
            }

            PsiTableHeaderAnnotation annotation = typeElement.getAnnotation(PsiTableHeaderAnnotation.class);
            int codeFormat = -1;
            try {
                codeFormat = annotation.codeForamt();
            }catch(Exception e) {


            }
            if(codeFormat!=PsiTableHeaderAnnotation.DECIMALISM
                    &&codeFormat!=PsiTableHeaderAnnotation.HEXADECIMAL){
                error(element,"field %s code format is not standard ",typeElement.getSimpleName());
                return true;
            }
            generateCode(element);

        }

        return false;
    }

    private void generateCode(Element element){
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                .build();
        try {
            javaFile.writeTo(mFiler);
        }catch (Exception e){
            error(element," generate code error");
        }

    }

    private void error(Element e, String msg, Object... args) {
        mMessager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }


}
