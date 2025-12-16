package com.immopilot.modules.properties.service.dto;

import com.immopilot.modules.properties.domain.DocumentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private Long propertyId;
    private String filename;
    private String contentType;
    private Long size;
    private DocumentCategory category;
    private String url;
}
