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
import com.helger.jcodemodel.JMethod;
import org.androidannotations.helper.APTCodeModelHelper;
import org.androidannotations.holder.EComponentHolder;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import static pl.com.dspot.archiannotations.ArchCanonicalNameConstants.VIEW_MODEL;

public class ViewModelHolder extends BaseArchitecturalHolder {

	private JMethod onClearedMethod;
	private JBlock onClearedMethodBlock;
	private JBlock onClearedMethodFinalBlock;

	private APTCodeModelHelper codeModelHelper;

	public ViewModelHolder(EComponentHolder holder) {
		super(holder);
		codeModelHelper = new APTCodeModelHelper(environment());
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

		TypeElement viewModelElement = processingEnv().getElementUtils().getTypeElement(VIEW_MODEL);
		for (Element viewModelElem : viewModelElement.getEnclosedElements()) {
			if (viewModelElem.getSimpleName().toString().equals("onCleared")) {
				onClearedMethod = codeModelHelper.overrideAnnotatedMethod((ExecutableElement) viewModelElem, holder());

				onClearedMethodBlock = onClearedMethod.body().blockVirtual();
				onClearedMethodFinalBlock = onClearedMethod.body().blockVirtual();

				break;
			}
		}

	}

}
