package com.dspot.declex.architecture.handler;

import com.dspot.declex.architecture.annotation.EViewPresenter;
import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.ElementValidation;
import org.androidannotations.handler.BaseAnnotationHandler;
import org.androidannotations.holder.EBeanHolder;

import javax.lang.model.element.Element;

public class EViewPresenterHandler extends BaseAnnotationHandler<EBeanHolder> {

    public EViewPresenterHandler(AndroidAnnotationsEnvironment environment) {
        super(EViewPresenter.class, environment);
    }

    @Override
    public void validate(Element element, ElementValidation valid) {}

    @Override
    public void process(Element element, EBeanHolder holder) {

//        ViewPresenterHolder viewPresenterHolder = holder.getPluginHolder(new ViewPresenterHolder(holder));
//
//        viewPresenterHolder.getConstructorMethod();
//
//        viewPresenterHolder.getPresenterLiveDataField();
//
//        viewPresenterHolder.getRebindMethod();

    }

}
