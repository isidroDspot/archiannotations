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
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JMethod;
import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.helper.ModelConstants;
import org.androidannotations.holder.EComponentHolder;
import pl.com.dspot.archiannotations.annotation.Observable;
import pl.com.dspot.archiannotations.helper.CompilationTreeHelper;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static com.helger.jcodemodel.JExpr._new;
import static com.helger.jcodemodel.JExpr._this;
import static com.helger.jcodemodel.JMod.PUBLIC;
import static org.androidannotations.helper.ModelConstants.generationSuffix;
import static pl.com.dspot.archiannotations.ArchiCanonicalNameConstants.LIVE_DATA;
import static pl.com.dspot.archiannotations.util.ElementUtils.isSubtype;
import static pl.com.dspot.archiannotations.util.FormatUtils.fieldToGetter;

public class ObservableHandler extends BaseAnnotationHandler<EComponentHolder> {

    private final CompilationTreeHelper compilationTreeHelper;

    public ObservableHandler(AndroidAnnotationsEnvironment environment) {
        super(Observable.class, environment);
        this.compilationTreeHelper = new CompilationTreeHelper(environment);
    }

    @Override
    public void validate(Element element, ElementValidation validation) {
        if (!isSubtype(element, LIVE_DATA, getProcessingEnvironment())) {
            validation.addError("%s can only be used on an element that extends " + LIVE_DATA);
        }
    }

    @Override
    public void process(Element element, EComponentHolder holder) {

        final String fieldName = element.getSimpleName().toString();
        JFieldRef fieldRef = _this().ref(element.getSimpleName().toString());

        //Create the class if it wasn't initialized
        Observable observable = element.getAnnotation(Observable.class);

        //Inject the class if normal constructor found
        if (observable.initialize() && !observable.lazyLoad()) {
            initializeInBlock(element, fieldRef, holder.getInitBodyInjectionBlock()._if(fieldRef.eqNull())._then());
        }

        //Create Getter
        AbstractJClass LiveData = liveDataTypeOf(element);
        JMethod getterMethod = holder.getGeneratedClass().method(PUBLIC, LiveData, fieldToGetter(fieldName));

        if (observable.lazyLoad()) {
            initializeInBlock(element, fieldRef, getterMethod.body()._if(fieldRef.eqNull())._then());
        }

        getterMethod.body()._return(_this().ref(fieldName));

    }

    private void initializeInBlock(Element element, JFieldRef fieldRef, JBlock block) {

        String liveDataClassName = element.asType().toString();
        if (liveDataClassName.contains("<")) {
            liveDataClassName = liveDataClassName.substring(0, liveDataClassName.indexOf("<"));
        }

        TypeElement typeElement = getProcessingEnvironment().getElementUtils().getTypeElement(liveDataClassName);

        if (canBeInitialized(typeElement)) {
            AbstractJClass LiveData = liveDataTypeOf(element);
            block.assign(fieldRef, _new(LiveData));
        }
    }

    private boolean canBeInitialized(TypeElement typeElement) {
        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) return false;
        return true;
    }

    private AbstractJClass liveDataTypeOf(Element element) {

        String liveDataClassName = element.asType().toString();

        if (liveDataClassName.contains("<")) {
            String liveDataTypeName = liveDataClassName.substring(liveDataClassName.indexOf("<") + 1, liveDataClassName.length() - 1);
            liveDataClassName = liveDataClassName.substring(0, liveDataClassName.indexOf("<"));
            liveDataTypeName = compilationTreeHelper.getClassNameFromCompilationUnitImports(liveDataTypeName, element);

            return getJClass(liveDataClassName).narrow(getJClass(liveDataTypeName));
        }

        //TODO
        throw new IllegalStateException("State not supported");

    }

}
