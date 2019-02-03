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
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.holder.EComponentHolder;
import pl.com.dspot.archiannotations.annotation.EBinder;
import pl.com.dspot.archiannotations.annotation.Observer;
import pl.com.dspot.archiannotations.holder.ObserverHolder;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static org.androidannotations.helper.CanonicalNameConstants.VIEW;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.LIFECYCLE_OWNER;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.VIEW_MODEL;
import static pl.com.dspot.archiannotations.util.ElementUtils.isSubtype;

public class ObserverHandler extends BaseAnnotationHandler<EComponentHolder> {

    public ObserverHandler(AndroidAnnotationsEnvironment environment) {
        super(Observer.class, environment);
    }

    @Override
    public void validate(Element element, ElementValidation validation) {

        if (element.getKind() == ElementKind.METHOD) {
            validatorHelper.param.anyType().validate((ExecutableElement) element, validation);
            validatorHelper.returnTypeIsVoidOrBoolean((ExecutableElement) element, validation);
        }

        Observer observer = element.getAnnotation(Observer.class);
        if (!observer.observeForever()) {

            Element rootElement = element.getEnclosingElement();
            if (!isSubtype(rootElement, LIFECYCLE_OWNER, getProcessingEnvironment())
                    && !isSubtype(rootElement, VIEW_MODEL, getProcessingEnvironment())) {
                validation.addError("@Observer can be placed only inside classes which implement LifeCycleOwner or annotated with @EViewModel, or should be marked with \"observeForever\"."
                                    + "\nIf you are aware of the consequences of observing another class forever, you can manually set \"observeForever = true\"");
            }

        }

        validatorHelper.enclosingElementHasAnnotation(EBinder.class, element, validation);

    }

    @Override
    public void process(Element element, EComponentHolder holder) {

        ObserverHolder observerHolder = holder.getPluginHolder(new ObserverHolder(holder));

        TypeMirror typeMirror = getProcessingEnvironment().getTypeUtils().erasure(element.asType());
        TypeElement viewTypeElement = annotationHelper.typeElementFromQualifiedName(typeMirror.toString());
        if (viewTypeElement == null) {
            return;
        }

        //Don't generate getters for views
        if (isSubtype(viewTypeElement, VIEW, getProcessingEnvironment())) return;

        observerHolder.getObserverMethodFor(element);

    }

}
