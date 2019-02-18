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
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.IJStatement;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JInvocation;
import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.handler.MethodInjectionHandler;
import org.androidannotations.helper.InjectHelper;
import org.androidannotations.holder.EActivityHolder;
import org.androidannotations.holder.EBeanHolder;
import org.androidannotations.holder.EComponentWithViewSupportHolder;
import org.androidannotations.holder.EFragmentHolder;
import pl.com.dspot.archiannotations.annotation.EViewModel;
import pl.com.dspot.archiannotations.annotation.EViewPresenter;
import pl.com.dspot.archiannotations.annotation.ViewPresenter;
import pl.com.dspot.archiannotations.holder.EViewModelHolder;
import pl.com.dspot.archiannotations.holder.EViewPresenterHolder;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;

import static com.helger.jcodemodel.JExpr.*;
import static java.util.Arrays.asList;
import static org.androidannotations.helper.ModelConstants.generationSuffix;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.*;
import static pl.com.dspot.archiannotations.util.ElementUtils.isSubtype;

public class ViewPresenterHandler extends ObserversLinkingHandler<EComponentWithViewSupportHolder> implements MethodInjectionHandler<EComponentWithViewSupportHolder> {

    private final InjectHelper<EComponentWithViewSupportHolder> injectHelper;

    public ViewPresenterHandler(AndroidAnnotationsEnvironment environment) {
        super(ViewPresenter.class, environment);
        injectHelper = new InjectHelper<>(validatorHelper, this);
    }

    @Override
    public void validate(Element element, ElementValidation validation) {

        //Shouldn't be annotated with @Bean
        if (element.getAnnotation(Bean.class) != null) {
            validation.addError("You should remove the @Bean Annotation, the view model should be injected only with @ViewModel");
        }

        injectHelper.validate(ViewPresenter.class, element, validation);
        if (!validation.isValid()) {
            return;
        }

        validatorHelper.typeOrTargetValueHasAnnotation(EViewPresenter.class, element, validation);

        validatorHelper.isNotPrivate(element, validation);

        List<Class<? extends Annotation>> validAnnotations = asList(EActivity.class, EFragment.class, EViewModel.class);
        validatorHelper.enclosingElementHasOneOfAnnotations(element, validAnnotations, validation);

    }

    @Override
    public JBlock getInvocationBlock(Element element, EComponentWithViewSupportHolder holder) {
        return holder.getInitBodyInjectionBlock();
    }

    @Override
    public void process(Element element, EComponentWithViewSupportHolder holder) {
       injectHelper.process(element, holder);
    }

    @Override
    public void assignValue(JBlock targetBlock, IJAssignmentTarget fieldRef, EComponentWithViewSupportHolder holder, Element element, Element param) {

        TypeMirror typeMirror = annotationHelper.extractAnnotationClassParameter(element);
        if (typeMirror == null) {
            typeMirror = param.asType();
            typeMirror = getProcessingEnvironment().getTypeUtils().erasure(typeMirror);
        }

        String typeQualifiedName = typeMirror.toString();
        AbstractJClass enhancedClass = getJClass(annotationHelper.generatedClassQualifiedNameFromQualifiedName(typeQualifiedName));

        IJExpression injectingField;
        String viewModelClassName;
        if (element.asType().toString().endsWith(generationSuffix())) {
            injectingField = fieldRef;
            viewModelClassName = element.asType().toString().substring(0, element.asType().toString().length() - 1);
        } else {
            injectingField = cast(enhancedClass, fieldRef);
            viewModelClassName = element.asType().toString();
        }

        EViewModelHolder viewModelHolder = null;
        final boolean isEnclosedInEViewModel = holder.getAnnotatedElement().getAnnotation(EViewModel.class) != null;
        if (isEnclosedInEViewModel) {
            viewModelHolder = holder.getPluginHolder(new EViewModelHolder((EBeanHolder) holder));
        }

        JInvocation injectedViewPresenter = _new(enhancedClass);
        IJExpression lifecycleOwner;

        if (isEnclosedInEViewModel) {
            lifecycleOwner = cast(getJClass(LIFECYCLE_OWNER), viewModelHolder.getRootViewField());

        } else {
            lifecycleOwner = _this();
        }

        IJStatement assignment = fieldRef.assign(injectedViewPresenter);
        targetBlock.add(assignment);

        //Call the bindTo method
        JInvocation bindToInvoke = targetBlock.invoke(injectingField, EViewPresenterHolder.BIND_TO_METHOD_NAME).arg(holder.getContextRef());
        if (holder instanceof EActivityHolder || holder instanceof EFragmentHolder) {
            bindToInvoke.arg(_this());
        } else if (isEnclosedInEViewModel) {
            bindToInvoke.arg(viewModelHolder.getRootViewField());
        } else {
            bindToInvoke.arg(holder.getContextRef());
        }

        //Register as Lifecycle Observer if needed
        if (isSubtype(viewModelClassName, LIFECYCLE_OBSERVER, getProcessingEnvironment())) {
            bindToInvoke.arg(lifecycleOwner);
        }

    }

    @Override
    public void validateEnclosingElement(Element element, ElementValidation valid) {
        validatorHelper.enclosingElementHasEnhancedComponentAnnotation(element, valid);
    }

}
