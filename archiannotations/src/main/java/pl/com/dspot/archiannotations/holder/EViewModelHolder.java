/**
 * Copyright (C) 2018-2019 DSpot Sp. z o.o
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

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;

import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.holder.EComponentHolder;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import pl.com.dspot.archiannotations.annotation.EViewModel;

import static com.helger.jcodemodel.JExpr._new;
import static com.helger.jcodemodel.JExpr._null;
import static com.helger.jcodemodel.JExpr.cast;
import static com.helger.jcodemodel.JExpr.invokeSuper;
import static com.helger.jcodemodel.JExpr.ref;
import static com.helger.jcodemodel.JMod.PRIVATE;
import static com.helger.jcodemodel.JMod.PUBLIC;
import static com.helger.jcodemodel.JMod.STATIC;
import static org.androidannotations.helper.CanonicalNameConstants.APPLICATION;
import static org.androidannotations.helper.CanonicalNameConstants.FRAGMENT_ACTIVITY;
import static org.androidannotations.helper.CanonicalNameConstants.SUPPORT_V4_FRAGMENT;
import static org.androidannotations.helper.ModelConstants.generationSuffix;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.ANDROID_VIEW_MODEL;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.LIFECYCLE_OWNER;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.VIEW_MODEL_PROVIDERS;
import static pl.com.dspot.archiannotations.util.ElementUtils.isSubtype;

public class EViewModelHolder extends EComponentHolder {

	public static final String GET_INSTANCE_METHOD_NAME = "getInstance" + generationSuffix();

	private JMethod onClearedMethod;
	private JBlock onClearedMethodBlock;
	private JBlock onClearedMethodFinalBlock;

	public EViewModelHolder(AndroidAnnotationsEnvironment environment, TypeElement annotatedElement) throws Exception {
		super(environment, annotatedElement);
		setConstructor();
	}

	private void setConstructor() {

		if (isSubtype(annotatedElement, ANDROID_VIEW_MODEL, getProcessingEnvironment())) {

			JMethod constructor = generatedClass.constructor(PRIVATE);
			JVar constructorApplicationParam = constructor.param(getJClass(APPLICATION), "application");
			JBlock constructorBody = constructor.body();

			List<ExecutableElement> constructors = ElementFilter.constructorsIn(annotatedElement.getEnclosedElements());
			ExecutableElement superConstructor = constructors.get(0);
			if (superConstructor.getParameters().size() == 1) {
				constructorBody.add(invokeSuper().arg(constructorApplicationParam));
			}

		}

	}

    @Override
    protected void setContextRef() {
        contextRef = ref("context");
    }

    @Override
    protected void setRootFragmentRef() {
        rootFragmentRef = ref("rootFragment" + generationSuffix());
    }

    @Override
    protected void setInit() {
        init = generatedClass.method(PRIVATE, getCodeModel().VOID, "init" + generationSuffix());
        init.param(getClasses().CONTEXT, "context");
        JVar lifecycleOwnerParam = init.param(getJClass(LIFECYCLE_OWNER), "lifecycleOwner");

        JVar rootFragmentVar = init.body().decl(getClasses().OBJECT, "rootFragment" + generationSuffix(), _null());
        init.body()._if(lifecycleOwnerParam._instanceof(getClasses().SUPPORT_V4_FRAGMENT))._then()
                .assign(rootFragmentVar, lifecycleOwnerParam);
    }

	public void createFactoryMethod(EViewModel.Scope scope) {

		AbstractJClass narrowedGeneratedClass = codeModelHelper.narrowGeneratedClass(generatedClass, annotatedElement.asType());

		JMethod factoryMethod = generatedClass.method(PUBLIC | STATIC, narrowedGeneratedClass, GET_INSTANCE_METHOD_NAME);
		codeModelHelper.generify(factoryMethod, annotatedElement);

		JVar contextParam = factoryMethod.param(getClasses().CONTEXT, "context");
		JVar lifecycleOwnerParam = factoryMethod.param(getJClass(LIFECYCLE_OWNER), "lifecycleOwner");

		JBlock factoryMethodBody = factoryMethod.body();
        JVar viewModelVar = factoryMethodBody.decl(getGeneratedClass(), "viewModel" + generationSuffix());

		JInvocation injectViewModelInFragmentActivity = getJClass(VIEW_MODEL_PROVIDERS)
				.staticInvoke("of").arg(cast(getJClass(FRAGMENT_ACTIVITY), lifecycleOwnerParam))
				.invoke("get").arg(getGeneratedClass().dotclass());
		JInvocation injectViewModelInFragment = getJClass(VIEW_MODEL_PROVIDERS)
				.staticInvoke("of").arg(cast(getJClass(SUPPORT_V4_FRAGMENT), lifecycleOwnerParam))
				.invoke("get").arg(getGeneratedClass().dotclass());

        JConditional ifFragment = factoryMethodBody._if(lifecycleOwnerParam._instanceof(getJClass(SUPPORT_V4_FRAGMENT)));
        ifFragment._then().assign(viewModelVar, injectViewModelInFragment);

		JConditional ifFragmentActivity = ifFragment._elseif(lifecycleOwnerParam._instanceof(getJClass(FRAGMENT_ACTIVITY)));
		ifFragmentActivity._then().assign(viewModelVar, injectViewModelInFragmentActivity);

        ifFragmentActivity._else()._throw(
		        _new(getJClass(IllegalStateException.class)).arg("This ViewModel was injected in an Activity or Fragment which doesn't extend FragmentActivity or Support Fragment"));

		factoryMethodBody.add(viewModelVar.invoke(getInit()).arg(contextParam).arg(lifecycleOwnerParam));

		factoryMethodBody._return(viewModelVar);

	}

	public JMethod getOnClearedMethod() {
		if (onClearedMethod == null) {
			setOnClearedMethod();
		}
		return onClearedMethod;
	}

	public JBlock getOnClearedMethodBlock() {
		if (onClearedMethodBlock == null) {
			setOnClearedMethod();
		}
		return onClearedMethodBlock;
	}

	public JBlock getOnClearedMethodFinalBlock() {
		if (onClearedMethodFinalBlock == null) {
			setOnClearedMethod();
		}
		return onClearedMethodFinalBlock;
	}

	private void setOnClearedMethod() {
		onClearedMethod = getGeneratedClass().method(PUBLIC, getCodeModel().VOID, "onCleared");
		onClearedMethod.body().invoke(ref("super"), "onCleared");
		onClearedMethodBlock = onClearedMethod.body().blockVirtual();
		onClearedMethodFinalBlock = onClearedMethod.body().blockVirtual();
	}

}
