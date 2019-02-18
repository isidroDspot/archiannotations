/**
 * Copyright (C) 2016-2019 DSpot Sp. z o.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.com.dspot.archiannotations.handler;

import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.holder.EBeanHolder;
import pl.com.dspot.archiannotations.annotation.EViewModel;
import pl.com.dspot.archiannotations.annotation.RootView;
import pl.com.dspot.archiannotations.annotation.ViewModel;
import pl.com.dspot.archiannotations.annotation.ViewPresenter;
import pl.com.dspot.archiannotations.holder.EViewModelHolder;

import javax.lang.model.element.Element;

import static com.dspot.declex.action.ActionsProcessor.hasAction;
import static com.helger.jcodemodel.JExpr._null;
import static com.helger.jcodemodel.JExpr._this;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.LIFECYCLE_OBSERVER;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.VIEW_MODEL;
import static pl.com.dspot.archiannotations.util.ElementUtils.isSubtype;

public class EViewModelHandler extends BaseAnnotationHandler<EBeanHolder> {

    public EViewModelHandler(AndroidAnnotationsEnvironment environment) {
        super(EViewModel.class.getCanonicalName(), environment);
    }

    @Override
    public void validate(Element element, ElementValidation valid) {

        validatorHelper.extendsType(element, VIEW_MODEL, valid);
        validatorHelper.typeHasAnnotation(EBean.class, element, valid);

        //TODO Do validations about data which can be injected in ViewModels

        for (Element elem : element.getEnclosedElements()) {

            if (hasAction(elem, getEnvironment())) {
                valid.addError("Actions are not permitted in EViewModel annotated classes");
            }

        }
    }

    @Override
    public void process(Element element, EBeanHolder holder) {

        EViewModelHolder viewModelHolder = holder.getPluginHolder(new EViewModelHolder(holder));

        viewModelHolder.getEmptyConstructorMethod();
        viewModelHolder.getBindToMethod();
        viewModelHolder.getOnClearedMethod();

        //Clear the context variable after the injections, to avoid that the class hold references to the Context
        holder.getInitBodyAfterInjectionBlock().assign(holder.getContextField(), _null());

        //Clear the rootView variable after the injections, to avoid that the class hold references to a view/context directly
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

            if (elem.getAnnotation(ViewPresenter.class) != null) {
                markedToRemove = true;
            }

            if (elem.getAnnotation(RootContext.class) != null) {
                markedToRemove = true;
            }

            if (elem.getAnnotation(RootView.class) != null) {
                markedToRemove = true;
            }

            if (markedToRemove) {
                viewModelHolder.getOnClearedMethodFinalBlock().assign(_this().ref(elem.getSimpleName().toString()), _null());
            }

        }

    }
}
