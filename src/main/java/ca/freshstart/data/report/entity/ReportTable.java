package ca.freshstart.data.report.entity;

import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class ReportTable {
    private Date dateFrom;
    private Date dateTo;

    private boolean autoupdate;

    private Set<ReportTableItem> items;
}
