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

import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JMethod;

import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.holder.EBeanHolder;
import org.androidannotations.holder.EComponentHolder;
import org.androidannotations.internal.process.ProcessHolder;
import org.androidannotations.plugin.PluginClassHolder;

import javax.lang.model.element.TypeElement;

import static com.helger.jcodemodel.JExpr.ref;
import static com.helger.jcodemodel.JMod.PRIVATE;
import static com.helger.jcodemodel.JMod.PUBLIC;
import static org.androidannotations.helper.ModelConstants.generationSuffix;

public class EViewModelHolder extends EComponentHolder {

	private JMethod onClearedMethod;
	private JBlock onClearedMethodBlock;
	private JBlock onClearedMethodFinalBlock;

	public EViewModelHolder(AndroidAnnotationsEnvironment environment, TypeElement annotatedElement) throws Exception {
		super(environment, annotatedElement);
	}

    @Override
    protected void setContextRef() {
        contextRef = ref("context");
    }

    @Override
    protected void setInit() {
        init = generatedClass.method(PRIVATE, getCodeModel().VOID, "init" + generationSuffix());
        init.param(getClasses().CONTEXT, "context");
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
