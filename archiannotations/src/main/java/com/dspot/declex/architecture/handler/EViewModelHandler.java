package com.dspot.declex.architecture.handler;

import com.dspot.declex.architecture.annotation.ViewModel;
import com.dspot.declex.architecture.annotation.EViewModel;
import com.dspot.declex.architecture.holder.ViewModelHolder;
import com.dspot.declex.helper.ActionHelper;
import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.handler.BaseGeneratingAnnotationHandler;
import org.androidannotations.holder.EBeanHolder;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Map;

import static com.dspot.declex.api.util.FormatsUtils.fieldToGetter;
import static com.dspot.declex.architecture.ArchCanonicalNameConstants.VIEW_MODEL;
import static com.dspot.declex.util.TypeUtils.isSubtype;
import static com.helger.jcodemodel.JExpr.*;

public class EViewModelHandler extends BaseGeneratingAnnotationHandler<EBeanHolder> {

    public EViewModelHandler(AndroidAnnotationsEnvironment environment) {
        super(EViewModel.class, environment);
    }

    @Override
    public void validate(Element element, ElementValidation valid) {
        validatorHelper.extendsType(element, VIEW_MODEL, valid);

        //TODO Don't permit certain injections types in ViewModels
    }

    @Override
    public void process(Element element, EBeanHolder holder) {

        ViewModelHolder viewModelHolder = holder.getPluginHolder(new ViewModelHolder(holder));

        viewModelHolder.getConstructorMethod();

        viewModelHolder.getRebindMethod();

        //Clear the context variable after the injections, to avoid that the class hold references to the Context
        holder.getInitBodyAfterInjectionBlock().assign(holder.getContextField(), _null());

        //Clear the rootView variable after the injection
        holder.getInitBodyAfterInjectionBlock().assign(viewModelHolder.getRootViewField(), _null());

        //Search for all the injections, and set them to null in the "onCleared", so no reference is kept
        //after the ViewModel was marked as not needed
        for (Element elem : element.getEnclosedElements()) {

            boolean markedToRemove = false;

            if (elem.getAnnotation(Bean.class) != null) {
                markedToRemove = true;
            }

            if (elem.getAnnotation(ViewModel.class) != null) {
                markedToRemove = true;
            }

            if (markedToRemove) {
                viewModelHolder.getOnClearedMethodFinalBlock().assign(_this().ref(elem.getSimpleName().toString()), _null());
            }

        }

    }

    @Override
    public EBeanHolder createGeneratedClassHolder(AndroidAnnotationsEnvironment environment, TypeElement annotatedElement) throws Exception {
        return null;
    }

}
