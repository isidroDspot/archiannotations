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
import com.helger.jcodemodel.JInvocation;

import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.handler.MethodInjectionHandler;
import org.androidannotations.helper.InjectHelper;
import org.androidannotations.holder.EComponentHolder;
import org.androidannotations.holder.EComponentWithViewSupportHolder;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import pl.com.dspot.archiannotations.annotation.EViewModel;
import pl.com.dspot.archiannotations.annotation.ViewModel;
import pl.com.dspot.archiannotations.helper.override.ValidatorHelper;
import pl.com.dspot.archiannotations.holder.EViewModelHolder;

import static com.helger.jcodemodel.JExpr._this;
import static com.helger.jcodemodel.JExpr.cast;
import static java.util.Arrays.asList;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.LIFECYCLE_OWNER;
import static pl.com.dspot.archiannotations.util.ElementUtils.rootElementOf;

public class ViewModelHandler extends BaseAnnotationHandler<EComponentHolder> implements MethodInjectionHandler<EComponentHolder> {

    private final InjectHelper<EComponentHolder> injectHelper;

    private ValidatorHelper validatorHelper;

    public ViewModelHandler(AndroidAnnotationsEnvironment environment) {
        super(ViewModel.class, environment);
        validatorHelper = new ValidatorHelper(annotationHelper);
        injectHelper = new InjectHelper<>(validatorHelper, this);
    }

    @Override
    public void validate(Element element, ElementValidation validation) {

        injectHelper.validate(ViewModel.class, element, validation);
        if (!validation.isValid()) {
            return;
        }

        validatorHelper.typeOrTargetValueHasAnnotation(EViewModel.class, element, validation);

        validatorHelper.isNotPrivate(element, validation);

        validatorHelper.hasLifecycleExtensionsOnDependencies(validation);
    }

    @Override
    public JBlock getInvocationBlock(EComponentHolder holder) {
        return holder.getInitBodyInjectionBlock();
    }

    @Override
    public void process(Element element, EComponentHolder holder) {
        injectHelper.process(element, holder);
    }

    @Override
    public void assignValue(JBlock targetBlock, IJAssignmentTarget fieldRef, EComponentHolder holder, Element element, Element param) {

        TypeMirror typeMirror = annotationHelper.extractAnnotationClassParameter(element);
        if (typeMirror == null) {
            typeMirror = param.asType();
            typeMirror = getProcessingEnvironment().getTypeUtils().erasure(typeMirror);
        }
        String typeQualifiedName = typeMirror.toString();
        AbstractJClass injectedClass = getJClass(annotationHelper.generatedClassQualifiedNameFromQualifiedName(typeQualifiedName));
        JInvocation viewModelInstance = injectedClass.staticInvoke(EViewModelHolder.GET_INSTANCE_METHOD_NAME).arg(holder.getContextRef());

        TypeElement rootElement = rootElementOf(element);
        if (rootElement.getAnnotation(EActivity.class) != null || rootElement.getAnnotation(EFragment.class) != null) {
            viewModelInstance.arg(_this());
        } else {
            viewModelInstance.arg(cast(getJClass(LIFECYCLE_OWNER), holder.getRootFragmentRef()));
        }

        IJStatement assignment = fieldRef.assign(viewModelInstance);
        targetBlock.add(assignment);

    }

    @Override
    public void validateEnclosingElement(Element element, ElementValidation valid) {
        List<Class<? extends Annotation>> validAnnotations = asList(EActivity.class, EFragment.class, EViewModel.class, EBean.class);
        validatorHelper.enclosingElementHasOneOfAnnotations(element, validAnnotations, valid);

        TypeElement rootElement = rootElementOf(element);
        if (rootElement.getAnnotation(EActivity.class) != null || rootElement.getAnnotation(EFragment.class) != null) {
            validatorHelper.extendsFragmentActivityOrSupportFragmentAndImplementsLifecycleOwner(rootElement, valid);
        }
    }

}
