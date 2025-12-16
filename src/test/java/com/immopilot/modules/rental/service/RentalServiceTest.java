package com.immopilot.modules.rental.service;

import com.immopilot.modules.properties.domain.Property;
import com.immopilot.modules.properties.domain.PropertyType;
import com.immopilot.modules.properties.domain.PropertyUnit;
import com.immopilot.modules.properties.repository.PropertyRepository;
import com.immopilot.modules.rental.domain.Lease;
import com.immopilot.modules.rental.domain.LeaseStatus;
import com.immopilot.modules.rental.repository.LeaseRepository;
import com.immopilot.modules.rental.repository.RentPaymentRepository;
import com.immopilot.modules.rental.repository.TenantRepository;
import com.immopilot.modules.rental.service.dto.LeaseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private LeaseRepository leaseRepository;
    @Mock
    private RentPaymentRepository paymentRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private RentalReceiptService receiptService;

    private RentalService rentalService;

    private Property immeubleWithUnits;
    private Property immeubleNoUnits;
    private PropertyUnit unit1;
    private PropertyUnit unit2;

    @BeforeEach
    void setUp() {
        rentalService = new RentalService(tenantRepository, leaseRepository, paymentRepository, receiptService,
                propertyRepository);

        lenient().when(paymentRepository.findByLeaseIdOrderByPaymentDateDesc(any()))
                .thenReturn(Collections.emptyList());

        immeubleWithUnits = Property.builder()
                .id(1L)
                .propertyType(PropertyType.IMMEUBLE)
                .units(new java.util.HashSet<>())
                .build();

        unit1 = PropertyUnit.builder().id(101L).property(immeubleWithUnits).build();
        unit2 = PropertyUnit.builder().id(102L).property(immeubleWithUnits).build();
        immeubleWithUnits.getUnits().add(unit1);
        immeubleWithUnits.getUnits().add(unit2);

        immeubleNoUnits = Property.builder()
                .id(2L)
                .propertyType(PropertyType.IMMEUBLE)
                .units(new java.util.HashSet<>())
                .build();
    }

    @Test
    void createLease_ImmeubleWithUnits_SpecificUnit_Success() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(immeubleWithUnits));
        when(leaseRepository.findByPropertyId(1L)).thenReturn(Collections.emptyList());
        when(tenantRepository.findAllById(any()))
                .thenReturn(List.of(new com.immopilot.modules.rental.domain.Tenant()));
        when(leaseRepository.save(any(Lease.class))).thenAnswer(i -> {
            Lease l = i.getArgument(0);
            l.setId(99L);
            return l;
        });

        LeaseDTO request = LeaseDTO.builder()
                .unitId(101L)
                .tenantIds(List.of(1L))
                .rentAmount(java.math.BigDecimal.valueOf(1000))
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusYears(1))
                .build();

        assertDoesNotThrow(() -> rentalService.createLease(1L, request));
    }

    @Test
    void createLease_ImmeubleWithUnits_SpecificUnit_Fail_UnitOccupied() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(immeubleWithUnits));

        Lease activeLease = Lease.builder()
                .unit(unit1)
                .status(LeaseStatus.ACTIVE)
                .build();
        when(leaseRepository.findByPropertyId(1L)).thenReturn(List.of(activeLease));

        LeaseDTO request = LeaseDTO.builder()
                .unitId(101L) // Unit 1
                .tenantIds(List.of(1L))
                .rentAmount(java.math.BigDecimal.valueOf(1000))
                .build();

        Exception e = assertThrows(RuntimeException.class, () -> rentalService.createLease(1L, request));
        assertEquals("This unit already has an active lease.", e.getMessage());
    }

    @Test
    void createLease_ImmeubleWithUnits_SpecificUnit_Success_OtherUnitOccupied() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(immeubleWithUnits));
        when(tenantRepository.findAllById(any()))
                .thenReturn(List.of(new com.immopilot.modules.rental.domain.Tenant()));
        when(leaseRepository.save(any(Lease.class))).thenAnswer(i -> {
            Lease l = i.getArgument(0);
            l.setId(99L);
            return l;
        });

        Lease activeLease = Lease.builder()
                .unit(unit1) // Unit 1 Occupied
                .status(LeaseStatus.ACTIVE)
                .build();
        when(leaseRepository.findByPropertyId(1L)).thenReturn(List.of(activeLease));

        LeaseDTO request = LeaseDTO.builder()
                .unitId(102L) // Requesting Unit 2
                .tenantIds(List.of(1L))
                .rentAmount(java.math.BigDecimal.valueOf(1000))
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusYears(1))
                .build();

        assertDoesNotThrow(() -> rentalService.createLease(1L, request));
    }

    @Test
    void createLease_ImmeubleWithUnits_SpecificUnit_Fail_GlobalLeaseExists() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(immeubleWithUnits));

        Lease globalLease = Lease.builder()
                .unit(null) // Global
                .status(LeaseStatus.ACTIVE)
                .build();
        when(leaseRepository.findByPropertyId(1L)).thenReturn(List.of(globalLease));

        LeaseDTO request = LeaseDTO.builder()
                .unitId(101L)
                .tenantIds(List.of(1L))
                .rentAmount(java.math.BigDecimal.valueOf(1000))
                .build();

        Exception e = assertThrows(RuntimeException.class, () -> rentalService.createLease(1L, request));
        assertEquals("Cannot create unit lease: A global lease exists for this property.", e.getMessage());
    }

    @Test
    void createLease_ImmeubleWithUnits_GlobalLease_Success() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(immeubleWithUnits));
        when(leaseRepository.findByPropertyId(1L)).thenReturn(Collections.emptyList());
        when(tenantRepository.findAllById(any()))
                .thenReturn(List.of(new com.immopilot.modules.rental.domain.Tenant()));
        when(leaseRepository.save(any(Lease.class))).thenAnswer(i -> {
            Lease l = i.getArgument(0);
            l.setId(99L);
            return l;
        });

        LeaseDTO request = LeaseDTO.builder()
                .isGlobalLease(true)
                .tenantIds(List.of(1L))
                .rentAmount(java.math.BigDecimal.valueOf(1000))
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusYears(1))
                .build();

        assertDoesNotThrow(() -> rentalService.createLease(1L, request));
    }

    @Test
    void createLease_ImmeubleWithUnits_GlobalLease_Fail_AnyUnitActive() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(immeubleWithUnits));

        Lease activeLease = Lease.builder()
                .unit(unit1)
                .status(LeaseStatus.ACTIVE)
                .build();
        when(leaseRepository.findByPropertyId(1L)).thenReturn(List.of(activeLease));

        LeaseDTO request = LeaseDTO.builder()
                .isGlobalLease(true)
                .tenantIds(List.of(1L))
                .rentAmount(java.math.BigDecimal.valueOf(1000))
                .build();

        Exception e = assertThrows(RuntimeException.class, () -> rentalService.createLease(1L, request));
        assertTrue(e.getMessage().contains("Cannot create a global lease: Active leases exist"));
    }

    @Test
    void createLease_ImmeubleNoUnits_Fail_WithoutGlobalFlag() {
        when(propertyRepository.findById(2L)).thenReturn(Optional.of(immeubleNoUnits));
        when(leaseRepository.findByPropertyId(2L)).thenReturn(Collections.emptyList());

        LeaseDTO request = LeaseDTO.builder()
                .tenantIds(List.of(1L))
                .rentAmount(java.math.BigDecimal.valueOf(1000))
                // No isGlobalLease=true
                .build();

        Exception e = assertThrows(RuntimeException.class, () -> rentalService.createLease(2L, request));
        assertTrue(e.getMessage().contains("please confirm 'Global Lease'"));
    }

    @Test
    void createLease_ImmeubleNoUnits_Success_WithGlobalFlag() {
        when(propertyRepository.findById(2L)).thenReturn(Optional.of(immeubleNoUnits));
        when(leaseRepository.findByPropertyId(2L)).thenReturn(Collections.emptyList());
        when(tenantRepository.findAllById(any()))
                .thenReturn(List.of(new com.immopilot.modules.rental.domain.Tenant()));
        when(leaseRepository.save(any(Lease.class))).thenAnswer(i -> {
            Lease l = i.getArgument(0);
            l.setId(99L);
            return l;
        });

        LeaseDTO request = LeaseDTO.builder()
                .isGlobalLease(true)
                .tenantIds(List.of(1L))
                .rentAmount(java.math.BigDecimal.valueOf(1000))
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusYears(1))
                .build();

        assertDoesNotThrow(() -> rentalService.createLease(2L, request));
    }
}
