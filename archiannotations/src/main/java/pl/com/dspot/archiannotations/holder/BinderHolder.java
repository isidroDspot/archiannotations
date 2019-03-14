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
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.handler.MethodInjectionHandler;
import org.androidannotations.helper.IdAnnotationHelper;
import org.androidannotations.holder.EActivityHolder;
import org.androidannotations.holder.EBeanHolder;
import org.androidannotations.holder.EComponentWithViewSupportHolder;
import org.androidannotations.holder.FoundViewHolder;
import org.androidannotations.logger.Logger;
import org.androidannotations.logger.LoggerFactory;
import org.androidannotations.plugin.PluginClassHolder;
import org.androidannotations.rclass.IRClass;
import pl.com.dspot.archiannotations.annotation.Observable;
import pl.com.dspot.archiannotations.annotation.Observer;
import pl.com.dspot.archiannotations.annotation.ViewModel;
import pl.com.dspot.archiannotations.helper.ViewsPropertiesReaderHelper;
import pl.com.dspot.archiannotations.helper.override.APTCodeModelHelper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.helger.jcodemodel.JExpr.*;
import static org.androidannotations.helper.CanonicalNameConstants.*;
import static org.androidannotations.helper.ModelConstants.generationSuffix;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.LIFECYCLE_OWNER;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.VIEW_MODEL;
import static pl.com.dspot.archiannotations.util.ElementUtils.isSubtype;
import static pl.com.dspot.archiannotations.util.FormatUtils.fieldToGetter;

