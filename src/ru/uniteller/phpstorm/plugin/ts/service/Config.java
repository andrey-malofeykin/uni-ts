package ru.uniteller.phpstorm.plugin.ts.service;

/**
 * Настройки плагина
 */
public class Config {
    /**
     * Имя интерфеса который должны реализовывать все субъекты
     */
    private String subjectInterfaceFQN = "\\TestSrv\\Lib\\Subject\\SubjectInterface";

    /**
     * Пространство имен в котором расположены субъекты
     */
    private String subjectNamespace = "\\TestSrv\\Subject\\";

    /**
     * Пространство имен в котором расположены субъекты
     */
    private String objInterface = "\\TestSrv\\Lib\\Domain\\ObjectInterface";

    /**
     * Интерфейс который реализуют классы доменов субъектов
     */
    private String domainInterface = "\\TestSrv\\Lib\\Domain\\DomainInterface";

    /**
     * Имя интерфеса который должны реализовывать все субъекты
     */
    public String getSubjectInterfaceFQN() {
        return subjectInterfaceFQN;
    }

    /**
     * Пространство имен в котором расположены субъекты
     */
    public String getSubjectNamespace() {
        return subjectNamespace;
    }

    /**
     * ИНтерфейс который реализуют объекты
     */
    public String getObjInterface() {
        return objInterface;
    }

    /**
     * Интерфейс который реализуют классы доменов субъектов
     */
    public String getDomainInterface() {
        return domainInterface;
    }
}
