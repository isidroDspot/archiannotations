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

import com.helger.jcodemodel.IJAssignmentTarget;
import com.helger.jcodemodel.JBlock;
import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.handler.MethodInjectionHandler;
import org.androidannotations.helper.InjectHelper;
import org.androidannotations.holder.EBeanHolder;
import pl.com.dspot.archiannotations.annotation.RootView;

import javax.lang.model.element.Element;

public class RootViewHandler extends BaseAnnotationHandler<EBeanHolder>implements MethodInjectionHandler<EBeanHolder> {

    private final InjectHelper<EBeanHolder> injectHelper;

    public RootViewHandler(AndroidAnnotationsEnvironment environment) {
        super(RootView.class, environment);
        injectHelper = new InjectHelper<>(validatorHelper, this);
    }

    @Override
    public void validate(Element element, ElementValidation validation) {
        injectHelper.validate(RootView.class, element, validation);
        if (!validation.isValid()) {
            return;
        }

        validatorHelper.isNotPrivate(element, validation);
    }

    @Override
    public void process(Element element, EBeanHolder holder) {
        injectHelper.process(element, holder);
    }

    @Override
    public JBlock getInvocationBlock(EBeanHolder holder) {
        return holder.getInitBodyInjectionBlock();
    }

    @Override
    public void assignValue(JBlock targetBlock, IJAssignmentTarget fieldRef, EBeanHolder holder, Element element, Element param) {
//        TypeMirror elementType = param.asType();
//        String typeQualifiedName = elementType.toString();
//
//        //Could be @EViewModel or @EViewPresenter
//        JFieldVar rootViewField = null;
//        if (adiHelper.hasAnnotation(holder.getAnnotatedElement(), EViewModel.class)) {
//            EViewModelHolder viewModelHolder = holder.getPluginHolder(new EViewModelHolder(holder));
//            rootViewField = viewModelHolder.getRootViewField();
//
//        } else if (adiHelper.hasAnnotation(holder.getAnnotatedElement(), EViewPresenter.class)) {
//            ViewPresenterHolder viewPresenterHolder = holder.getPluginHolder(new ViewPresenterHolder(holder));
//            rootViewField = viewPresenterHolder.getRootViewField();
//        }
//
//        if (rootViewField == null) return;
//
//        if (CanonicalNameConstants.OBJECT.equals(typeQualifiedName)) {
//            targetBlock.add(fieldRef.assign(rootViewField));
//        } else {
//
//            AbstractJClass extendingRootViewClass = getEnvironment().getJClass(typeQualifiedName);
//
//            JConditional cond = getInvocationBlock(element, holder)._if(rootViewField._instanceof(extendingRootViewClass));
//            cond._then().add(fieldRef.assign(cast(extendingRootViewClass, rootViewField)));
//
//            JInvocation warningInvoke = getClasses().LOG.staticInvoke("w");
//            warningInvoke.arg(logTagForClassHolder(holder));
//            warningInvoke.arg(lit("Due to the class ").plus(holder.getContextRef().invoke("getClass").invoke("getSimpleName"))
//                    .plus(lit(", the @RootView " + extendingRootViewClass.name() + " won't be populated")));
//            cond._else().add(warningInvoke);
//        }
    }

    @Override
    public void validateEnclosingElement(Element element, ElementValidation valid) {
        validatorHelper.enclosingElementHasEBeanAnnotation(element, valid);
    }
}

