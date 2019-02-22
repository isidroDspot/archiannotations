package pl.com.dspot.archiannotations.helper.override;

import com.helger.jcodemodel.AbstractJClass;
import org.androidannotations.AndroidAnnotationsEnvironment;
import pl.com.dspot.archiannotations.helper.CompilationTreeHelper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class APTCodeModelHelper extends org.androidannotations.helper.APTCodeModelHelper {

    private AndroidAnnotationsEnvironment environment;

    private final CompilationTreeHelper compilationTreeHelper;

    public APTCodeModelHelper(AndroidAnnotationsEnvironment environment) {
        super(environment);
        this.environment = environment;
        this.compilationTreeHelper = new CompilationTreeHelper(environment);
    }

    public AbstractJClass elementTypeToJClass(Element element) {
        return elementTypeToJClass(element, false);
    }

    public AbstractJClass elementTypeToJClass(Element element, boolean useFirstTypeArgument) {

        TypeMirror type = element instanceof ExecutableElement ? ((ExecutableElement)element).getReturnType()
                : element.asType();

        if (useFirstTypeArgument && type instanceof DeclaredType) {
            if (((DeclaredType)type).getTypeArguments().size() > 0) {
                type = ((DeclaredType)type).getTypeArguments().get(0);
            }
        }

        return typeMirrorToJClass(type, element, Collections.<String, TypeMirror> emptyMap());
    }

    public String typeStringToClassName(String typeName, Element referenceElement) {
        return compilationTreeHelper.getClassNameFromCompilationUnitImports(typeName, referenceElement);
    }

    public AbstractJClass typeMirrorToJClass(TypeMirror type) {
        return typeMirrorToJClass(type, null, Collections.<String, TypeMirror> emptyMap());
    }

    public AbstractJClass typeMirrorToJClass(TypeMirror type, Element referenceElement) {
        return typeMirrorToJClass(type, referenceElement, Collections.<String, TypeMirror> emptyMap());
    }

    private AbstractJClass typeMirrorToJClass(TypeMirror type, Map<String, TypeMirror> substitute) {
        return typeMirrorToJClass(type, null, substitute);
    }

    private AbstractJClass typeMirrorToJClass(TypeMirror type, Element referenceElement, Map<String, TypeMirror> substitute) {
        if (type instanceof ErrorType) {
            return environment.getJClass(typeStringToClassName(type.toString(), referenceElement));
        } else if (type instanceof DeclaredType) {
            return typeMirrorToJClass((DeclaredType) type, referenceElement, substitute);
        } else if (type instanceof WildcardType) {
            return typeMirrorToJClass((WildcardType) type, referenceElement, substitute);
        } else if (type instanceof ArrayType) {
            return typeMirrorToJClass((ArrayType) type, referenceElement, substitute);
        } else {

            TypeMirror substituted = substitute.get(type.toString());
            if (substituted != null && type != substituted) {
                return typeMirrorToJClass(substituted, referenceElement, substitute);
            }

            if (type.getKind().isPrimitive() || type.getKind() == TypeKind.VOID || type.getKind() == TypeKind.TYPEVAR) {
                return environment.getJClass(type.toString());
            }

            return environment.getJClass(typeStringToClassName(type.toString(), referenceElement));

        }
    }

    private AbstractJClass typeMirrorToJClass(DeclaredType declaredType, Element referenceElement, Map<String, TypeMirror> substitute) {
        String declaredTypeName = declaredType.asElement().toString();

        AbstractJClass declaredClass = environment.getJClass(declaredTypeName);

        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

        List<AbstractJClass> typeArgumentJClasses = new ArrayList<>();
        for (TypeMirror typeArgument : typeArguments) {
            typeArgumentJClasses.add(typeMirrorToJClass(typeArgument, referenceElement, substitute));
        }
        if (typeArgumentJClasses.size() > 0) {
            declaredClass = declaredClass.narrow(typeArgumentJClasses);
        }

        return declaredClass;
    }

    private AbstractJClass typeMirrorToJClass(WildcardType wildcardType, Element referenceElement, Map<String, TypeMirror> substitute) {
        TypeMirror bound = wildcardType.getExtendsBound();
        if (bound == null) {
            bound = wildcardType.getSuperBound();
            if (bound == null) {
                return environment.getClasses().OBJECT.wildcard();
            }
            return typeMirrorToJClass(bound, referenceElement, substitute).wildcardSuper();
        }

        TypeMirror extendsBound = wildcardType.getExtendsBound();

        if (extendsBound == null) {
            return environment.getClasses().OBJECT.wildcard();
        } else {
            return typeMirrorToJClass(extendsBound, referenceElement, substitute).wildcard();
        }
    }

    private AbstractJClass typeMirrorToJClass(ArrayType arrayType, Element referenceElement, Map<String, TypeMirror> substitute) {
        AbstractJClass refClass = typeMirrorToJClass(arrayType.getComponentType(), referenceElement, substitute);
        return refClass.array();
    }

}
