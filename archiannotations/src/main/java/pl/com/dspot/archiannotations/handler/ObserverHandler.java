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
import pl.com.dspot.archiannotations.annotation.Observer;

import javax.lang.model.element.Element;

public class ObserverHandler extends BaseAnnotationHandler<EComponentHolder> {

    public ObserverHandler(AndroidAnnotationsEnvironment environment) {
        super(Observer.class, environment);
    }

    @Override
    public void validate(Element element, ElementValidation validation) {
//        validatorHelper.param.anyType().validate((ExecutableElement) element, validation);
//
//        Observer observer = adiHelper.getAnnotation(element, Observer.class);
//
//        if (!observer.observeForever()) {
//            TypeElement rootElement = getRootElement(element);
//            if (!isSubtype(rootElement, LIFECYCLE_OWNER, getProcessingEnvironment())
//                    && !isSubtype(rootElement, VIEW_MODEL, getProcessingEnvironment())) {
//                validation.addError("@Observer can be placed only inside classes which implement LifeCycleOwner or ViewModel, or should be marked with \"observerForever\"."
//                                    + "\nIf you are aware of the consequences of observing another class forever, you can manually set \"observeForever = true\"");
//            }
//        }
//
//        validatorHelper.returnTypeIsVoidOrBoolean((ExecutableElement) element, validation);

    }

    @Override
    public void process(Element element, EComponentHolder holder) {

//        ObserversHolder observersHolder = holder.getPluginHolder(new ObserversHolder(holder));
//
//        final String fieldName = element.getSimpleName().toString();
//        final ExecutableElement executableElement = (ExecutableElement) element;
//        final String observerName = observerNameFor(executableElement, codeModelHelper);
//
//        final List<? extends VariableElement> params = (executableElement).getParameters();
//
//        //Create get observer method
//        {
//            AbstractJClass Observer = getJClass(OBSERVER);
//            AbstractJClass ReferencedClass = codeModelHelper.elementTypeToJClass(params.get(0));
//
//            //Create Observer field
//            JFieldVar field = holder.getGeneratedClass().field(PRIVATE, Observer.narrow(ReferencedClass), observerName);
//
//            //Create Getter for this observer
//            JMethod getterMethod = holder.getGeneratedClass().method(
//                    JMod.PUBLIC,
//                    Observer.narrow(ReferencedClass),
//                    fieldToGetter(observerName)
//            );
//            JBlock creationBlock = getterMethod.body()._if(_this().ref(observerName).eqNull())._then();
//
//            JDefinedClass AnonymousObserver = getCodeModel().anonymousClass(Observer.narrow(ReferencedClass));
//            JMethod anonymousOnChanged = AnonymousObserver.method(JMod.PUBLIC, getCodeModel().VOID, "onChanged");
//            anonymousOnChanged.annotate(Override.class);
//            JVar param = anonymousOnChanged.param(ReferencedClass, "value");
//
//            //Call this method
//            if (executableElement.getReturnType().toString().equals("void")) {
//                anonymousOnChanged.body().invoke(fieldName).arg(param);
//            } else {
//                //Check the boolean, its value determines if it is needed to unregister the observer
//                JVar removeObserver = anonymousOnChanged.body().decl(getCodeModel().BOOLEAN, "removeObserver", invoke(fieldName).arg(param));
//                JBlock removeObserverBlock = anonymousOnChanged.body()._if(removeObserver)._then();
//                removeObserverBlock.invoke(observersHolder.getRemoveObserverMethod()).arg(_this());
//            }
//
//            creationBlock.assign(field, _new(AnonymousObserver));
//            getterMethod.body()._return(_this().ref(observerName));
//        }



    }

//    public static String observerNameFor(ExecutableElement element, APTCodeModelHelper codeModelHelper) {
//
//        final String fieldName = element.getSimpleName().toString();
//        final List<? extends VariableElement> params = ((ExecutableElement) element).getParameters();
//        AbstractJClass ReferencedClass = codeModelHelper.elementTypeToJClass(params.get(0)).erasure();
//
//        return "observerFor" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + "$" + ReferencedClass.name();
//    }

}
