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

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJAssignmentTarget;
import com.helger.jcodemodel.IJStatement;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;
import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.handler.MethodInjectionHandler;
import org.androidannotations.helper.CanonicalNameConstants;
import org.androidannotations.helper.InjectHelper;
import org.androidannotations.holder.EBeanHolder;
import pl.com.dspot.archiannotations.annotation.EViewModel;
import pl.com.dspot.archiannotations.annotation.RootView;
import pl.com.dspot.archiannotations.holder.EViewModelHolder;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import static com.helger.jcodemodel.JExpr._null;
import static com.helger.jcodemodel.JExpr.cast;
import static com.helger.jcodemodel.JExpr.lit;
import static org.androidannotations.helper.LogHelper.logTagForClassHolder;

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
    public JBlock getInvocationBlock(Element element, EBeanHolder holder) {
        return holder.getInitBodyInjectionBlock();
    }

    @Override
    public void assignValue(JBlock targetBlock, IJAssignmentTarget fieldRef, EBeanHolder holder, Element element, Element param) {
        TypeMirror elementType = param.asType();
        String typeQualifiedName = elementType.toString();

        //Could be @EViewModel or @EViewPresenter
        EViewModelHolder viewModelHolder = holder.getPluginHolder(new EViewModelHolder(holder));
        JFieldVar rootViewField = viewModelHolder.getRootViewField();

        JMethod dependencyProviderMethod = holder.createProviderMethod(getJClass(typeQualifiedName));
        if (dependencyProviderMethod != null) {
            JVar contextParam = dependencyProviderMethod.param(getClasses().CONTEXT, "context");
            JVar rootViewParam = dependencyProviderMethod.param(getClasses().OBJECT, "rootView");

            JBlock dependencyProviderBody = dependencyProviderMethod.body();
            
            if (CanonicalNameConstants.OBJECT.equals(typeQualifiedName)) {
                dependencyProviderBody._return(rootViewParam);
            } else {

                AbstractJClass extendingRootViewClass = getEnvironment().getJClass(typeQualifiedName);

                JConditional cond = dependencyProviderBody._if(rootViewParam._instanceof(extendingRootViewClass));
                cond._then()._return(cast(extendingRootViewClass, rootViewParam));

                JInvocation warningInvoke = getClasses().LOG.staticInvoke("w");
                warningInvoke.arg(logTagForClassHolder(holder));
                warningInvoke.arg(lit("Due to the class ").plus(contextParam.invoke("getClass").invoke("getSimpleName"))
                        .plus(lit(", the @RootView " + extendingRootViewClass.name() + " won't be populated")));
                cond._else().add(warningInvoke);

                dependencyProviderBody._return(_null());
            }
        }

        IJStatement assignment = fieldRef.assign(holder.getDependencyProvider().invoke(holder.getProviderMethod(getJClass(typeQualifiedName))).arg(holder.getContextRef()).arg(rootViewField));
        targetBlock.add(assignment);
    }

    @Override
    public void validateEnclosingElement(Element element, ElementValidation valid) {
        validatorHelper.enclosingElementHasAnnotation(EViewModel.class, element, valid);
    }
}

