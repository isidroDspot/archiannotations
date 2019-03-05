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
package pl.com.dspot.archiannotations.holder;

import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;
import org.androidannotations.holder.EBeanHolder;
import org.androidannotations.holder.EComponentHolder;
import org.androidannotations.plugin.PluginClassHolder;

import static com.helger.jcodemodel.JExpr._this;
import static com.helger.jcodemodel.JMod.PRIVATE;
import static com.helger.jcodemodel.JMod.PUBLIC;
import static org.androidannotations.helper.ModelConstants.generationSuffix;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.LIFECYCLE_OBSERVER;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.LIFECYCLE_OWNER;
import static pl.com.dspot.archiannotations.util.ElementUtils.isSubtype;

public class BaseArchitectureComponentHolder extends PluginClassHolder<EBeanHolder>  {

    public final static String BIND_TO_METHOD_NAME = "bindTo";

    private JMethod emptyConstructorMethod;

    private JFieldVar rootViewField;
    private JFieldVar alreadyBoundToField;

    private JMethod bindToMethod;

    public BaseArchitectureComponentHolder(EBeanHolder holder) {
        super(holder);
    }

    public JMethod getEmptyConstructorMethod() {
        if (emptyConstructorMethod == null) {
            setEmptyConstructor();
        }
        return emptyConstructorMethod;
    }

    public JFieldVar getRootViewField() {
        if (rootViewField == null) {
            rootViewField = holder().getGeneratedClass().field(PRIVATE, getClasses().OBJECT, "rootView" + generationSuffix());
        }
        return rootViewField;
    }

    public JMethod getBindToMethod() {
        if (bindToMethod == null) {
            setBindToMethod();
        }
        return bindToMethod;
    }

    public JFieldVar getAlreadyBoundToField() {
        if (alreadyBoundToField == null) {
            setBindToMethod();
        }
        return alreadyBoundToField;
    }

    private void setEmptyConstructor() {
        //The View Models needs to have an empty emptyConstructorMethod
        emptyConstructorMethod = holder().getGeneratedClass().constructor(PUBLIC);
        JBlock constructorBody = emptyConstructorMethod.body();
        constructorBody.invoke("super");
    }

    private void setBindToMethod() {
        bindToMethod = holder().getGeneratedClass().method(PUBLIC, getCodeModel().VOID, BIND_TO_METHOD_NAME);
        JVar contextParam = bindToMethod.param(getClasses().CONTEXT, "context");
        JVar rootViewParam = bindToMethod.param(getClasses().OBJECT, "rootView");

        JBlock body = bindToMethod.body();

        alreadyBoundToField = holder().getGeneratedClass().field(PRIVATE, getCodeModel().INT, "alreadyBound_");
        IJExpression identityHashCodeOfContext = getJClass(System.class).staticInvoke("identityHashCode").arg(contextParam);

        body = body._if(alreadyBoundToField.ne(identityHashCodeOfContext))._then();
        body.assign(alreadyBoundToField, identityHashCodeOfContext);

        body.assign(holder().getContextField(), contextParam);
        body.assign(getRootViewField(), rootViewParam);
        body.invoke(holder().getInit());

        if (isSubtype(getAnnotatedElement(), LIFECYCLE_OBSERVER, getProcessingEnvironment())) {
            JVar lifecycleOwner = bindToMethod.param(getJClass(LIFECYCLE_OWNER), "lifecycleOwner");
            body.add(lifecycleOwner.invoke("getLifecycle").invoke("addObserver").arg(_this()));
        }

    }

}