public class BinderHolder extends PluginClassHolder<EComponentWithViewSupportHolder> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BinderHolder.class);

	private Map<VariableElement, IJExpression> observableFieldInvocations = new HashMap<>();

	private IdAnnotationHelper annotationHelper;
	private APTCodeModelHelper codeModelHelper;
	private ViewsPropertiesReaderHelper propertiesHelper;

	private ObserverHolder observerHolder;

	public BinderHolder(EComponentWithViewSupportHolder holder) {
		super(holder);

		this.annotationHelper = new IdAnnotationHelper(environment(), ViewById.class.getName());
		this.codeModelHelper = new APTCodeModelHelper(environment());
		this.propertiesHelper = ViewsPropertiesReaderHelper.getInstance(environment());

		this.observerHolder = holder().getPluginHolder(new ObserverHolder(holder));

	}

	public void exploreObservablesAndBindingMethods() {

		TypeElement binderElement = holder().getAnnotatedElement();
		exploreObservablesAndBindingMethodsIn(binderElement, _this());

		List<VariableElement> fields = ElementFilter.fieldsIn(binderElement.getEnclosedElements());
		for (VariableElement field : fields) {

			ViewModel viewModel = field.getAnnotation(ViewModel.class);
			if (viewModel != null) {
				TypeElement typeElement = annotationHelper.typeElementFromQualifiedName(field.asType().toString());
				exploreObservablesAndBindingMethodsIn(typeElement, getExpressionForField(field));
			}

			Bean bean = field.getAnnotation(Bean.class);
			if (bean != null) {
				TypeElement typeElement = annotationHelper.typeElementFromQualifiedName(field.asType().toString());
				exploreObservablesAndBindingMethodsIn(typeElement, getExpressionForField(field));
			}

		}

	}

	public void performObservations() {

		TypeElement binderElement = holder().getAnnotatedElement();
		List<VariableElement> fields = ElementFilter.fieldsIn(binderElement.getEnclosedElements());

		for (VariableElement field : fields) {

			Observer observer = field.getAnnotation(Observer.class);
			if (observer != null) {

				if (field.asType().getKind().isPrimitive()) {

					if (!bindObserver(field)) {
						LOGGER.error("@Observer not resolved", field);
					}

				} else if (isSubtype(field.asType(), VIEW, environment().getProcessingEnvironment())) {

					JFieldRef idRef = annotationHelper.extractOneAnnotationFieldRef(field, IRClass.Res.ID, true);
					AbstractJClass viewClass = codeModelHelper.typeMirrorToJClass(field.asType());

					//Create the view reference
					JFieldRef viewHolderTarget = ref(field.getSimpleName().toString());
					FoundViewHolder viewHolder = holder().getFoundViewHolder(idRef, viewClass, viewHolderTarget);
					if (!viewHolder.getRef().equals(viewHolderTarget)) {
						holder().getOnViewChangedBodyInjectionBlock().add(viewHolderTarget.assign(viewHolder.getOrCastRef(viewClass)));
					}

					if (!bindObservablesToView(field, field.asType())) {
						LOGGER.error("@Observer not resolved", field);
					}

				} else if (!bindObserver(field)) {
					LOGGER.error("@Observer not resolved", field);
				}

			}

		}

		List<ExecutableElement> methods = ElementFilter.methodsIn(binderElement.getEnclosedElements());
		for (ExecutableElement method : methods) {

			Observer observer = method.getAnnotation(Observer.class);
			if (observer != null) {
				if (!bindObserver(method)) {
					LOGGER.warn("@Observer not resolved", method);
				}
			}

		}

	}

	private boolean bindObservablesToView(Element element, TypeMirror viewType) {

		final String viewReference = element.getSimpleName().toString();
		boolean bound = false;

		for (VariableElement observableField : observableFieldInvocations.keySet()) {

			final String observableName = observableField.getSimpleName().toString();

			if (observableName.startsWith(viewReference)) {

				if (observableName.equals(viewReference)) {
					//Bind by the default property
					bound |= bindViewObserver(viewReference, viewType, null,
							observableField, observableFieldInvocations.get(observableField),
							element);
					continue;
				}

				final Map<String, TypeMirror> getters = new HashMap<>();
				final Map<String, Set<TypeMirror>> setters = new HashMap<>();
				propertiesHelper.readGettersAndSetters(element.asType().toString(), getters, setters);

				final String propertyName = observableName.substring(viewReference.length());

				if (setters.containsKey(propertyName)) {

					String observableClassName = codeModelHelper.elementTypeToJClass(observableField, true).fullName();
					TypeElement observableType = null;

					for (TypeMirror setter : setters.get(propertyName)) {

						boolean hasSetter = isSubtype(wrapperToPrimitive(observableClassName), setter.toString(), environment().getProcessingEnvironment());

						if (!hasSetter) {

							if (observableType == null) {
								observableType = annotationHelper.typeElementFromQualifiedName(observableClassName);
								if (observableType == null) continue;
							}

							hasSetter = isSubtype(observableType.asType(), setter, environment().getProcessingEnvironment());
						}

						if (hasSetter) {

							bound |= bindViewObserver(
									viewReference, viewType, propertyName,
									observableField, observableFieldInvocations.get(observableField),
									element);
						}

					}

				}
			}

		}

		return bound;

	}

	private boolean bindObserver(Element element) {

		final String observerReference = element.getSimpleName().toString();
		boolean bound = false;

		for (VariableElement observableField : observableFieldInvocations.keySet()) {

			final String observableName = observableField.getSimpleName().toString();

			if (observableName.equals(observerReference)) {
				bound |= bindObserver(element, observableFieldInvocations.get(observableField));
			}

		}

		return  bound;

	}

	private boolean bindObserver(Element element, IJExpression observableInvocation) {

		IJExpression observerGetMethod = invoke(observerHolder.getObserverMethodFor(element));

		//Register the observer
		JBlock block;
		if (holder() instanceof MethodInjectionHandler) {
			block = ((MethodInjectionHandler) this).getInvocationBlock(element, holder());
		} else {
			block = holder().getInitBodyAfterInjectionBlock();
		}

		//Different behaviors depending on the injected element holder class type
		if (isSubtype(holder().getAnnotatedElement(), LIFECYCLE_OWNER, environment().getProcessingEnvironment())) {
			block.add(observableInvocation.invoke("observe").arg(_this()).arg(observerGetMethod));
		} else if (isSubtype(holder().getAnnotatedElement(), VIEW_MODEL, environment().getProcessingEnvironment())) {
			block.add(observableInvocation.invoke("observeForever").arg(observerGetMethod));

			//Remove the observer when the ViewModel is not needed anymore
			EViewModelHolder viewModelHolder = holder().getPluginHolder(new EViewModelHolder((EBeanHolder) holder()));
			viewModelHolder.getOnClearedMethodBlock().add(invoke(observerHolder.getRemoveObserverMethod()).arg(observerGetMethod));

			observerHolder.getRemoveObserverMethod().body()
					._if(observerHolder.getRemoveObserverMethodParam().eq(observerGetMethod))._then()
					.add(observableInvocation.invoke("removeObserver").arg(observerHolder.getRemoveObserverMethodParam()));

		} else {

			//It is assumed that a validation was done to ensure the user is aware of a "forever" observer
			//This validation should have been done in the ObserverHandler
			block.add(observableInvocation.invoke("observeForever").arg(observerGetMethod));

		}

		return true;

	}

	private boolean bindViewObserver(String viewReference, TypeMirror viewType, String propertyName,
								  VariableElement observableField, IJExpression observableInvocation,
								  Element element) {

		AbstractJClass observableClass = codeModelHelper.elementTypeToJClass(observableField, true);

		JMethod observerMethod = observerHolder.getObserverMethodFor(
				element, propertyName ==  null? "Default" : propertyName, observableClass);

		JBlock observerBody = observerHolder.getObserverMethodBodyFor(
				element, propertyName ==  null? "Default" : propertyName, observableClass);

		JVar observerParam = observerHolder.getObserverMethodParamFor(
				element, propertyName ==  null? "Default" : propertyName, observableClass);

		assignProperty(viewReference, viewType, propertyName, observableField, observerParam, observerBody);

		JBlock block = holder().getOnViewChangedBodyAfterInjectionBlock();
		block.add(observableInvocation.invoke("observe").arg(_this()).arg(invoke(observerMethod)));

		return true;

	}

	private void assignProperty(String viewReference, TypeMirror viewType, String propertyName,
								VariableElement observableField, JVar param, JBlock block) {

		String observableClassName = codeModelHelper.elementTypeToJClass(observableField, true).name();
		IJExpression viewRef = ref(viewReference);

		if (propertyName == null) {

			//CompoundButtons, if the param is boolean, it will set the checked state
			if (isSubtype(viewType, COMPOUND_BUTTON, environment().getProcessingEnvironment())) {
				if (observableClassName.equals("boolean") || observableClassName.equals(BOOLEAN)) {
					block.add(invoke(viewRef, "setChecked").arg(param));

					//This ensures not to check against TextView (since a CompoundButton is a TextView descendant)
					return;
				}
			}

			if (isSubtype(viewType, TEXT_VIEW, environment().getProcessingEnvironment())) {
				if (isSubtype(observableClassName, "android.text.Spanned", environment().getProcessingEnvironment())) {
					block.add(invoke(viewRef, "setText").arg(param));
				} else if (isSubtype(observableClassName, CHAR_SEQUENCE, environment().getProcessingEnvironment())) {
					block.add(invoke(viewRef, "setText").arg(param));
				} else {
					block.add(invoke(viewRef, "setText").arg(getJClass(String.class).staticInvoke("valueOf").arg(param)));
				}
			}

		} else {

			block.add(invoke(viewRef, "set" + propertyName).arg(param));

		}

	}

	private IJExpression getExpressionForField(VariableElement field) {
 		return ref(field.getSimpleName().toString());
	}

	private void exploreObservablesAndBindingMethodsIn(TypeElement typeElement, IJExpression invocation) {

		//Find observables
		List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
		for (VariableElement field : fields) {

			Observable observable = field.getAnnotation(Observable.class);
			if (observable != null) {
				observableFieldInvocations.put(
						field,
						invocation.invoke(fieldToGetter(field.getSimpleName().toString()))
				);
			}

		}

	}

	private String wrapperToPrimitive(String wrapper) {
		if (wrapper.equals(Boolean.class.getCanonicalName())) return "boolean";
		if (wrapper.equals(Integer.class.getCanonicalName())) return "int";
		if (wrapper.equals(Short.class.getCanonicalName())) return "short";
		if (wrapper.equals(Long.class.getCanonicalName())) return "long";
		if (wrapper.equals(Character.class.getCanonicalName())) return "char";
		if (wrapper.equals(Byte.class.getCanonicalName())) return "byte";
		if (wrapper.equals(Float.class.getCanonicalName())) return "float";
		if (wrapper.equals(Double.class.getCanonicalName())) return "double";
		return wrapper;
	}

}
