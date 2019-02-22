/**
 * Copyright (C) 2018-2019 DSpot Sp. z o.o
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
import org.androidannotations.handler.BaseGeneratingAnnotationHandler;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import pl.com.dspot.archiannotations.annotation.EViewModel;
import pl.com.dspot.archiannotations.helper.override.ValidatorHelper;
import pl.com.dspot.archiannotations.holder.EViewModelHolder;

import static com.helger.jcodemodel.JExpr._null;
import static com.helger.jcodemodel.JExpr._this;
import static org.androidannotations.helper.CanonicalNameConstants.APPLICATION;
import static org.androidannotations.helper.ModelConstants.addEnhancedComponentAnnotation;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.ANDROID_VIEW_MODEL;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.VIEW_MODEL;
import static pl.com.dspot.archiannotations.util.ElementUtils.isSubtype;

public class EViewModelHandler extends BaseGeneratingAnnotationHandler<EViewModelHolder> {

    private ValidatorHelper validatorHelper;

    public EViewModelHandler(AndroidAnnotationsEnvironment environment) {
        super(EViewModel.class, environment);
        validatorHelper = new ValidatorHelper(annotationHelper);
        addEnhancedComponentAnnotation(EViewModel.class);
    }

    @Override
    public EViewModelHolder createGeneratedClassHolder(AndroidAnnotationsEnvironment environment, TypeElement annotatedComponent) throws Exception {
        return new EViewModelHolder(environment, annotatedComponent);
    }

    @Override
    public void validate(Element element, ElementValidation validation) {
        super.validate(element, validation);

        validatorHelper.extendsType(element, VIEW_MODEL, validation);

        if (validation.isValid()) {
            if (isSubtype(element, ANDROID_VIEW_MODEL, getProcessingEnvironment())) {
                validatorHelper.isAbstractOrHasOneParamConstructor(APPLICATION, element, validation);
            } else {
                validatorHelper.isAbstractOrHasEmptyConstructor(element, validation);
            }
        }

        validatorHelper.hasLifecycleExtensionsOnDependencies(validation);
    }

    @Override
    public void process(Element element, EViewModelHolder holder) {

        EViewModel viewModelAnnotation = element.getAnnotation(EViewModel.class);
        holder.createFactoryMethod(viewModelAnnotation.scope());
        holder.getOnClearedMethod();

        List<VariableElement> fields = ElementFilter.fieldsIn(element.getEnclosedElements());

        //Clear all the not primitive fields. This will ensure to Clean up all the fields injections as well
        for (Element field : fields) {
            holder.getOnClearedMethodFinalBlock().assign(_this().ref(field.getSimpleName().toString()), _null());
        }

    }

}
