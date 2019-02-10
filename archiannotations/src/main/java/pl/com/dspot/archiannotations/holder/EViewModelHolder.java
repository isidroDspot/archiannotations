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

import com.dspot.declex.annotation.Event;
import com.dspot.declex.annotation.OnEvent;
import com.dspot.declex.annotation.UpdateOnEvent;
import com.dspot.declex.holder.EventHolder;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;
import org.androidannotations.annotations.export.Export;
import org.androidannotations.helper.ADIHelper;
import org.androidannotations.holder.EBeanHolder;
import org.androidannotations.internal.process.ProcessHolder;
import org.androidannotations.plugin.PluginClassHolder;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;

import java.util.List;

import static com.helger.jcodemodel.JExpr.TRUE;
import static com.helger.jcodemodel.JExpr.ref;
import static com.helger.jcodemodel.JMod.PRIVATE;
import static com.helger.jcodemodel.JMod.PUBLIC;
import static org.androidannotations.helper.ModelConstants.generationSuffix;

public class EViewModelHolder extends PluginClassHolder<EBeanHolder> {

	public final static String BIND_TO_METHOD_NAME = "bindTo";

	private JMethod emptyConstructorMethod;

	private JMethod onClearedMethod;
	private JBlock onClearedMethodBlock;
	private JBlock onClearedMethodFinalBlock;

	private JFieldVar rootViewField;

	private JMethod bindToMethod;

	public EViewModelHolder(EBeanHolder holder) {
		super(holder);
	}

	public JFieldVar getRootViewField() {
		if (rootViewField == null) {
			rootViewField = holder().getGeneratedClass().field(PRIVATE, getClasses().OBJECT, "rootView" + generationSuffix());
		}
		return rootViewField;
	}

	public JMethod getEmptyConstructorMethod() {
		if (emptyConstructorMethod == null) {
			setEmptyConstructor();
		}
		return emptyConstructorMethod;
	}

	public JMethod getBindToMethod() {
		if (bindToMethod == null) {
			setBindToMethod();
		}
		return bindToMethod;
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

		JFieldVar alreadyBound = holder().getGeneratedClass().field(PRIVATE, getCodeModel().BOOLEAN, "alreadyBound_");
		body = body._if(alreadyBound.not())._then();
		body.assign(alreadyBound, TRUE);

		body.assign(holder().getContextField(), contextParam);
		body.assign(getRootViewField(), rootViewParam);
		body.invoke(holder().getInit());
	}

	private void setOnClearedMethod() {
		onClearedMethod = holder().getGeneratedClass().method(PUBLIC, getCodeModel().VOID, "onCleared");
		onClearedMethod.body().invoke(ref("super"), "onCleared");
		onClearedMethodBlock = onClearedMethod.body().blockVirtual();
		onClearedMethodFinalBlock = onClearedMethod.body().blockVirtual();

		//Check if events registration is needed
		ADIHelper adiHelper = new ADIHelper(environment());
		List<ExecutableElement> methods = ElementFilter.methodsIn(getAnnotatedElement().getEnclosedElements());

		boolean shouldRegisterForEvent = false;

		for (ExecutableElement method : methods) {

			if (adiHelper.hasAnnotation(method, Export.class)) continue;

			if (adiHelper.hasAnnotation(method, Event.class)
				|| adiHelper.hasAnnotation(method, OnEvent.class)
				|| adiHelper.hasAnnotation(method, UpdateOnEvent.class)) {

				shouldRegisterForEvent = true;
			}

		}

		//Set the onClearMethod as the place to unregister events
		if (shouldRegisterForEvent) {
			EventHolder eventHolder = holder().getPluginHolder(new EventHolder(holder()));
			eventHolder.setEventUnregisteringBlock(onClearedMethodBlock);
			eventHolder.registerAsEventListener();
		}

	}

	protected ProcessHolder.Classes getClasses() {
		return environment().getClasses();
	}

}
