package com.immopilot.modules.properties.web;

import com.immopilot.modules.properties.domain.PropertyStatus;
import com.immopilot.modules.properties.domain.PropertyType;
import com.immopilot.modules.properties.service.PropertyService;
import com.immopilot.modules.properties.service.dto.CreatePropertyRequest;
import com.immopilot.modules.properties.service.dto.PropertyDTO;
import com.immopilot.modules.properties.service.dto.PropertyUnitDTO;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<PropertyDTO> createProperty(@RequestBody CreatePropertyRequest request,
                                                      Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(propertyService.createProperty(user.getId(), request));
    }

    @GetMapping
    public ResponseEntity<Page<PropertyDTO>> getProperties(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PropertyType type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) PropertyStatus status,
            Pageable pageable,
            Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(propertyService.getProperties(user.getId(), search, type, city, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDTO> getProperty(@PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(propertyService.getProperty(user.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyDTO> updateProperty(@PathVariable Long id,
                                                      @RequestBody CreatePropertyRequest request,
                                                      Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(propertyService.updateProperty(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id, Authentication authentication) {
        User user = getUser(authentication);
        propertyService.deleteProperty(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/main-photo")
    public ResponseEntity<PropertyDTO> setMainPhoto(@PathVariable Long id,
                                                    @RequestParam Long documentId,
                                                    Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(propertyService.setMainPhoto(user.getId(), id, documentId));
    }

    @GetMapping("/{id}/units")
    public ResponseEntity<List<PropertyUnitDTO>> getPropertyUnits(@PathVariable Long id,
                                                                  Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(propertyService.getPropertyUnits(user.getId(), id));
    }

    @PostMapping("/{id}/units")
    public ResponseEntity<PropertyUnitDTO> createUnit(@PathVariable Long id,
                                                      @RequestBody PropertyUnitDTO request,
                                                      Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(propertyService.createUnit(user.getId(), id, request));
    }

    @PutMapping("/units/{unitId}")
    public ResponseEntity<PropertyUnitDTO> updateUnit(@PathVariable Long unitId,
                                                      @RequestBody PropertyUnitDTO request,
                                                      Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(propertyService.updateUnit(user.getId(), unitId, request));
    }

    @DeleteMapping("/units/{unitId}")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long unitId,
                                           Authentication authentication) {
        User user = getUser(authentication);
        propertyService.deleteUnit(user.getId(), unitId);
        return ResponseEntity.noContent().build();
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
