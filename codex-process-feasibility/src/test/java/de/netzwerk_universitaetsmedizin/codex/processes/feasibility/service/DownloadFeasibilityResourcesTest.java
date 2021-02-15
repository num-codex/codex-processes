package de.netzwerk_universitaetsmedizin.codex.processes.feasibility.service;

import de.netzwerk_universitaetsmedizin.codex.processes.feasibility.EnhancedFhirWebserviceClientProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.netzwerk_universitaetsmedizin.codex.processes.feasibility.variables.ConstantsFeasibility.CODESYSTEM_FEASIBILITY;
import static de.netzwerk_universitaetsmedizin.codex.processes.feasibility.variables.ConstantsFeasibility.CODESYSTEM_FEASIBILITY_VALUE_MEASURE_REFERENCE;
import static de.netzwerk_universitaetsmedizin.codex.processes.feasibility.variables.ConstantsFeasibility.VARIABLE_LIBRARY;
import static de.netzwerk_universitaetsmedizin.codex.processes.feasibility.variables.ConstantsFeasibility.VARIABLE_MEASURE;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TASK;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DownloadFeasibilityResourcesTest {

    @Mock
    private EnhancedFhirWebserviceClientProvider clientProvider;

    @Mock
    private FhirWebserviceClient webserviceClient;

    @Mock
    private TaskHelper taskHelper;

    @Mock
    private DelegateExecution execution;

    @Mock
    private Task task;

    @InjectMocks
    private DownloadFeasibilityResources service;

    private Map<String, List<String>> createSearchQueryParts(String measureId) {
        return Map.of("_id", Collections.singletonList(measureId), "_include",
                Collections.singletonList("Measure:depends-on"));
    }

    @Test
    public void testDoExecute_NoMeasureReference() {
        when(execution.getVariable(BPMN_EXECUTION_VARIABLE_TASK)).thenReturn(task);
        when(taskHelper.getFirstInputParameterReferenceValue(task, CODESYSTEM_FEASIBILITY,
                CODESYSTEM_FEASIBILITY_VALUE_MEASURE_REFERENCE))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.execute(execution));
        verify(task).setStatus(Task.TaskStatus.FAILED);
    }

    @Test
    public void testDoExecute_BundleWithTooFewResultEntries() {
        final String measureId = "id-151003";
        final IdType measureRefId = new IdType("http://remote.host/Measure/" + measureId);
        final Reference measureRef = new Reference(measureRefId);

        when(execution.getVariable(BPMN_EXECUTION_VARIABLE_TASK))
                .thenReturn(task);
        when(taskHelper.getFirstInputParameterReferenceValue(task, CODESYSTEM_FEASIBILITY,
                CODESYSTEM_FEASIBILITY_VALUE_MEASURE_REFERENCE))
                .thenReturn(Optional.of(measureRef));
        when(clientProvider.getWebserviceClient(measureRefId))
                .thenReturn(webserviceClient);
        when(webserviceClient.searchWithStrictHandling(Measure.class, createSearchQueryParts(measureId)))
                .thenReturn(new Bundle());

        assertThrows(RuntimeException.class, () -> service.execute(execution));
        verify(task).setStatus(Task.TaskStatus.FAILED);
    }

    @Test
    public void testDoExecute_FirstBundleEntryIsNoMeasure() {
        final String measureId = "id-151003";
        final IdType measureRefId = new IdType("http://remote.host/Measure/" + measureId);
        final Reference measureRef = new Reference(measureRefId);

        final Bundle.BundleEntryComponent patientEntryA = new Bundle.BundleEntryComponent()
                .setResource(new Patient().setId("id-170524"));

        final Bundle.BundleEntryComponent patientEntryB = new Bundle.BundleEntryComponent()
                .setResource(new Patient().setId("id-123456"));

        final Bundle measureOnlyBundle = new Bundle()
                .addEntry(patientEntryA)
                .addEntry(patientEntryB);

        when(execution.getVariable(BPMN_EXECUTION_VARIABLE_TASK))
                .thenReturn(task);
        when(taskHelper.getFirstInputParameterReferenceValue(task, CODESYSTEM_FEASIBILITY,
                CODESYSTEM_FEASIBILITY_VALUE_MEASURE_REFERENCE))
                .thenReturn(Optional.of(measureRef));
        when(clientProvider.getWebserviceClient(measureRefId))
                .thenReturn(webserviceClient);
        when(webserviceClient.searchWithStrictHandling(Measure.class, createSearchQueryParts(measureId)))
                .thenReturn(measureOnlyBundle);

        assertThrows(RuntimeException.class, () -> service.execute(execution));
        verify(task).setStatus(Task.TaskStatus.FAILED);
    }

    @Test
    public void testDoExecute_SecondBundleEntryIsNoLibrary() {
        final String measureId = "id-151003";
        final IdType measureRefId = new IdType("http://remote.host/Measure/" + measureId);
        final Reference measureRef = new Reference(measureRefId);

        final Bundle.BundleEntryComponent measureEntryA = new Bundle.BundleEntryComponent()
                .setResource(new Measure().setId("id-170418"));

        final Bundle.BundleEntryComponent measureEntryB = new Bundle.BundleEntryComponent()
                .setResource(new Measure().setId("id-123456"));

        final Bundle measureOnlyBundle = new Bundle()
                .addEntry(measureEntryA)
                .addEntry(measureEntryB);

        when(execution.getVariable(BPMN_EXECUTION_VARIABLE_TASK))
                .thenReturn(task);
        when(taskHelper.getFirstInputParameterReferenceValue(task, CODESYSTEM_FEASIBILITY,
                CODESYSTEM_FEASIBILITY_VALUE_MEASURE_REFERENCE))
                .thenReturn(Optional.of(measureRef));
        when(clientProvider.getWebserviceClient(measureRefId))
                .thenReturn(webserviceClient);
        when(webserviceClient.searchWithStrictHandling(Measure.class, createSearchQueryParts(measureId)))
                .thenReturn(measureOnlyBundle);

        assertThrows(RuntimeException.class, () -> service.execute(execution));
        verify(task).setStatus(Task.TaskStatus.FAILED);
    }

    @Test
    public void testDoExecuteLocal() throws Exception {
        final String measureId = "id-151003";
        final IdType measureRefId = new IdType("Measure/" + measureId);
        final Reference measureRef = new Reference(measureRefId);

        final Resource measure = new Measure();
        measure.setId("id-170418");
        final Bundle.BundleEntryComponent measureEntry = new Bundle.BundleEntryComponent()
                .setResource(measure);

        final Resource library = new Library();
        library.setId("id-170912");
        final Bundle.BundleEntryComponent libraryEntry = new Bundle.BundleEntryComponent()
                .setResource(library);

        final Bundle measureOnlyBundle = new Bundle()
                .addEntry(measureEntry)
                .addEntry(libraryEntry);

        when(execution.getVariable(BPMN_EXECUTION_VARIABLE_TASK))
                .thenReturn(task);
        when(taskHelper.getFirstInputParameterReferenceValue(task, CODESYSTEM_FEASIBILITY,
                CODESYSTEM_FEASIBILITY_VALUE_MEASURE_REFERENCE))
                .thenReturn(Optional.of(measureRef));
        when(clientProvider.getWebserviceClient(measureRefId))
                .thenReturn(webserviceClient);
        when(webserviceClient.searchWithStrictHandling(Measure.class, createSearchQueryParts(measureId)))
                .thenReturn(measureOnlyBundle);

        service.execute(execution);

        verify(execution).setVariable(VARIABLE_MEASURE, measure);
        verify(execution).setVariable(VARIABLE_LIBRARY, library);
    }

    @Test
    public void testDoExecuteRemote() throws Exception {
        final String measureId = "id-151003";
        final IdType measureRefId = new IdType("http://remote.host/Measure/" + measureId);
        final Reference measureRef = new Reference(measureRefId);

        final Resource measure = new Measure();
        measure.setId("id-170418");
        final Bundle.BundleEntryComponent measureEntry = new Bundle.BundleEntryComponent();
        measureEntry.setResource(measure);

        final Resource library = new Library();
        library.setId("id-170912");
        final Bundle.BundleEntryComponent libraryEntry = new Bundle.BundleEntryComponent();
        libraryEntry.setResource(library);

        final Bundle measureOnlyBundle = new Bundle()
                .addEntry(measureEntry)
                .addEntry(libraryEntry);

        when(execution.getVariable(BPMN_EXECUTION_VARIABLE_TASK))
                .thenReturn(task);
        when(taskHelper.getFirstInputParameterReferenceValue(task, CODESYSTEM_FEASIBILITY,
                CODESYSTEM_FEASIBILITY_VALUE_MEASURE_REFERENCE))
                .thenReturn(Optional.of(measureRef));
        when(clientProvider.getWebserviceClient(measureRefId))
                .thenReturn(webserviceClient);
        when(webserviceClient.searchWithStrictHandling(Measure.class, createSearchQueryParts(measureId)))
                .thenReturn(measureOnlyBundle);

        service.execute(execution);

        verify(execution).setVariable(VARIABLE_MEASURE, measure);
        verify(execution).setVariable(VARIABLE_LIBRARY, library);
    }
}
