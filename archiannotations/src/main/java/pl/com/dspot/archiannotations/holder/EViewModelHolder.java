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
import com.helger.jcodemodel.JMethod;
import org.androidannotations.annotations.export.Export;
import org.androidannotations.helper.ADIHelper;
import org.androidannotations.holder.EBeanHolder;
import org.androidannotations.internal.process.ProcessHolder;
import org.androidannotations.plugin.PluginClassHolder;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;

import static com.helger.jcodemodel.JExpr.ref;
import static com.helger.jcodemodel.JMod.PUBLIC;

public class EViewModelHolder extends BaseArchitectureComponentHolder {

	private JMethod onClearedMethod;
	private JBlock onClearedMethodBlock;
	private JBlock onClearedMethodFinalBlock;

	public EViewModelHolder(EBeanHolder holder) {
		super(holder);
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
