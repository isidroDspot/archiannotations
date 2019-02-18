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

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;
import org.androidannotations.helper.AnnotationHelper;
import org.androidannotations.holder.GeneratedClassHolder;
import org.androidannotations.plugin.PluginClassHolder;
import pl.com.dspot.archiannotations.helper.override.APTCodeModelHelper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;

import static com.helger.jcodemodel.JExpr.*;
import static com.helger.jcodemodel.JMod.PUBLIC;
import static java.lang.reflect.Modifier.PRIVATE;
import static org.androidannotations.helper.CanonicalNameConstants.VIEW;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.OBSERVER;
import static pl.com.dspot.archiannotations.util.ElementUtils.isSubtype;
import static pl.com.dspot.archiannotations.util.FormatUtils.fieldToGetter;

public class ObserverHolder extends PluginClassHolder<GeneratedClassHolder> {

	private JMethod removeObserverMethod;
	private JVar removeObserverMethodParam;

	private Map<String, JMethod> getObserverMethodByElement = new HashMap<>();
	private Map<String, JBlock> getObserverMethodBodyByElement = new HashMap<>();
	private Map<String, JVar> getObserverMethodParamByElement = new HashMap<>();

	private AnnotationHelper annotationHelper;
	private APTCodeModelHelper codeModelHelper;

	public ObserverHolder(GeneratedClassHolder holder) {
		super(holder);
		this.annotationHelper = new AnnotationHelper(environment());
		this.codeModelHelper = new APTCodeModelHelper(environment());
	}

	public JMethod getRemoveObserverMethod() {
		if (removeObserverMethod == null) {
			setRemoveObserverMethod();
		}
		return removeObserverMethod;
	}

	public JMethod getObserverMethodFor(Element element) {
		return getObserverMethodFor(element, null, null);
	}

	public JMethod getObserverMethodFor(Element element, String propertyName, AbstractJClass propertyType) {
		if (!getObserverMethodByElement.containsKey(keyForObserver(element, propertyName, propertyType))) {
			setObserverMethodFor(element, propertyName, propertyType);
		}

		return getObserverMethodByElement.get(keyForObserver(element, propertyName, propertyType));
	}

	public JBlock getObserverMethodBodyFor(Element element) {
		return getObserverMethodBodyFor(element, null, null);
	}

	public JBlock getObserverMethodBodyFor(Element element, String propertyName, AbstractJClass propertyType) {
		if (!getObserverMethodBodyByElement.containsKey(keyForObserver(element, propertyName, propertyType))) {
			setObserverMethodFor(element, propertyName, propertyType);
		}

		return getObserverMethodBodyByElement.get(keyForObserver(element, propertyName, propertyType));
	}

	public JVar getObserverMethodParamFor(Element element) {
		return getObserverMethodParamFor(element, null, null);
	}

	public JVar getObserverMethodParamFor(Element element, String propertyName, AbstractJClass propertyType) {
		if (!getObserverMethodParamByElement.containsKey(keyForObserver(element, propertyName, propertyType))) {
			setObserverMethodFor(element, propertyName, propertyType);
		}

		return getObserverMethodParamByElement.get(keyForObserver(element, propertyName, propertyType));
	}

	public JVar getRemoveObserverMethodParam() {
		if (removeObserverMethodParam == null) {
			setRemoveObserverMethod();
		}
		return removeObserverMethodParam;
	}

	private String keyForObserver(Element element, String propertyName, AbstractJClass propertyType) {
		if (propertyName == null && propertyType == null) return element.toString();
		return element.toString() + ":" + propertyName + ":" + propertyType.name();
	}

