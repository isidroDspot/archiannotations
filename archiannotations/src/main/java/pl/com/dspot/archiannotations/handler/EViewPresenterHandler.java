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
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;
import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.annotations.EBean;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.holder.EBeanHolder;
import pl.com.dspot.archiannotations.annotation.EViewPresenter;
import pl.com.dspot.archiannotations.api.MethodCall;
import pl.com.dspot.archiannotations.holder.EViewPresenterHolder;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static com.dspot.declex.action.ActionsProcessor.hasAction;
import static com.helger.jcodemodel.JExpr.*;
import static com.helger.jcodemodel.JMod.NONE;
import static com.helger.jcodemodel.JMod.PRIVATE;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class EViewPresenterHandler extends BaseAnnotationHandler<EBeanHolder> {

    public EViewPresenterHandler(AndroidAnnotationsEnvironment environment) {
        super(EViewPresenter.class, environment);
    }

    @Override
    public void validate(Element element, ElementValidation valid) {
        validatorHelper.typeHasAnnotation(EBean.class, element, valid);
    }

    @Override
    public void process(Element element, EBeanHolder holder) {

        EViewPresenterHolder viewPresenterHolder = holder.getPluginHolder(new EViewPresenterHolder(holder));
        viewPresenterHolder.getEmptyConstructorMethod();
        viewPresenterHolder.getPresenterLiveDataField();
        viewPresenterHolder.getBindToMethod();

        shadowPublicMethods(viewPresenterHolder, element, holder);

    }

    public void shadowPublicMethods(EViewPresenterHolder viewPresenterHolder, Element element, EBeanHolder holder) {

        for (ExecutableElement method : methodsIn(element.getEnclosedElements())) {

            //Only shadow Public, no abstract methods
            if (!method.getModifiers().contains(Modifier.PUBLIC) || (method.getModifiers().contains(Modifier.ABSTRACT))) continue;

            //Create the MethodCall class for current method
            JDefinedClass methodCallClass;
            try {
                methodCallClass = holder.getGeneratedClass()._class(PRIVATE, methodCallClassNameFor(method));
            } catch (JClassAlreadyExistsException e) {
                continue;
            }

            JMethod constructor = methodCallClass.constructor(NONE);
            methodCallClass._implements(MethodCall.class);

            //Check the method call
            JBlock methodCallBlock = viewPresenterHolder.getPerformMethodCallBlock();
            methodCallBlock = methodCallBlock._if(viewPresenterHolder.getPerformMethodCallField()._instanceof(methodCallClass))._then();

            JVar methodCallCasted = methodCallBlock.decl(methodCallClass, "methodCallCasted",
                    cast(methodCallClass, viewPresenterHolder.getPerformMethodCallField()));

            //If has action syntax, call the action method instead of the super
            JInvocation methodCallToSuper;
            if (hasAction(method, getEnvironment())) {
                methodCallToSuper = methodCallBlock.invoke("$" + method.getSimpleName().toString());
            } else {
                methodCallToSuper = methodCallBlock.invoke(ref("super"), method.getSimpleName().toString());
            }

            //Add fields and params to methods
            JInvocation newInvocation = _new(methodCallClass);

            final List<? extends VariableElement> params = method.getParameters();
            for (VariableElement param : params) {
                String paramName = param.getSimpleName().toString();
                AbstractJClass paramType = codeModelHelper.elementTypeToJClass(param).erasure();

                JFieldVar field = methodCallClass.field(NONE, paramType, paramName);
                JVar methodParam = constructor.param(paramType, paramName);
                constructor.body().assign(_this().ref(field), methodParam);

                newInvocation = newInvocation.arg(ref(paramName));
                methodCallToSuper = methodCallToSuper.arg(methodCallCasted.ref(paramName));
            }

            //Override the method
            JMethod generatedMethod = codeModelHelper.overrideAnnotatedMethod(method, holder, false, false);
            JVar methodCallInstance = generatedMethod.body().decl(methodCallClass, "methodCall", newInvocation);
            generatedMethod.body().invoke(viewPresenterHolder.getPresenterLiveDataField(), "setValue").arg(methodCallInstance);

        }

        List<? extends TypeMirror> superTypes = getProcessingEnvironment().getTypeUtils().directSupertypes(element.asType());
        for (TypeMirror type : superTypes) {
            TypeElement superElement = getProcessingEnvironment().getElementUtils().getTypeElement(type.toString());
            if (superElement == null) continue;
            if (superElement.getKind().equals(ElementKind.INTERFACE)) continue;
            if (superElement.asType().toString().equals(Object.class.getCanonicalName())) break;

            shadowPublicMethods(viewPresenterHolder, superElement, holder);

        }

    }


    public static String methodCallClassNameFor(ExecutableElement element) {

        final String fieldName = element.getSimpleName().toString();
        final List<? extends VariableElement> params = ((ExecutableElement) element).getParameters();

        String className = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        for (VariableElement param : params) {
            String paramName = param.asType().toString();

            while (paramName.endsWith("[]")) paramName = paramName.substring(0, paramName.length() - 2) + "$";

            if (paramName.contains("<")) paramName = paramName.substring(0, paramName.indexOf('<'));

            className = className + "$" + paramName.substring(paramName.lastIndexOf('.') + 1);
        }

        className = className + "MethodCall";

        return  className;
    }

}
