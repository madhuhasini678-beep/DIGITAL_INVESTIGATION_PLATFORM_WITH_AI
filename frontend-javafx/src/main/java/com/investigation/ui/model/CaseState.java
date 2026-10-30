package com.investigation.ui.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class CaseState {

    private static final CaseState INSTANCE = new CaseState();

    private final ObjectProperty<CaseModel> activeCase = new SimpleObjectProperty<>(null);

    private CaseState() {}

    public static CaseState getInstance() {
        return INSTANCE;
    }

    public ObjectProperty<CaseModel> activeCaseProperty() {
        return activeCase;
    }

    public CaseModel getActiveCase() {
        return activeCase.get();
    }

    public void setActiveCase(CaseModel caseModel) {
        this.activeCase.set(caseModel);
    }
}
