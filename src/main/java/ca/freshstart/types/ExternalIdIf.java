package ca.freshstart.types;


public interface ExternalIdIf<ID> {
    ID getExternalId();

    void setExternalId(ID externalId);
}
