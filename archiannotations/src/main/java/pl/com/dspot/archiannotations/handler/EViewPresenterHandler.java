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
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.holder.EBeanHolder;
import pl.com.dspot.archiannotations.annotation.EViewPresenter;
import pl.com.dspot.archiannotations.annotation.PresenterMethod;
import pl.com.dspot.archiannotations.holder.ViewPresenterHolder;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import java.util.Map;

public class EViewPresenterHandler extends BaseAnnotationHandler<EBeanHolder> {

    public EViewPresenterHandler(AndroidAnnotationsEnvironment environment) {
        super(EViewPresenter.class, environment);
    }

    @Override
    public void getDependencies(Element element, Map<Element, Object> dependencies) {

        //EViewPresenter works different when it is in a view or activity
        if (adiHelper.hasAnnotation(element, EFragment.class) || adiHelper.hasAnnotation(element, EActivity.class)) {
            return;
        }

        dependencies.put(element, EBean.class);

        EViewPresenter viewPresenter = element.getAnnotation(EViewPresenter.class);

        if (viewPresenter.markPresenterMethods()) {

            //Mark the public methods with @PresenterMethod
            for (Element elem : ((TypeElement) element).getEnclosedElements()) {

                if (elem.getKind() != ElementKind.METHOD) continue;
                if (elem.getModifiers().contains(Modifier.STATIC)) continue;
                if (((ExecutableElement)elem).getReturnType().getKind() != TypeKind.VOID) continue; //TODO notify to the user about this method not being "protected"?

                if (elem.getModifiers().contains(Modifier.PUBLIC)) {
                    dependencies.put(elem, PresenterMethod.class);
                }

            }

        }

    }

    @Override
    public void validate(Element element, ElementValidation valid) {}

    @Override
    public void process(Element element, EBeanHolder holder) {

        ViewPresenterHolder viewPresenterHolder = holder.getPluginHolder(new ViewPresenterHolder(holder));

        viewPresenterHolder.getConstructorMethod();

        viewPresenterHolder.getPresenterLiveDataField();

        viewPresenterHolder.getRebindMethod();

    }

}
