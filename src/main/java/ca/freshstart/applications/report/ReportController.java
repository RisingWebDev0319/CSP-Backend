package ca.freshstart.applications.report;

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.client.repository.ClientRepository;
import ca.freshstart.data.report.entity.Report;
import ca.freshstart.data.report.entity.ReportTableItem;
import ca.freshstart.data.report.enums.ReportType;
import ca.freshstart.data.report.repository.ReportRepository;
import ca.freshstart.data.report.services.ReportService;
import ca.freshstart.data.suggestions.entity.PreliminaryEvent;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.types.AbstractController;
import ca.freshstart.types.Count;
import ca.freshstart.types.IdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ReportController extends AbstractController {
    private final ReportService reportService;

    @RequestMapping(value = "/reports/byKey/{reportKey}", method = RequestMethod.GET)
    public Report getReportByKey(@PathVariable("reportKey") String reportKey) {

        return reportService.findByKeyHash(reportKey)
                .orElseThrow(() -> new NotFoundException("This report not found"));
    }

    @RequestMapping(value = "/reports", method = RequestMethod.GET)
    @Secured("ROLE_REPORTS")
    public List<Report> getList(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                @RequestParam(value = "sort", required = false) String sort) {
        return reportService.getList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    @RequestMapping(value = "/reports/count", method = RequestMethod.GET)
    @Secured("ROLE_REPORTS")
    public Count reportsCount() {
        return reportService.getCount();
    }

    /**
     * Get data for table representation
     *
     * @param {STRING} reportKey
     * @return
     */
    @RequestMapping(value = "/reports/table/{reportKey}", method = RequestMethod.GET)
    public List <ReportTableItem>getReportTable(@PathVariable("reportKey") String reportKey) {

        Report report = reportService.findByKeyHash(reportKey)
                .orElseThrow(() -> new NotFoundException("Report not found"));

        switch (report.getType()) {
            case CLIENTS:
                return reportService.getReportByClient(report);
            case ROOMS:
                return reportService.getReportByRoom(report);
            case THERAPISTS:
                return reportService.getReportByTherapist(report);
            default:
                return new ArrayList<>();
        }
    }

    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.GET)
    @Secured("ROLE_REPORTS")
    public Report getReportById(@PathVariable("reportId") Long reportId) {

        return reportService.findById(reportId)
                .orElseThrow(() -> new NotFoundException("No such Report"));
    }

    @RequestMapping(value = "/reports", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    public void createReport(@RequestBody List<Report> report) {
       reportService.createReport(report);
    }

    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.PUT)
    @Secured("ROLE_REPORTS")
    public void updateReport(@PathVariable("reportId") Long reportId,
                             @RequestBody Report report) {

        reportService.findById(reportId)
                .orElseThrow(() -> new NotFoundException("No such Report"));

        reportService.saveReport(report);
    }

    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.DELETE)
    @Secured("ROLE_REPORTS")
    public void deleteReport(@PathVariable("reportId") Long reportId) {
        Report report = reportService.findById(reportId)
                .orElseThrow(() -> new NotFoundException("No such Report"));

        report.setArchived(true);
        reportService.saveReport(report);
    }
}
