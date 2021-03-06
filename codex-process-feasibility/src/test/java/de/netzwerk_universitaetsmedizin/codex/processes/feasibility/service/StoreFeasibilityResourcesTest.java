package de.netzwerk_universitaetsmedizin.codex.processes.feasibility.service;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.netzwerk_universitaetsmedizin.codex.processes.feasibility.variables.ConstantsFeasibility;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static de.netzwerk_universitaetsmedizin.codex.processes.feasibility.variables.ConstantsFeasibility.VARIABLE_LIBRARY;
import static de.netzwerk_universitaetsmedizin.codex.processes.feasibility.variables.ConstantsFeasibility.VARIABLE_MEASURE;
import static de.netzwerk_universitaetsmedizin.codex.processes.feasibility.variables.ConstantsFeasibility.VARIABLE_MEASURE_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class StoreFeasibilityResourcesTest {

    public static final String ID = "foo";

    @Captor
    ArgumentCaptor<Bundle> bundleCaptor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IGenericClient storeClient;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private StoreFeasibilityResources service;

    @Test
    public void testDoExecute() {
        Measure measure = new Measure();
        Library library = new Library();
        when(execution.getVariable(VARIABLE_MEASURE)).thenReturn(measure);
        when(execution.getVariable(VARIABLE_LIBRARY)).thenReturn(library);

        Bundle transactionResponse = new Bundle();
        transactionResponse.addEntry().getResponse().setLocation("http://localhost/some-location/" + ID);

        when(storeClient.transaction().withBundle(bundleCaptor.capture()).execute())
                .thenReturn(transactionResponse);

        service.doExecute(execution);

        verify(execution).setVariable(VARIABLE_MEASURE_ID, ID);
        assertEquals(measure, bundleCaptor.getValue().getEntry().get(0).getResource());
        assertEquals(library, bundleCaptor.getValue().getEntry().get(1).getResource());
        assertEquals("Measure", bundleCaptor.getValue().getEntry().get(0).getRequest().getUrl());
        assertEquals("Library", bundleCaptor.getValue().getEntry().get(1).getRequest().getUrl());
    }
}
