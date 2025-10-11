package com.example.booking.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

public class ReviewReportForm {

    @NotBlank(message = "Vui lòng nhập lý do báo cáo")
    @Size(max = 1000, message = "Lý do báo cáo không được vượt quá 1000 ký tự")
    private String reasonText;

    private List<MultipartFile> evidenceFiles = new ArrayList<>();

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public List<MultipartFile> getEvidenceFiles() {
        return evidenceFiles;
    }

    public void setEvidenceFiles(List<MultipartFile> evidenceFiles) {
        this.evidenceFiles = evidenceFiles;
    }
}

