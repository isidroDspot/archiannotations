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
package pl.com.dspot.archiannotations.helper;

import org.androidannotations.AndroidAnnotationsEnvironment;

public class ViewsLinkingObservablesHelper {

//    private AndroidAnnotationsEnvironment environment;
//
    public ViewsLinkingObservablesHelper(AndroidAnnotationsEnvironment environment) {
//        this.environment = environment;
    }
//
//    public void linkViewToObservable(String observableFieldName, String observableTypeName,
//                                     Element injectingElement, AbstractJClass injectingEnhancedClass, EComponentHolder injectingElementHolder) {
//
//        final ViewsHolder viewsHolder = injectingElementHolder.getPluginHolder(new ViewsHolder((EComponentWithViewSupportHolder) injectingElementHolder));
//
//        //Determine if the observer can be bound to a visual component property
//        ViewsHolder.ViewProperty viewProperty = viewsHolder.getPropertySetterForFieldName(observableFieldName, observableTypeName);
//        if (viewProperty != null) {
//
//            bindObserver(viewProperty, observableFieldName, observableTypeName, injectingElement, injectingEnhancedClass, viewsHolder.holder());
//
//        }
//
//    }
//
//    private void bindObserver(ViewsHolder.ViewProperty viewProperty, String fieldName, String referencedClass,
//                              Element element, AbstractJClass enhancedClass, EComponentWithViewSupportHolder holder) {
//
//        AbstractJClass Observer = getJClass(OBSERVER);
//        AbstractJClass ReferencedClass = getJClass(referencedClass);
//
//        JDefinedClass AnonymousObserver = getCodeModel().anonymousClass(Observer.narrow(ReferencedClass));
//        JMethod anonymousOnChanged = AnonymousObserver.method(JMod.PUBLIC, getCodeModel().VOID, "onChanged");
//        anonymousOnChanged.annotate(Override.class);
//        JVar param = anonymousOnChanged.param(ReferencedClass, "value");
//
//        assignProperty(viewProperty, referencedClass, param, anonymousOnChanged.body());
//
//        JBlock block = holder.getOnViewChangedBodyAfterInjectionBlock().block();
//        JVar observer = block.decl(Observer.narrow(ReferencedClass), "observer", _new(AnonymousObserver));
//
//        IJExpression archField;
//        if (element.asType().toString().endsWith(ModelConstants.generationSuffix())) {
//            archField = ref(element.getSimpleName().toString());
//        } else {
//            archField = cast(enhancedClass, ref(element.getSimpleName().toString()));
//        }
//
//
//        block.add(invoke(archField, fieldToGetter(fieldName)).invoke("observe").arg(_this()).arg(observer));
//
//    }
//
//    private void assignProperty(ViewsHolder.ViewProperty viewProperty, String referencedClass, JVar param, JBlock block) {
//
//        final String viewClass = viewProperty.viewClass.fullName();
//        final JFieldRef view = viewProperty.view;
//
//        if (viewProperty.property == null) {
//
//            //CompoundButtons, if the param is boolean, it will set the checked state
//            if (TypeUtils.isSubtype(viewClass, "android.widget.CompoundButton", getProcessingEnvironment())) {
//                if (referencedClass.equals("boolean") || referencedClass.equals(BOOLEAN)) {
//                    block.invoke(view, "setChecked").arg(param);
//
//                    //This ensures not to check against TextView (since a CompoundButton is a TextView descendant)
//                    return;
//                }
//            }
//
//            if (isSubtype(viewClass, "android.widget.TextView", getProcessingEnvironment())) {
//                if (isSubtype(referencedClass, "android.text.Spanned", getProcessingEnvironment())) {
//                    block.invoke(view, "setText").arg(param);
//                } else if (isSubtype(referencedClass, CharSequence.class.getCanonicalName(), getProcessingEnvironment())) {
//                    block.invoke(view, "setText").arg(param);
//                } else {
//                    block.invoke(view, "setText").arg(getJClass(String.class).staticInvoke("valueOf").arg(param));
//                }
//            }
//
//        } else {
//
//            block.invoke(view, "set" + viewProperty.property).arg(param);
//
//        }
//
//    }
//
//    private ProcessingEnvironment getProcessingEnvironment() {
//        return environment.getProcessingEnvironment();
//    }
//
//    private AbstractJClass getJClass(Class<?> clazz) {
//        return environment.getJClass(clazz);
//    }
//
//    private AbstractJClass getJClass(String fullyQualifiedName) {
//        return environment.getJClass(fullyQualifiedName);
//    }
//
//    private JCodeModel getCodeModel() {
//        return environment.getCodeModel();
//    }

}
