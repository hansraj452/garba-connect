package com.garbaconnect.repository;

import com.garbaconnect.domain.entity.Report;
import com.garbaconnect.domain.enums.ReportStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByReportedUser_IdAndStatus(UUID reportedUserId, ReportStatus status);

    List<Report> findByReporter_IdOrderByCreatedAtDesc(UUID reporterId);
}
