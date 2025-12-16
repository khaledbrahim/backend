package com.immopilot.modules.properties.web;

import com.immopilot.modules.properties.domain.DocumentCategory;
import com.immopilot.modules.properties.service.DocumentService;
import com.immopilot.modules.properties.service.dto.DocumentDTO;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/properties/{propertyId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<DocumentDTO> uploadDocument(
            @PathVariable("propertyId") Long propertyId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") DocumentCategory category,
            Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(documentService.uploadDocument(user.getId(), propertyId, file, category));
    }

    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getDocuments(@PathVariable("propertyId") Long propertyId,
                                                          Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(documentService.getDocuments(user.getId(), propertyId));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable("propertyId") Long propertyId,
            @PathVariable("documentId") Long documentId,
            Authentication authentication) {
        User user = getUser(authentication);
        documentService.deleteDocument(user.getId(), propertyId, documentId);
        return ResponseEntity.noContent().build();
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
