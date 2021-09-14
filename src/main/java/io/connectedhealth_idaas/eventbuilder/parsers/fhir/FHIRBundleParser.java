package io.connectedhealth_idaas.eventbuilder.parsers.fhir;import com.google.gson.Gson;import io.connectedhealth_idaas.eventbuilder.dataobjects.clinical.fhir.r4.resources.Bundle;import io.connectedhealth_idaas.eventbuilder.dataobjects.clinical.fhir.r4.resources.Encounter;import io.connectedhealth_idaas.eventbuilder.dataobjects.clinical.fhir.r4.resources.EncounterR3;import io.connectedhealth_idaas.eventbuilder.dataobjects.general.Application;import io.connectedhealth_idaas.eventbuilder.dataobjects.general.Codeset;import io.connectedhealth_idaas.eventbuilder.dataobjects.general.Organization;import io.connectedhealth_idaas.eventbuilder.dataobjects.platform.MessageHeader;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.text.SimpleDateFormat;import java.util.*;public class FHIRBundleParser {    private static final Logger LOG = LoggerFactory.getLogger(FHIRResourceParser.class);    /*     *   Return Generic Message Header based on FHIR Resources     */    public static String parseFHIRBundleToMessageHeader(String body)    {        //Create Unique MesageID GUID        UUID uuid = UUID.randomUUID();        String uuidstr = uuid.toString();        Date date = new Date();        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ss'Z'");        simpleDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));        String fullDate = simpleDateFormat.format(date);        Long hour = (long)(date.getTime() % 86400000) / 3600000;        List<Organization> sendingOrganizations = new ArrayList<>();        List<Application> sendingApplications = new ArrayList<>();        Organization orgs = new Organization();        Application apps = new Application();        Codeset cpEvent = new Codeset();        List<Codeset> terminologyCodes = new ArrayList<Codeset>();        MessageHeader mshHeader = new MessageHeader();            Gson gson = new Gson();            Bundle bundle = gson.fromJson(body, Bundle.class);            for (int i = 0; i < bundle.entry.size(); i++) {            if(bundle.entry.get(i).resource.resourceType.equals("Organization")){                orgs.setBusinessName("UNDF");                orgs.setIdentifierCode(bundle.entry.get(i).resource.identifier.get(0).value);                sendingOrganizations.add(orgs);            }            if(bundle.entry.get(i).resource.resourceType.equals("Patient")){                apps.setIdentifierCode(bundle.entry.get(i).resource.identifier.get(0).system.split(":")[2].toString());                apps.setApplicationName("UNDF");                sendingApplications.add(apps);            }            if(bundle.entry.get(i).resource.resourceType.equals("Encounter")){                EncounterR3 encounter = gson.fromJson(gson.toJson(bundle.entry.get(i).resource), EncounterR3.class);                cpEvent.setCodeValue(encounter.getType().get(0).getCoding().get(0).code.toString());                cpEvent.setDisplayName(encounter.getType().get(0).getCoding().get(0).display.toString());                terminologyCodes.add(cpEvent);            }            }            mshHeader.setIndustryStd("FHIR");            mshHeader.setMessageType("Clinical");            mshHeader.setMessageId(uuidstr);            mshHeader.setMessageVersion("R4Bundle");            mshHeader.setApplications(sendingApplications);            mshHeader.setOrganization(sendingOrganizations);        final Map<String, Object> response = new HashMap<>();        response.put("header", mshHeader);        response.put("terminologies", terminologyCodes);        return new Gson().toJson(response);    }}