package com.immopilot.modules.properties.service;

import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.domain.PropertyStatus;
import com.immopilot.modules.properties.domain.PropertyType;
import com.immopilot.modules.properties.domain.PropertyUnit;
import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.properties.repository.PropertyUnitRepository;
import com.immopilot.modules.properties.service.dto.CreatePropertyRequest;
import com.immopilot.modules.users.domain.User;
import com.immopilot.modules.users.repository.UserRepository;
import com.immopilot.modules.users.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;
    @Mock
    private com.immopilot.modules.properties.repository.PropertyDocumentRepository propertyDocumentRepository;
    @Mock
    private PropertyUnitRepository propertyUnitRepository;

    @InjectMocks
    private PropertyService propertyService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
    }

    @Test
    void createProperty_ImmeubleWithUnits_ShouldCreateUnits() {
        // Arrange
        CreatePropertyRequest request = CreatePropertyRequest.builder()
                .name("Immeuble Test")
                .propertyType(PropertyType.IMMEUBLE)
                .status(PropertyStatus.OWNED)
                .numberOfUnits(5)
                .address("123 Test St")
                .city("Test City")
                .country("Test Country")
                .price(BigDecimal.valueOf(1000000))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSubscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty()); // Free plan
        when(propertyRepository.countByUserId(1L)).thenReturn(0L); // Under limit

        Property savedProperty = Property.builder()
                .id(10L)
                .user(user)
                .name("Immeuble Test")
                .propertyType(PropertyType.IMMEUBLE)
                .build();

        when(propertyRepository.save(any(Property.class))).thenReturn(savedProperty);

        // Act
        propertyService.createProperty(1L, request);

        // Assert
        verify(propertyUnitRepository, times(5)).save(any(PropertyUnit.class));
    }
}
