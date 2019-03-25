package wang.jason.lib;



import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.List;

public class FactoryAnnotationClass {


    private TypeElement typeElement;
    private String qualifiedSuperClassName;
    private String simpleTypeName;
    private String route;
    private Class<?> type;
    private TypeMirror typeMirror;
    private String packageName;
    private Elements elementUtils;



    public FactoryAnnotationClass(Elements elementUtil, TypeElement typeElement) {

        this.typeElement = typeElement;
        this.elementUtils = elementUtil;
        FactoryAnnotation annotation = typeElement.getAnnotation(FactoryAnnotation.class);
        //String route = null;
        //Class<?> type = null;
        try {
            route = annotation.route();

            if(route.equals("")){
                throw new IllegalArgumentException(
                        String.format("route() in @%s for class %s is null or empty! that's not allowed",
                                FactoryAnnotation.class.getSimpleName(), typeElement.getQualifiedName().toString()));
            }

            type = annotation.type();
            qualifiedSuperClassName = type.getCanonicalName();
            simpleTypeName = type.getSimpleName();




        }catch(MirroredTypeException e) {
            typeMirror = e.getTypeMirror();
            DeclaredType classTypeMirror = (DeclaredType) e.getTypeMirror();

            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();
            simpleTypeName = classTypeElement.getSimpleName().toString();

        }
        try {
            TypeElement superClassName = elementUtils.getTypeElement(qualifiedSuperClassName);

            PackageElement pkg = elementUtils.getPackageOf(superClassName);
            packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName().toString();

            /*if(typeElement.getEnclosedElements()!=null&&typeElement.getEnclosedElements().size()>0) {
                for (Element element : typeElement.getEnclosedElements()) {

                    if(element.getKind()==ElementKind.PACKAGE){
                        PackageElement packageElement = (PackageElement)element;

                        packageName = packageElement.getQualifiedName().toString();
                        break;
                    }

                }
            }*/
        }catch (Exception e){

        }

    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public String getQualifiedSuperClassName() {
        return qualifiedSuperClassName;
    }

    public String getSimpleTypeName() {
        return simpleTypeName;
    }

    public String getRoute() {
        return route;
    }

    public Class<?> getType() {
        return type;
    }

    public String getPackageName() {
        return packageName;
    }

    public TypeMirror getTypeMirror() {
        return typeMirror;
    }
}
