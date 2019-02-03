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
package pl.com.dspot.archiannotations.helper;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import org.androidannotations.AndroidAnnotationsEnvironment;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

public class CompilationTreeHelper {

    private AndroidAnnotationsEnvironment environment;
    private ProcessingEnvironment processingEnvironment;
    private final Trees trees;

    public CompilationTreeHelper(AndroidAnnotationsEnvironment environment) {
        super();
        this.environment = environment;
        this.processingEnvironment = environment.getProcessingEnvironment();
        this.trees = Trees.instance(environment.getProcessingEnvironment());
    }

    @Nullable
    public CompilationUnitTree getCompilationUnitImportFromElement(Element referenceElement) {

        final TreePath treePath = trees.getPath(referenceElement);

        if (treePath == null) return null;

        return treePath.getCompilationUnit();

    }

    public String getClassNameFromCompilationUnitImports(String className, Element referenceElement) {

        if (referenceElement == null
                || processingEnvironment.getElementUtils().getTypeElement(className) != null) {

            return className;

        }

        CompilationUnitTree compilationUnit = getCompilationUnitImportFromElement(referenceElement);
        if (compilationUnit == null) return className;

        //TODO Handle imports with "*"

        for (ImportTree importTree : compilationUnit.getImports()) {

            String lastElementImport = importTree.getQualifiedIdentifier().toString();
            String firstElementName = className;
            String currentVariableClass = "";

            int pointIndex = lastElementImport.lastIndexOf('.');
            if (pointIndex != -1) {
                lastElementImport = lastElementImport.substring(pointIndex + 1);
            }

            pointIndex = firstElementName.indexOf('.');
            if (pointIndex != -1) {
                firstElementName = firstElementName.substring(0, pointIndex);
                currentVariableClass = className.substring(pointIndex);
            }

            while (firstElementName.endsWith("[]")) {
                firstElementName = firstElementName.substring(0, firstElementName.length()-2);
                if (currentVariableClass.isEmpty()) currentVariableClass = currentVariableClass + "[]";
            }

            if (lastElementImport.equals(firstElementName)) {
                return importTree.getQualifiedIdentifier() + currentVariableClass;
            }

        }

        //If the class is not referenced in the imports, then the class probably is in the same package
        return compilationUnit.getPackageName() + "." + className;

    }

    public void visitElementTree(Element element, TreePathScanner<Boolean, Trees> scanner) {
        final TreePath treePath = trees.getPath(element);
        scanner.scan(treePath, trees);
    }

}
