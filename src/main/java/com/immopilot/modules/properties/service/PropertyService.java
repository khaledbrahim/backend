package com.immopilot.modules.properties.service;

import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.domain.PropertyStatus;
import com.immopilot.modules.properties.domain.PropertyType;
import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.properties.service.dto.CreatePropertyRequest;
import com.immopilot.modules.properties.service.dto.PropertyDTO;
import com.immopilot.modules.properties.service.dto.PropertyUnitDTO;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.repository.UserRepository;
import com.immopilot.modules.users.repository.UserSubscriptionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    public static final String PROPERTY_NOT_FOUND = "Property not found";
    public static final String UNIT_NOT_FOUND = "Unit not found";
    private static final int MAX_PROPERTIES_FREE = 2;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final com.immopilot.modules.properties.repository.PropertyDocumentRepository propertyDocumentRepository;
    private final com.immopilot.modules.properties.repository.PropertyUnitRepository propertyUnitRepository;

    @Transactional
    public PropertyDTO createProperty(Long userId, CreatePropertyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (isFreePlan(userId) && propertyRepository.countByUserId(userId) >= MAX_PROPERTIES_FREE) {
            throw new RuntimeException("Property limit reached for FREE plan. Please upgrade.");
        }

        Property property = Property.builder()
                .user(user)
                .name(request.getName())
                .propertyType(request.getPropertyType())
                .rentalType(request.getRentalType())
                .ownershipType(request.getOwnershipType())
                .status(request.getStatus())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .area(request.getArea())
                .price(request.getPrice())
                .marketValue(request.getMarketValue())
                .acquisitionDate(request.getAcquisitionDate())
                .description(request.getDescription())
                .build();

        Property saved = propertyRepository.save(property);

        // Auto-create units for IMMEUBLE if requested
        if (request.getPropertyType() == PropertyType.IMMEUBLE &&
                request.getNumberOfUnits() != null && request.getNumberOfUnits() > 0) {

            for (int i = 1; i <= request.getNumberOfUnits(); i++) {
                com.immopilot.modules.properties.domain.PropertyUnit unit = com.immopilot.modules.properties.domain.PropertyUnit
                        .builder()
                        .property(saved)
                        .name("Lot " + i)
                        .type(PropertyType.APPARTEMENT)
                        .build();
                propertyUnitRepository.save(unit);
            }
        }
        return mapToDTO(saved);
    }

    public Page<PropertyDTO> getProperties(Long userId, String search, PropertyType type, String city,
                                           PropertyStatus status, Pageable pageable) {
        Specification<Property> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            predicates.add(cb.isFalse(root.get("deleted")));

            if (StringUtils.hasText(search)) {
                String searchLike = "%" + search.toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchLike);
                Predicate cityLike = cb.like(cb.lower(root.get("city")), searchLike);
                Predicate addressLike = cb.like(cb.lower(root.get("address")), searchLike);
                predicates.add(cb.or(nameLike, cityLike, addressLike));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("propertyType"), type));
            }

            if (StringUtils.hasText(city)) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return propertyRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    public PropertyDTO getProperty(Long userId, Long propertyId) {
        Property property = propertyRepository.findByIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new RuntimeException(PROPERTY_NOT_FOUND));
        if (property.isDeleted()) {
            throw new RuntimeException(PROPERTY_NOT_FOUND);
        }
        return mapToDTO(property);
    }

    @Transactional
    public PropertyDTO updateProperty(Long userId, Long propertyId, CreatePropertyRequest request) {
        Property property = propertyRepository.findByIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new RuntimeException(PROPERTY_NOT_FOUND));

        property.setName(request.getName());
        property.setPropertyType(request.getPropertyType());
        property.setRentalType(request.getRentalType());
        property.setOwnershipType(request.getOwnershipType());
        property.setMarketValue(request.getMarketValue());
        property.setStatus(request.getStatus());
        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setCountry(request.getCountry());
        property.setArea(request.getArea());
        property.setPrice(request.getPrice());
        property.setAcquisitionDate(request.getAcquisitionDate());
        property.setDescription(request.getDescription());

        Property updated = propertyRepository.save(property);
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteProperty(Long userId, Long propertyId) {
        Property property = propertyRepository.findByIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new RuntimeException(PROPERTY_NOT_FOUND));
        property.setDeleted(true);
        propertyRepository.save(property);
    }

    @Transactional
    public PropertyDTO setMainPhoto(Long userId, Long propertyId, Long documentId) {
        Property property = propertyRepository.findByIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new RuntimeException(PROPERTY_NOT_FOUND));

        var doc = propertyDocumentRepository.findByIdAndPropertyId(documentId, propertyId)
                .orElseThrow(() -> new RuntimeException("Document not found or does not belong to this property"));

        if (doc.getCategory() != com.immopilot.modules.properties.domain.DocumentCategory.PHOTO) {
            throw new RuntimeException("Selected document is not a photo");
        }

        property.setMainPhotoUrl("/api/documents/download/" + doc.getStorageKey());
        Property saved = propertyRepository.save(property);
        return mapToDTO(saved);
    }

    public List<PropertyUnitDTO> getPropertyUnits(Long userId, Long propertyId) {

        return propertyUnitRepository.findAllByPropertyId(propertyId).stream()
                .map(this::mapUnitToDTO)
                .toList();
    }

    @Transactional
    public PropertyUnitDTO createUnit(Long userId, Long propertyId, PropertyUnitDTO request) {
        Property property = propertyRepository.findByIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new RuntimeException(PROPERTY_NOT_FOUND));

        if (property.getPropertyType() != PropertyType.IMMEUBLE) {
            throw new RuntimeException("Units can only be added to IMMEUBLE properties");
        }

        com.immopilot.modules.properties.domain.PropertyUnit unit = com.immopilot.modules.properties.domain.PropertyUnit
                .builder()
                .property(property)
                .name(request.getName())
                .type(request.getType() != null ? request.getType() : PropertyType.APPARTEMENT)
                .area(request.getArea())
                .shares(request.getShares())
                .description(request.getDescription())
                .build();

        return mapUnitToDTO(propertyUnitRepository.save(unit));
    }

    @Transactional
    public PropertyUnitDTO updateUnit(Long userId, Long unitId, PropertyUnitDTO request) {
        com.immopilot.modules.properties.domain.PropertyUnit unit = propertyUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException(UNIT_NOT_FOUND));

        // Verify ownership via property
        if (!unit.getProperty().getUser().getId().equals(userId)) {
            throw new RuntimeException(UNIT_NOT_FOUND);
        }

        unit.setName(request.getName());
        if (request.getType() != null)
            unit.setType(request.getType());
        unit.setArea(request.getArea());
        unit.setShares(request.getShares());
        unit.setDescription(request.getDescription());

        return mapUnitToDTO(propertyUnitRepository.save(unit));
    }

    @Transactional
    public void deleteUnit(Long userId, Long unitId) {
        com.immopilot.modules.properties.domain.PropertyUnit unit = propertyUnitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException(UNIT_NOT_FOUND));

        if (!unit.getProperty().getUser().getId().equals(userId)) {
            throw new RuntimeException(UNIT_NOT_FOUND);
        }

        propertyUnitRepository.delete(unit);
    }

    private PropertyUnitDTO mapUnitToDTO(com.immopilot.modules.properties.domain.PropertyUnit unit) {
        return PropertyUnitDTO.builder()
                .id(unit.getId())
                .propertyId(unit.getProperty().getId())
                .name(unit.getName())
                .type(unit.getType())
                .area(unit.getArea())
                .shares(unit.getShares())
                .description(unit.getDescription())
                .build();
    }

    private boolean isFreePlan(Long userId) {
        return userSubscriptionRepository.findByUserId(userId)
                .map(sub -> sub.getPlan().getName().equalsIgnoreCase("FREE"))
                .orElse(true); // Default to FREE if no subscription found
    }

    private PropertyDTO mapToDTO(Property p) {
        return PropertyDTO.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .name(p.getName())
                .propertyType(p.getPropertyType())
                .rentalType(p.getRentalType())
                .ownershipType(p.getOwnershipType())
                .status(p.getStatus())
                .address(p.getAddress())
                .city(p.getCity())
                .country(p.getCountry())
                .area(p.getArea())
                .price(p.getPrice())
                .marketValue(p.getMarketValue())
                .acquisitionDate(p.getAcquisitionDate())
                .description(p.getDescription())
                .mainPhotoUrl(p.getMainPhotoUrl())
                .photoUrls(getPhotoUrls(p.getId()))
                .build();
    }

    private List<String> getPhotoUrls(Long propertyId) {
        return propertyDocumentRepository.findByPropertyId(propertyId).stream()
                .filter(d -> d.getCategory() == com.immopilot.modules.properties.domain.DocumentCategory.PHOTO)
                .map(d -> "/api/documents/download/" + d.getStorageKey())
                .toList();
    }
}
