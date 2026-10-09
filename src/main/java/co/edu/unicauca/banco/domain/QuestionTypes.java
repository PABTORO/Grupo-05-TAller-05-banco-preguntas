package co.edu.unicauca.banco.domain;

import java.util.List;

/** Identificadores de tipo de pregunta que entienden los plugins incluidos. */
public final class QuestionTypes {

    public static final String MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
    public static final String CASE_BASED = "CASE_BASED";
    public static final String MULTIMEDIA = "MULTIMEDIA";

    public static final List<String> ALL = List.of(MULTIPLE_CHOICE, CASE_BASED, MULTIMEDIA);

    private QuestionTypes() { }
}
