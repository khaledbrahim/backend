package com.immopilot.modules.properties.web;

import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.domain.PropertyType;
import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.properties.web.dto.PublicPropertyDTO;
import com.immopilot.modules.properties.web.dto.PublicPropertyDetailDTO;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/public/properties")
@RequiredArgsConstructor
public class PublicPropertyController {

    private final PropertyRepository propertyRepository;

    @GetMapping
    public Page<PublicPropertyDTO> getPublicProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) PropertyType type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minArea,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Specification<Property> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (city != null && !city.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("propertyType"), type));
            }
            if (minPrice != null) {
                // Assuming price field. If rental, logic might differ but keeping simple.
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (minArea != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("area"), minArea));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Property> propertiesInfo = propertyRepository.findAll(spec, pageable);

        return propertiesInfo.map(this::mapToDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicPropertyDetailDTO> getPropertyDetail(@PathVariable Long id) {
        return propertyRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .map(this::mapToDetailDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private PublicPropertyDTO mapToDTO(Property property) {
        return PublicPropertyDTO.builder()
                .id(property.getId())
                .title(property.getName())
                .description(property.getDescription()) // Maybe truncate for list?
                .city(property.getCity())
                .zipCode(property.getZipCode())
                .type(property.getPropertyType())
                .status(property.getStatus())
                .area(property.getArea())
                .rooms(property.getNumberOfRooms())
                .price(property.getPrice())
                .mainPhotoUrl(property.getMainPhotoUrl())
                .build();
    }

    private PublicPropertyDetailDTO mapToDetailDTO(Property property) {
        return PublicPropertyDetailDTO.builder()
                .id(property.getId())
                .title(property.getName())
                .description(property.getDescription())
                .city(property.getCity())
                .zipCode(property.getZipCode())
                .type(property.getPropertyType())
                .status(property.getStatus())
                .area(property.getArea())
                .rooms(property.getNumberOfRooms())
                .price(property.getPrice())
                .mainPhotoUrl(property.getMainPhotoUrl())
                .constructionYear(property.getConstructionYear())
                .country(property.getCountry())
                .build();
    }
}
