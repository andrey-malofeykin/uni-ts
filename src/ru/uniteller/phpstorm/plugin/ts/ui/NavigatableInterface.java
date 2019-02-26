package ru.uniteller.phpstorm.plugin.ts.ui;

import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.Nullable;

public interface NavigatableInterface {
    @Nullable
    Navigatable getNavigatable();
}
