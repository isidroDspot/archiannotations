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
package pl.com.dspot.archiannotations.viewmodel;

import org.androidannotations.annotations.EActivity;

import pl.com.dspot.archiannotations.annotation.ViewModel;
import android.support.v4.app.FragmentActivity;

@EActivity
public class SomeFragmentActivity extends FragmentActivity {

    @ViewModel
    SomeViewModel viewModel;

    @ViewModel
    SomeAndroidViewModel androidViewModel;

    @ViewModel
    SomeViewModelActivityScoped activityScoped;

    @ViewModel
    SomeViewModelWhichInjectsViewModel injectsViewModel;

    @ViewModel
    void injectViewModelMethod(SomeViewModel someViewModel) {

    }

    void injectViewModelParam(@ViewModel SomeViewModel someViewModel) {

    }

    void injectViewModelMultiParam(@ViewModel SomeViewModel someViewModel, @ViewModel SomeAndroidViewModel someAndroidViewModel) {

    }

}
