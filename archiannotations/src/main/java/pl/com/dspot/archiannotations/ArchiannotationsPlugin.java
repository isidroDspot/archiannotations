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
package pl.com.dspot.archiannotations;

import org.androidannotations.AndroidAnnotationsEnvironment;
import org.androidannotations.handler.AnnotationHandler;
import org.androidannotations.plugin.AndroidAnnotationsPlugin;
import pl.com.dspot.archiannotations.handler.*;

import java.util.LinkedList;
import java.util.List;

public class ArchiannotationsPlugin extends AndroidAnnotationsPlugin {

    @Override
    public String getName() {
        return "archiannotations";
    }

    @Override
    public List<AnnotationHandler<?>> getHandlers(AndroidAnnotationsEnvironment androidAnnotationEnv) {

        List<AnnotationHandler<?>> handlers = new LinkedList<>();

        handlers.add(new EViewModelHandler(androidAnnotationEnv));

        handlers.add(new ObservableHandler(androidAnnotationEnv));
        handlers.add(new ObserverHandler(androidAnnotationEnv));

        handlers.add(new EViewPresenterHandler(androidAnnotationEnv));
        handlers.add(new PresenterMethodHandler(androidAnnotationEnv));

        handlers.add(new ViewModelHandler(androidAnnotationEnv));
        handlers.add(new RootViewHandler(androidAnnotationEnv));

        return handlers;
    }

}
