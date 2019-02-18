package ru.uniteller.phpstorm.plugin.ts.service;

import java.util.regex.Pattern;

/**
 * Настройки плагина
 */
public class Config {
    /**
     * Интерфейс которые реализуют все тесты из TS 2.0
     */
    private String testInterfaceFQN = "\\TestSrv\\Lib\\TestEngine\\TestInterface";

    /**
     * Имя группы в шаблоне для получения имени субъекта
     * @see Config#patternObjInfo
     */
    private String subjectGroupName =  "SUBJECT";
    /**
     * Имя группы в шаблоне для получения имени объекта
     * @see Config#patternObjInfo
     */
    private String objGroupName =  "OBJ";

    /**
     * Шаблон для получения имени субъекта и объекта из FQN объекта
     */
    private Pattern patternObjInfo = Pattern.compile("\\\\TestSrv\\\\Subject\\\\(?<SUBJECT>.+)\\\\Object\\\\(?<OBJ>.+)");

    /**
     * Имя групыы из шаблона patternSearchCriteria (@see ru.uniteller.phpstorm.plugin.ts.service.Config#patternSearchCriteria)
     * для получения имени аргумента метода для которого в see док блоке описывается интерфейс с данными о возможных
     * значениях
     */
    private String searchCriteriaGroupName = "CRITERIA";

    /**
     * Шаблон для имени для критерия поиска
     */
    private Pattern patternSearchCriteria = Pattern.compile(" *- *значени[яе] +для +\\$(?<CRITERIA>.+) *");

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

    /**
     * Имя группы в шаблоне для получения имени субъекта
     * @see Config#patternObjInfo
     */
    public String getSubjectGroupName() {
        return subjectGroupName;
    }
    /**
     * Имя группы в шаблоне для получения имени объекта
     * @see Config#patternObjInfo
     */
    public String getObjGroupName() {
        return objGroupName;
    }
    /**
     * Шаблон для получения имени субъекта и объекта из FQN объекта
     */
    public Pattern getPatternObjInfo() {
        return patternObjInfo;
    }

    /**
     * Имя групыы из шаблона patternSearchCriteria (@see ru.uniteller.phpstorm.plugin.ts.service.Config#patternSearchCriteria)
     * для получения имени аргумента метода для которого в see док блоке описывается интерфейс с данными о возможных
     * значениях
     */
    public String getSearchCriteriaGroupName() {
        return searchCriteriaGroupName;
    }

    /**
     * Шаблон для имени для критерия поиска
     */
    public Pattern getPatternSearchCriteria() {
        return patternSearchCriteria;
    }

    /**
     * Интерфейс которые реализуют все тесты из TS 2.0
     */
    public String getTestInterfaceFQN() {
        return testInterfaceFQN;
    }
}