	private void setObserverMethodFor(Element element, String propertyName, AbstractJClass propertyType) {

		final String fieldName = element.getSimpleName().toString();
		final String observerName = observerNameFor(element, propertyName, codeModelHelper);

		AbstractJClass observerClass = propertyType;

		if (observerClass == null) {

			if (element.getKind() == ElementKind.METHOD) {
				observerClass = codeModelHelper.elementTypeToJClass(((ExecutableElement) element).getParameters().get(0));
			} else {
				observerClass = codeModelHelper.elementTypeToJClass(element);
			}

		}

		//Create get observer method
		AbstractJClass Observer = getJClass(OBSERVER);

		//Create Observer field
		JFieldVar field = holder().getGeneratedClass().field(PRIVATE, Observer.narrow(observerClass), observerName);

		//Create Getter for this observer
		JMethod getterMethod = holder().getGeneratedClass().method(
				PUBLIC,
				Observer.narrow(observerClass),
				fieldToGetter(observerName)
		);
		JBlock creationBlock = getterMethod.body()._if(_this().ref(observerName).eq(_null()))._then();

		JDefinedClass AnonymousObserver = getCodeModel().anonymousClass(Observer.narrow(observerClass));
		JMethod anonymousOnChanged = AnonymousObserver.method(PUBLIC, getCodeModel().VOID, "onChanged");
		anonymousOnChanged.annotate(Override.class);
		JVar param = anonymousOnChanged.param(observerClass, "value");
		creationBlock.assign(field, _new(AnonymousObserver));
		getterMethod.body()._return(_this().ref(observerName));

		getObserverMethodParamByElement.put(keyForObserver(element, propertyName, propertyType), param);
		getObserverMethodBodyByElement.put(keyForObserver(element, propertyName, propertyType), anonymousOnChanged.body().blockVirtual());
		getObserverMethodByElement.put(keyForObserver(element, propertyName, propertyType), getterMethod);

		//Update the observer
		if (element.getKind() == ElementKind.METHOD) {

			ExecutableElement executableElement = (ExecutableElement) element;
			if (executableElement.getReturnType().toString().equals("void")) {
				anonymousOnChanged.body().invoke(fieldName).arg(param);
			} else {
				//Check the boolean, its value determines if it is needed to unregister the observer
				JVar removeObserver = anonymousOnChanged.body().decl(getCodeModel().BOOLEAN, "removeObserver", invoke(fieldName).arg(param));
				JBlock removeObserverBlock = anonymousOnChanged.body()._if(removeObserver)._then();
				removeObserverBlock.invoke(getRemoveObserverMethod()).arg(_this());
			}

		} if (element.asType().getKind().isPrimitive()) {

			anonymousOnChanged.body().assign(ref(fieldName), param);

		} else {

			TypeMirror typeMirror = environment().getProcessingEnvironment().getTypeUtils().erasure(element.asType());
			TypeElement viewTypeElement = annotationHelper.typeElementFromQualifiedName(typeMirror.toString());
			if (viewTypeElement == null) {
				return;
			}

			//Ignore views
			if (isSubtype(viewTypeElement, VIEW, environment().getProcessingEnvironment())) return;

			anonymousOnChanged.body().assign(ref(fieldName), param);

		}


	}

	private void setRemoveObserverMethod() {
		removeObserverMethod = getGeneratedClass().method(JMod.PRIVATE, getCodeModel().VOID, "removeObserver_");
		removeObserverMethodParam = removeObserverMethod.param(getJClass(OBSERVER), "observer");
	}

	public static String observerNameFor(Element element, String propertyName, APTCodeModelHelper codeModelHelper) {

		final String fieldName = element.getSimpleName().toString();

		String type;
		TypeMirror observerType;

		if (element.getKind() == ElementKind.METHOD) {
			type = "observerMethodFor";
			observerType = ((ExecutableElement) element).getParameters().get(0).asType();
		} else {
			type = "observerFieldFor";
			observerType = element.asType();
		}

		AbstractJClass observerClass = codeModelHelper.typeMirrorToJClass(observerType).erasure();
		return type + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + "$" + observerClass.name()
				+ (propertyName != null? "$" + propertyName : "");

	}

}
