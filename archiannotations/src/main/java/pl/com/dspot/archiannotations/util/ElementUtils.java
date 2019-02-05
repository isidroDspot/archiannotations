package pl.com.dspot.archiannotations.util;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public class ElementUtils {

    public static boolean isSubTypeRecursive(TypeMirror potentialSubtype, TypeMirror potentialSupertype, ProcessingEnvironment processingEnv) {
        String subType = potentialSubtype.toString();
        String superType = potentialSupertype.toString();

        int indexGenericSubType = subType.indexOf('<');
        int indexGenericSuperType = superType.indexOf('<');
        if (indexGenericSubType != -1) subType = subType.substring(0, indexGenericSubType);
        if (indexGenericSuperType != -1) superType = superType.substring(0, indexGenericSuperType);

        if (subType.equals(superType)) {
            return true;
        }

        List<? extends TypeMirror> superTypes = processingEnv.getTypeUtils().directSupertypes(potentialSubtype);
        for (TypeMirror type : superTypes) {
            if (isSubTypeRecursive(type, potentialSupertype, processingEnv)) return true;
        }

        return false;
    }

    public static boolean isSubtype(TypeMirror potentialSubtype, TypeMirror potentialSupertype, ProcessingEnvironment processingEnv) {

        //This is because isSubtype is failing with generic classes in gradle
        return isSubTypeRecursive(potentialSubtype, potentialSupertype, processingEnv);
    }

    public static boolean isSubtype(TypeMirror t1, String t2, ProcessingEnvironment processingEnv) {
        if (t2.contains("<")) t2 = t2.substring(0, t2.indexOf('<'));

        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(t2);
        if (typeElement != null) {
            TypeMirror expectedType = typeElement.asType();
            return isSubtype(t1, expectedType, processingEnv);
        }

        return false;
    }

    public static boolean isSubtype(TypeElement t1, TypeElement t2, ProcessingEnvironment processingEnv) {
        return isSubtype(t1.asType(), t2.asType(), processingEnv);
    }

    public static boolean isSubtype(Element t1, String t2, ProcessingEnvironment processingEnv) {
        TypeMirror elementType = t1.asType();
        if (t2.contains("<")) t2 = t2.substring(0, t2.indexOf('<'));

        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(t2);
        if (typeElement != null) {
            TypeMirror expectedType = typeElement.asType();
            return isSubtype(elementType, expectedType, processingEnv);
        }

        return false;
    }

    public static boolean isSubtype(String t1, String t2, ProcessingEnvironment processingEnv) {
        if (t1.equals(t2)) return true;

        if (t1.contains("<")) t1 = t1.substring(0, t1.indexOf('<'));

        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(t1);
        if (typeElement != null) {
            return isSubtype(typeElement, t2, processingEnv);
        }

        return false;
    }

}
