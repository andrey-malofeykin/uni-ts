package ru.uniteller.phpstorm.plugin.ts.util;


import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;

import java.util.function.Consumer;

public class PhpPsiUtil {
    public static void iterateArrayOfArray(PhpPsiElement elem, Consumer<? super ArrayCreationExpression> action) {
        for (PsiElement nodeValueArray = elem.getFirstChild(); nodeValueArray != null; nodeValueArray = nodeValueArray.getNextSibling()) {
            if (!(nodeValueArray.getNode() instanceof CompositeElement) && nodeValueArray.getNode().getElementType() != PhpElementTypes.ARRAY_VALUE) {
                continue;
            }
            for (PsiElement childNode = nodeValueArray.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
                if (!(childNode instanceof ArrayCreationExpression)) {
                    continue;
                }
                action.accept((ArrayCreationExpression)childNode);
            }

        }
    }



}
