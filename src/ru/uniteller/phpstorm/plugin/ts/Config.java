package ru.uniteller.phpstorm.plugin.ts;

/**
 * Конфиги
 */
public class Config {
    /**
     * Интерфейс который должны реализовывать все субъекты
     */
    private String subjectInterface = "\\TestSrv\\Lib\\Subject\\SubjectInterface";

    /**
     * Интерфейс который должны реализовывать все субъекты
     */
    public String getSubjectInterface() {
        return subjectInterface;
    }
}
