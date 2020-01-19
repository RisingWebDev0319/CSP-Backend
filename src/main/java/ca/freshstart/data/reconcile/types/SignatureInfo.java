package ca.freshstart.data.reconcile.types;

import lombok.Data;

@Data
public class SignatureInfo {

    private String name;

    private Long[] events;
}
