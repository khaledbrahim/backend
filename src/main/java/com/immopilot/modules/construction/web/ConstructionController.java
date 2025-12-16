package com.immopilot.modules.construction.web;

import com.immopilot.modules.construction.service.ConstructionService;
import com.immopilot.modules.construction.service.dto.ConstructionLotDTO;
import com.immopilot.modules.construction.service.dto.ConstructionProgressLogDTO;
import com.immopilot.modules.construction.service.dto.ConstructionProjectDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/construction")
@RequiredArgsConstructor
public class ConstructionController {

    private final ConstructionService constructionService;

    // --- Projects ---

    @GetMapping("/projects")
    public ResponseEntity<List<ConstructionProjectDTO>> getProjects(@RequestParam Long propertyId) {
        return ResponseEntity.ok(constructionService.getProjectsByProperty(propertyId));
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<ConstructionProjectDTO> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(constructionService.getProject(id));
    }

    @PostMapping("/projects")
    public ResponseEntity<ConstructionProjectDTO> createProject(@RequestParam Long propertyId,
                                                                @RequestBody ConstructionProjectDTO request) {
        return ResponseEntity.ok(constructionService.createProject(propertyId, request));
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<ConstructionProjectDTO> updateProject(@PathVariable Long id,
                                                                @RequestBody ConstructionProjectDTO request) {
        return ResponseEntity.ok(constructionService.updateProject(id, request));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        constructionService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects/{id}/close")
    public ResponseEntity<Void> closeProject(@PathVariable Long id) {
        constructionService.closeProject(id);
        return ResponseEntity.ok().build();
    }

    // --- Lots ---

    @GetMapping("/lots")
    public ResponseEntity<List<ConstructionLotDTO>> getLots(@RequestParam Long projectId) {
        return ResponseEntity.ok(constructionService.getLotsByProject(projectId));
    }

    @PostMapping("/lots")
    public ResponseEntity<ConstructionLotDTO> createLot(@RequestParam Long projectId,
                                                        @RequestBody ConstructionLotDTO request) {
        return ResponseEntity.ok(constructionService.createLot(projectId, request));
    }

    @PutMapping("/lots/{id}")
    public ResponseEntity<ConstructionLotDTO> updateLot(@PathVariable Long id,
                                                        @RequestBody ConstructionLotDTO request) {
        return ResponseEntity.ok(constructionService.updateLot(id, request));
    }

    @DeleteMapping("/lots/{id}")
    public ResponseEntity<Void> deleteLot(@PathVariable Long id) {
        constructionService.deleteLot(id);
        return ResponseEntity.noContent().build();
    }

    // --- Logs ---

    @GetMapping("/logs")
    public ResponseEntity<List<ConstructionProgressLogDTO>> getLogs(@RequestParam Long lotId) {
        return ResponseEntity.ok(constructionService.getLogsByLot(lotId));
    }

    @PostMapping("/logs")
    public ResponseEntity<ConstructionProgressLogDTO> addLog(@RequestParam Long lotId,
                                                             @RequestBody ConstructionProgressLogDTO request) {
        return ResponseEntity.ok(constructionService.addLog(lotId, request));
    }
}
