package com.immopilot.modules.properties.service;

import com.immopilot.infrastructure.storage.StorageService;
import com.immopilot.modules.properties.domain.DocumentCategory;
import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.domain.PropertyDocument;
import com.immopilot.modules.properties.repository.PropertyDocumentRepository;
import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.properties.service.dto.DocumentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    // Limits
    private static final long MAX_SIZE_FREE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_DOCS_FREE = 10;
    private final PropertyDocumentRepository documentRepository;
    private final PropertyRepository propertyRepository;
    private final StorageService storageService;

    @Transactional
    public DocumentDTO uploadDocument(Long userId, Long propertyId, MultipartFile file, DocumentCategory category) {
        Property property = propertyRepository.findByIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        // Check limits (Mock logic for plan check)
        // if Free plan...
        if (file.getSize() > MAX_SIZE_FREE) {
            throw new RuntimeException("File too large for Free plan");
        }
        if (documentRepository.countByPropertyId(propertyId) >= MAX_DOCS_FREE) {
            throw new RuntimeException("Document limit reached for this property");
        }

        String storageKey = storageService.store(file);

        PropertyDocument doc = PropertyDocument.builder()
                .property(property)
                .filename(file.getOriginalFilename())
                .storageKey(storageKey)
                .contentType(file.getContentType())
                .size(file.getSize())
                .category(category)
                .build();

        // Auto-set main photo if it doesn't exist and this is a photo
        if (category == DocumentCategory.PHOTO
                && (property.getMainPhotoUrl() == null || property.getMainPhotoUrl().isEmpty())) {
            property.setMainPhotoUrl("/api/documents/download/" + storageKey); // Mock URL logic
            propertyRepository.save(property);
        }

        return mapToDTO(documentRepository.save(doc));
    }

    public List<DocumentDTO> getDocuments(Long userId, Long propertyId) {
        // Validate ownership
        propertyRepository.findByIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        return documentRepository.findByPropertyId(propertyId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public void deleteDocument(Long userId, Long propertyId, Long documentId) {
        PropertyDocument doc = documentRepository.findByIdAndPropertyId(documentId, propertyId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // delete from storage
        storageService.delete(doc.getStorageKey());
        documentRepository.delete(doc);
    }

    private DocumentDTO mapToDTO(PropertyDocument d) {
        return DocumentDTO.builder()
                .id(d.getId())
                .propertyId(d.getProperty().getId())
                .filename(d.getFilename())
                .contentType(d.getContentType())
                .size(d.getSize())
                .category(d.getCategory())
                .url("/api/documents/download/" + d.getStorageKey()) // Mock URL
                .build();
    }
}
