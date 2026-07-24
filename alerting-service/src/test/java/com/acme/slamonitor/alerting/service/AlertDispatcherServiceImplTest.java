package com.acme.slamonitor.alerting.service;

import com.acme.slamonitor.alerting.document.AlertNotificationDocument;
import com.acme.slamonitor.alerting.dto.AlertDispatchRequest;
import com.acme.slamonitor.alerting.dto.AlertNotificationResponse;
import com.acme.slamonitor.alerting.entity.Tenant;
import com.acme.slamonitor.alerting.entity.UserProfile;
import com.acme.slamonitor.alerting.exception.TenantNotFoundException;
import com.acme.slamonitor.alerting.mapper.AlertNotificationMapper;
import com.acme.slamonitor.alerting.repository.AlertNotificationRepository;
import com.acme.slamonitor.alerting.repository.TenantRepository;
import com.acme.slamonitor.alerting.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertDispatcherServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AlertNotificationRepository alertNotificationRepository;

    @Mock
    private AlertNotificationMapper mapper;

    @InjectMocks
    private AlertDispatcherServiceImpl service;

    private Tenant activeTenant;
    private UserProfile userProfile;

    @BeforeEach
    void setUp() {
        activeTenant = new Tenant();
        activeTenant.setId(1L);
        activeTenant.setTenantCode("ACME");
        activeTenant.setActive(true);

        userProfile = new UserProfile();
        userProfile.setId(1L);
        userProfile.setTenant(activeTenant);
        userProfile.setUsername("admin_acme");
        userProfile.setEmail("admin@acme.com");
        userProfile.setRole("ADMIN");
    }

    @Test
    void dispatch_withKnownTenantAndUserProfiles_dispatchesLogAndEmail() {
        when(tenantRepository.findByTenantCodeAndActiveTrue("ACME")).thenReturn(Optional.of(activeTenant));
        when(userProfileRepository.findByTenantTenantCode("ACME")).thenReturn(List.of(userProfile));

        AlertDispatchRequest request = new AlertDispatchRequest(
                "ACME", "PAYMENT_BATCH", "job-100", "BREACH", 360000L, "SLA Breach Detected");

        AlertNotificationDocument doc1 = AlertNotificationDocument.builder().id("1").channel("LOG").build();
        AlertNotificationDocument doc2 = AlertNotificationDocument.builder().id("2").channel("EMAIL").recipient("admin@acme.com").build();
        List<AlertNotificationDocument> savedDocs = List.of(doc1, doc2);

        AlertNotificationResponse res1 = AlertNotificationResponse.builder().id("1").channel("LOG").build();
        AlertNotificationResponse res2 = AlertNotificationResponse.builder().id("2").channel("EMAIL").build();

        when(alertNotificationRepository.saveAll(anyList())).thenReturn(savedDocs);
        when(mapper.toResponse(doc1)).thenReturn(res1);
        when(mapper.toResponse(doc2)).thenReturn(res2);

        List<AlertNotificationResponse> responses = service.dispatch(request);

        assertThat(responses).hasSize(2);
        verify(alertNotificationRepository).saveAll(argThat(list -> ((List<?>) list).size() == 2));
    }

    @Test
    void dispatch_withUnknownTenant_throwsTenantNotFoundException() {
        when(tenantRepository.findByTenantCodeAndActiveTrue("UNKNOWN")).thenReturn(Optional.empty());

        AlertDispatchRequest request = new AlertDispatchRequest(
                "UNKNOWN", "PAYMENT_BATCH", "job-101", "BREACH", 360000L, "SLA Breach Detected");

        assertThatThrownBy(() -> service.dispatch(request))
                .isInstanceOf(TenantNotFoundException.class)
                .hasMessageContaining("UNKNOWN");

        verifyNoInteractions(alertNotificationRepository);
    }

    @Test
    void findAlerts_withTenantOnly_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        AlertNotificationDocument doc = AlertNotificationDocument.builder().id("1").tenantCode("ACME").build();
        Page<AlertNotificationDocument> docPage = new PageImpl<>(List.of(doc));
        AlertNotificationResponse response = AlertNotificationResponse.builder().id("1").tenantCode("ACME").build();

        when(alertNotificationRepository.findByTenantCode(eq("ACME"), any(Pageable.class))).thenReturn(docPage);
        when(mapper.toResponse(doc)).thenReturn(response);

        Page<AlertNotificationResponse> result = service.findAlerts("ACME", null, pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findAlerts_withTenantAndSeverity_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        AlertNotificationDocument doc = AlertNotificationDocument.builder().id("2").tenantCode("ACME").severity("BREACH").build();
        Page<AlertNotificationDocument> docPage = new PageImpl<>(List.of(doc));
        AlertNotificationResponse response = AlertNotificationResponse.builder().id("2").tenantCode("ACME").severity("BREACH").build();

        when(alertNotificationRepository.findByTenantCodeAndSeverity(eq("ACME"), eq("BREACH"), any(Pageable.class)))
                .thenReturn(docPage);
        when(mapper.toResponse(doc)).thenReturn(response);

        Page<AlertNotificationResponse> result = service.findAlerts("ACME", "breach", pageable);

        assertThat(result.getContent()).containsExactly(response);
    }
}
