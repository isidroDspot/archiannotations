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

import com.helger.jcodemodel.*;
import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.annotations.*;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.handler.MethodInjectionHandler;
import org.androidannotations.helper.InjectHelper;
import org.androidannotations.holder.EActivityHolder;
import org.androidannotations.holder.EBeanHolder;
import org.androidannotations.holder.EComponentWithViewSupportHolder;
import org.androidannotations.holder.EFragmentHolder;
import pl.com.dspot.archiannotations.annotation.EViewModel;
import pl.com.dspot.archiannotations.annotation.ViewModel;
import pl.com.dspot.archiannotations.holder.EActivityUtilsHolder;
import pl.com.dspot.archiannotations.holder.EViewModelHolder;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.helger.jcodemodel.JExpr.*;
import static java.util.Arrays.asList;
import static org.androidannotations.helper.CanonicalNameConstants.FRAGMENT_ACTIVITY;
import static org.androidannotations.helper.ModelConstants.generationSuffix;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.*;
import static pl.com.dspot.archiannotations.util.ElementUtils.isSubtype;

public class ViewModelHandler extends BaseAnnotationHandler<EComponentWithViewSupportHolder> implements MethodInjectionHandler<EComponentWithViewSupportHolder> {

    private final InjectHelper<EComponentWithViewSupportHolder> injectHelper;

    public ViewModelHandler(AndroidAnnotationsEnvironment environment) {
        super(ViewModel.class, environment);

        injectHelper = new InjectHelper<>(validatorHelper, this);

    }

    @Override
    public void validate(Element element, ElementValidation validation) {

        //Shouldn't be annotated with @Bean
        if (element.getAnnotation(Bean.class) != null) {
            validation.addError("You should remove the @Bean Annotation, the view model should be injected only with @ViewModel");
        }

        injectHelper.validate(ViewModel.class, element, validation);
        if (!validation.isValid()) {
            return;
        }

        validatorHelper.typeOrTargetValueHasAnnotation(EViewModel.class, element, validation);

        validatorHelper.isNotPrivate(element, validation);

        List<Class<? extends Annotation>> validAnnotations = asList(EActivity.class, EFragment.class, EViewModel.class, EBean.class);
        validatorHelper.enclosingElementHasOneOfAnnotations(element, validAnnotations, validation);

        if (element.getEnclosingElement().getAnnotation(EBean.class) != null
            && element.getEnclosingElement().getAnnotation(EViewModel.class) == null) {

            ViewModel viewModel = element.getAnnotation(ViewModel.class);
            if (viewModel.scope() != ViewModel.Scope.Activity) {
                validation.addError("You can inject ViewModels on @EBean annotated classes only with the Activity Scope");
            }

        }

    }

    @Override
    public JBlock getInvocationBlock(Element element, EComponentWithViewSupportHolder holder) {

        if (holder instanceof EActivityHolder) {
            EActivityUtilsHolder activityUtilsHolder = holder.getPluginHolder(new EActivityUtilsHolder((EActivityHolder) holder));
            return activityUtilsHolder.getOnCreateAfterSuperBlock();
        }

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

        //Inject the ViewModel
        ViewModel viewModel = element.getAnnotation(ViewModel.class);

        JBlock previousTargetBlock = targetBlock;
        JInvocation injectedViewModel;
        IJExpression lifecycleOwner;
        switch (viewModel.scope()) {
            case Activity:

                //Add check for Fragment Activity Content
                JBlock checkLifecycleBlock = holder.getInitBodyBeforeInjectionBlock()
                        ._if(holder.getContextRef()._instanceof(getJClass(FRAGMENT_ACTIVITY)).not())._then();

                checkLifecycleBlock._throw(_new(getJClass(IllegalStateException.class))
                        .arg("This Bean can only be injected in the context of a FragmentActivity"));

                injectedViewModel = getJClass(VIEW_MODEL_PROVIDERS)
                        .staticInvoke("of").arg(cast(getJClass(FRAGMENT_ACTIVITY), holder.getContextRef()))
                        .invoke("get").arg(enhancedClass.dotclass());

                lifecycleOwner = cast(getJClass(LIFECYCLE_OWNER), holder.getContextRef());

                break;

            default:

                if (isEnclosedInEViewModel) {

                    JConditional ifFragmentActivity = targetBlock._if(viewModelHolder.getRootViewField()._instanceof(getClasses().FRAGMENT_ACTIVITY));

                    ifFragmentActivity._then().add(
                        getJClass(VIEW_MODEL_PROVIDERS)
                            .staticInvoke("of").arg(cast(getClasses().FRAGMENT_ACTIVITY, viewModelHolder.getRootViewField()))
                            .invoke("get").arg(enhancedClass.dotclass())
                    );

                    targetBlock = ifFragmentActivity._else();

                    injectedViewModel = getJClass(VIEW_MODEL_PROVIDERS)
                            .staticInvoke("of").arg(cast(getClasses().SUPPORT_V4_FRAGMENT, viewModelHolder.getRootViewField()))
                            .invoke("get").arg(enhancedClass.dotclass());

                    lifecycleOwner = cast(getJClass(LIFECYCLE_OWNER), viewModelHolder.getRootViewField());


                } else {

                    injectedViewModel = getJClass(VIEW_MODEL_PROVIDERS)
                            .staticInvoke("of").arg(_this())
                            .invoke("get").arg(enhancedClass.dotclass());

                    lifecycleOwner = _this();

                }
        }

        IJStatement assignment = fieldRef.assign(injectedViewModel);
        targetBlock.add(assignment);

        targetBlock = previousTargetBlock;

        //Call the bindTo method
        JInvocation bindToInvoke = invoke(injectingField, EViewModelHolder.BIND_TO_METHOD_NAME).arg(holder.getContextRef());
        if (holder instanceof EActivityHolder || holder instanceof EFragmentHolder) {
            bindToInvoke.arg(_this());
        } else if (isEnclosedInEViewModel) {
            bindToInvoke.arg(viewModelHolder.getRootViewField());
        } else {
            bindToInvoke.arg(holder.getContextRef());
        }

        targetBlock.add(bindToInvoke);

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
