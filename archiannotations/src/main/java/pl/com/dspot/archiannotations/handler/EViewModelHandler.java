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
import org.androidannotations.annotations.EBean;
import org.androidannotations.handler.BaseGeneratingAnnotationHandler;
import org.androidannotations.helper.ModelConstants;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import pl.com.dspot.archiannotations.annotation.EViewModel;
import pl.com.dspot.archiannotations.holder.EViewModelHolder;

import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.VIEW_MODEL;

public class EViewModelHandler extends BaseGeneratingAnnotationHandler<EViewModelHolder> {

    public EViewModelHandler(AndroidAnnotationsEnvironment environment) {
        super(EViewModel.class, environment);
        ModelConstants.VALID_ENHANCED_COMPONENT_ANNOTATIONS.add(EViewModel.class);
    }

    @Override
    public EViewModelHolder createGeneratedClassHolder(AndroidAnnotationsEnvironment environment, TypeElement annotatedComponent) throws Exception {
        return new EViewModelHolder(environment, annotatedComponent);
    }

    @Override
    public void validate(Element element, ElementValidation valid) {
        super.validate(element, valid);

        validatorHelper.extendsType(element, VIEW_MODEL, valid);
    }

    @Override
    public void process(Element element, EViewModelHolder holder) {

    }

}
