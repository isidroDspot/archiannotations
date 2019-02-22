package pl.com.dspot.archiannotations.helper.override;

import org.androidannotations.ElementValidation;
import org.androidannotations.helper.IdAnnotationHelper;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;

import static java.util.Arrays.asList;
import static org.androidannotations.helper.CanonicalNameConstants.FRAGMENT_ACTIVITY;
import static org.androidannotations.helper.CanonicalNameConstants.SUPPORT_V4_FRAGMENT;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.LIFECYCLE_OWNER;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.VIEW_MODEL;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.VIEW_MODEL_PROVIDERS;

public class ValidatorHelper extends org.androidannotations.helper.IdValidatorHelper {

    public ValidatorHelper(IdAnnotationHelper idAnnotationHelper) {
        super(idAnnotationHelper);
    }

    public void isAbstractOrHasOneParamConstructor(String paramClassName, Element element, ElementValidation valid) {

        List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());

        if (!annotationHelper.isAbstract(element)) {
            if (constructors.size() == 1) {
                ExecutableElement constructor = constructors.get(0);

                if (!annotationHelper.isPrivate(constructor)) {
                    if (constructor.getParameters().size() > 1) {
                        valid.addError("%s annotated element should have a constructor with one parameter of type " + paramClassName);
                    } else if (constructor.getParameters().size() == 1) {
                        VariableElement parameter = constructor.getParameters().get(0);
                        if (!parameter.asType().toString().equals(paramClassName)) {
                            valid.addError("%s annotated element should have a constructor with one parameter of type " + paramClassName);
                        }
                    } else {
                        valid.addError("%s annotated element should have a constructor with one parameter of type " + paramClassName);
                    }
                } else {
                    valid.addError("%s annotated element should not have a private constructor");
                }
            } else {
                valid.addError("%s annotated element should have only one constructor");
            }
        }

    }

    public void hasLifecycleExtensionsOnDependencies(ElementValidation valid) {
        Elements elementUtils = annotationHelper.getElementUtils();
        if (elementUtils.getTypeElement(VIEW_MODEL_PROVIDERS) == null || elementUtils.getTypeElement(VIEW_MODEL) == null) {
            valid.addError("To use ArchiAnnotations, you MUST include the android.arch.lifecycle:extensions on your dependencies");
        }
    }

    public void extendsFragmentActivityOrSupportFragmentAndImplementsLifecycleOwner(Element element, ElementValidation validation) {
        extendsOneOfTypes(element, asList(FRAGMENT_ACTIVITY, SUPPORT_V4_FRAGMENT), validation);
    }

}
