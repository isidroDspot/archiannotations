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

import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;
import org.androidannotations.holder.EBeanHolder;
import org.androidannotations.internal.process.ProcessHolder;
import org.androidannotations.plugin.PluginClassHolder;

import static com.helger.jcodemodel.JExpr.ref;
import static com.helger.jcodemodel.JMod.PRIVATE;
import static com.helger.jcodemodel.JMod.PUBLIC;
import static org.androidannotations.helper.ModelConstants.generationSuffix;

public class EViewModelHolder extends PluginClassHolder<EBeanHolder> {

	public final static String BIND_TO_METHOD_NAME = "bindTo";

	private JMethod constructor;

	private JMethod onClearedMethod;
	private JBlock onClearedMethodBlock;
	private JBlock onClearedMethodFinalBlock;

	private JFieldVar rootViewField;

	private JMethod bindToMethod;

	public EViewModelHolder(EBeanHolder holder) {
		super(holder);
	}

	public void setEmptyConstructor() {
		//The View Models needs to have an empty constructor
		constructor = holder().getGeneratedClass().constructor(PUBLIC);
		JBlock constructorBody = constructor.body();
		constructorBody.invoke("super");
	}

	public void setBindToMethod() {
		bindToMethod = holder().getGeneratedClass().method(PUBLIC, getCodeModel().VOID, BIND_TO_METHOD_NAME);
		JVar contextParam = bindToMethod.param(getClasses().CONTEXT, "context");
		JVar rootViewParam = bindToMethod.param(getClasses().OBJECT, "rootView");

		JBlock body = bindToMethod.body();
		body.assign(holder().getContextField(), contextParam);
		body.assign(getRootViewField(), rootViewParam);
		body.invoke(holder().getInit());
	}

	public void setOnClearedMethod() {
		onClearedMethod = holder().getGeneratedClass().method(PUBLIC, getCodeModel().VOID, "onCleared");
		onClearedMethod.body().invoke(ref("super"), "onCleared");
		onClearedMethodBlock = onClearedMethod.body().blockVirtual();
		onClearedMethodFinalBlock = onClearedMethod.body().blockVirtual();
	}

	public JFieldVar getRootViewField() {
		if (rootViewField == null) {
			rootViewField = holder().getGeneratedClass().field(PRIVATE, getClasses().OBJECT, "rootView" + generationSuffix());
		}
		return rootViewField;
	}

	public JMethod getOnClearedMethod() {
		return onClearedMethod;
	}

	public JBlock getOnClearedMethodBlock() {
		return onClearedMethodBlock;
	}

	public JBlock getOnClearedMethodFinalBlock() {
		return onClearedMethodFinalBlock;
	}

	protected ProcessHolder.Classes getClasses() {
		return environment().getClasses();
	}

}
