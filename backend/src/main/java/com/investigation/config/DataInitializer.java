package com.investigation.config;

import com.investigation.entity.*;
import com.investigation.entity.enums.*;
import com.investigation.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.Instant;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CaseRepository caseRepository;
    private final EvidenceRepository evidenceRepository;
    private final TimelineEventRepository timelineEventRepository;
    private final GraphNodeRepository graphNodeRepository;
    private final GraphEdgeRepository graphEdgeRepository;
    private final AnomalyRepository anomalyRepository;
    private final AiSummaryRepository aiSummaryRepository;

    public DataInitializer(
            CaseRepository caseRepository,
            EvidenceRepository evidenceRepository,
            TimelineEventRepository timelineEventRepository,
            GraphNodeRepository graphNodeRepository,
            GraphEdgeRepository graphEdgeRepository,
            AnomalyRepository anomalyRepository,
            AiSummaryRepository aiSummaryRepository) {
        this.caseRepository = caseRepository;
        this.evidenceRepository = evidenceRepository;
        this.timelineEventRepository = timelineEventRepository;
        this.graphNodeRepository = graphNodeRepository;
        this.graphEdgeRepository = graphEdgeRepository;
        this.anomalyRepository = anomalyRepository;
        this.aiSummaryRepository = aiSummaryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (caseRepository.count() > 0) {
            log.info("Database already contains {} cases. Skipping historical data seeding.", caseRepository.count());
            return;
        }

        log.info("Seeding database with the six mandatory preloaded historical investigation cases...");

        seedMonaLisaCase();
        seedLindberghCase();
        seedHarshadMehtaCase();
        seedBhopalCase();
        seedMundhraCase();
        seedLufthansaCase();

        log.info("Historical cases seeding completed successfully. Total cases in database: {}", caseRepository.count());
    }

    private void seedMonaLisaCase() {
        CaseEntity c = new CaseEntity(
                "CAS-1911-001",
                "Mona Lisa Theft (1911)",
                "Theft of Leonardo da Vinci's Mona Lisa from the Salon Carré at the Musée du Louvre, Paris, on August 21, 1911, by former glazier Vincenzo Peruggia. Frame discarded on internal stairwell; recovered in Florence in December 1913.",
                CaseCategory.HEIST_THEFT,
                CaseStatus.CLOSED,
                true,
                Instant.parse("1911-08-21T07:00:00Z")
        );
        c = caseRepository.save(c);

        EvidenceEntity e1 = createEvidence(c, "data/samples/mona_lisa_1911/mona_lisa_museum_guard_rounds.csv", "mona_lisa_museum_guard_rounds.csv", 8, true);
        EvidenceEntity e2 = createEvidence(c, "data/samples/mona_lisa_1911/mona_lisa_worker_logs.csv", "mona_lisa_worker_logs.csv", 5, true);

        createTimeline(c, e2, Instant.parse("1911-08-21T06:30:00Z"), "Worker Ingress Logged", "Glazier Vincenzo Peruggia enters Cour Visconti wearing standard Louvre white worker smock.", "PHYSICAL", false);
        createTimeline(c, e1, Instant.parse("1911-08-21T06:45:00Z"), "Missed Security Checkpoint", "Guard patrol round unrecorded in Salon Carré sector during cleaning transition.", "PHYSICAL", true);
        createTimeline(c, e1, Instant.parse("1911-08-21T07:10:00Z"), "Frame Bracket Discarded", "Peruggia unhooks portrait, carries it to Service Staircase 4, removes wooden frame and protective glass.", "PHYSICAL", false);
        createTimeline(c, e2, Instant.parse("1911-08-21T07:35:00Z"), "Keyless Forced Egress", "Doorknob unscrewed at Cour du Sphinx exit; Peruggia leaves museum with painting under smock.", "PHYSICAL", true);
        createTimeline(c, e1, Instant.parse("1911-08-22T08:30:00Z"), "Theft Discovered by Painter Béroud", "Artist Louis Béroud arrives to sketch Mona Lisa; spots four iron pegs on vacant wall. Official alarm sounded.", "FORENSIC", false);
        createTimeline(c, null, Instant.parse("1913-11-29T14:00:00Z"), "Florentine Art Dealer Contacted", "Peruggia contacts antique dealer Alfredo Geri in Florence requesting 500,000 lire for return to Italy.", "COMMUNICATION", false);
        createTimeline(c, null, Instant.parse("1913-12-11T11:30:00Z"), "Recovery and Apprehension", "Painting verified by Uffizi Gallery Director Giovanni Poggi; Peruggia arrested at Hotel Tripoli-Italia.", "FORENSIC", false);

        GraphNodeEntity nPeruggia = createNode(c, "Vincenzo Peruggia", NodeType.PERSON);
        GraphNodeEntity nLouvre = createNode(c, "Musée du Louvre", NodeType.ORGANIZATION);
        GraphNodeEntity nSalon = createNode(c, "Salon Carré", NodeType.LOCATION);
        GraphNodeEntity nBeroud = createNode(c, "Louis Béroud", NodeType.PERSON);
        GraphNodeEntity nGeri = createNode(c, "Alfredo Geri", NodeType.PERSON);
        GraphNodeEntity nPainting = createNode(c, "Mona Lisa (La Gioconda)", NodeType.EVIDENCE_OBJECT);

        createEdge(c, nPeruggia, nLouvre, "EMPLOYED_AT", 1.0);
        createEdge(c, nPainting, nSalon, "HOUSED_IN", 1.0);
        createEdge(c, nPeruggia, nPainting, "STOLE_FROM_WALL", 2.0);
        createEdge(c, nBeroud, nSalon, "DISCOVERED_ABSENCE", 1.0);
        createEdge(c, nPeruggia, nGeri, "ATTEMPTED_FENCE_TO", 1.5);

        createAnomaly(c, "OUT_OF_SEQUENCE", "Missing Guard Round in Salon Carré", "Security checkpoints unattended between 06:45 and 07:45 CET, creating unguarded 60-minute window in primary exhibition gallery.", AnomalySeverity.HIGH, 0.95, "Interval gap: 60 min vs standard 15 min patrol cycle");
        createAnomaly(c, "DISCREPANCY", "Forced Doorknob Disassembly at Cour du Sphinx", "Service door mechanical hardware dismantled from inside to bypass locked exit during museum closure.", AnomalySeverity.HIGH, 0.92, "Physical latch tampering confirmed by Inspector Lepine");

        createSummary(c,
                "On August 21, 1911, Leonardo da Vinci's masterpiece Mona Lisa was stolen from the Salon Carré of the Musée du Louvre in Paris while the museum was closed for routine maintenance. Vincenzo Peruggia, a former Louvre glass fitter, carried the painting out concealed beneath his workman smock.",
                "Analysis of worker logs and security patrols confirms Peruggia entered through regular worker access points. He removed the portrait to an enclosed service staircase, separated the poplar wood panel from its 200-pound frame, and exited via a forced service door.",
                "Primary irregularities include an unmonitored 60-minute gap in security guard logs and the discovery of the discarded frame and protective glass bracket on internal service staircases hours before the missing artwork was officially reported.",
                "Forensic Guardrail Notice: All findings reflect established historical records and archival trial evidence from Peruggia's 1914 Florence tribunal. Modern simulated patrol logs are calibrated to documented museum rounds."
        );
    }

    private void seedLindberghCase() {
        CaseEntity c = new CaseEntity(
                "CAS-1932-001",
                "Lindbergh Kidnapping (1932)",
                "Abduction and murder of 20-month-old Charles Augustus Lindbergh Jr. from his family estate in East Amwell, New Jersey. Intercepted gold certificate serial numbers and botanical wood grain analysis of the handmade ladder led to the conviction of Bruno Richard Hauptmann.",
                CaseCategory.KIDNAPPING_ABDUCTION,
                CaseStatus.CLOSED,
                true,
                Instant.parse("1932-03-01T21:00:00Z")
        );
        c = caseRepository.save(c);

        EvidenceEntity e1 = createEvidence(c, "data/samples/lindbergh_1932/lindbergh_gold_certificate_serial_tracker.csv", "lindbergh_gold_certificate_serial_tracker.csv", 5, true);
        EvidenceEntity e2 = createEvidence(c, "data/samples/lindbergh_1932/lindbergh_wood_ladder_forensics.csv", "lindbergh_wood_ladder_forensics.csv", 3, true);

        createTimeline(c, null, Instant.parse("1932-03-01T21:00:00Z"), "Abduction from Nursery", "Infant Charles Jr. abducted from second-floor nursery window using a three-section wooden ladder.", "PHYSICAL", false);
        createTimeline(c, null, Instant.parse("1932-03-01T22:00:00Z"), "Ransom Note Discovered", "Handwritten note demanding $50,000 ransom discovered on nursery windowsill with singlet interlocking circle seal.", "FORENSIC", false);
        createTimeline(c, null, Instant.parse("1932-04-02T20:30:00Z"), "Ransom Delivery in Cemetery", "Dr. John F. Condon delivers $50,000 in recorded U.S. Gold Certificates to suspect 'Cemetery John'.", "FINANCIAL", false);
        createTimeline(c, e1, Instant.parse("1934-09-08T16:20:00Z"), "Bronx Gas Station License Note", "Attendant John Lyons writes license plate 4U-13-41-NY on $10 gold certificate tendered for gas.", "FINANCIAL", true);
        createTimeline(c, e2, Instant.parse("1934-09-19T10:00:00Z"), "Hauptmann Arrested and Wood Traced", "Bruno Richard Hauptmann arrested; wood expert Arthur Koehler matches Ladder Rail 16 grain to Hauptmann attic floorboard.", "FORENSIC", false);

        GraphNodeEntity nHauptmann = createNode(c, "Bruno Richard Hauptmann", NodeType.PERSON);
        GraphNodeEntity nLindbergh = createNode(c, "Charles Lindbergh Sr.", NodeType.PERSON);
        GraphNodeEntity nCondon = createNode(c, "Dr. John F. Condon", NodeType.PERSON);
        GraphNodeEntity nKoehler = createNode(c, "Arthur Koehler (Forensic Botanist)", NodeType.PERSON);
        GraphNodeEntity nEstate = createNode(c, "East Amwell Estate", NodeType.LOCATION);
        GraphNodeEntity nLumber = createNode(c, "National Lumber Company", NodeType.ORGANIZATION);

        createEdge(c, nHauptmann, nEstate, "TRESPASSED_AT", 2.0);
        createEdge(c, nCondon, nHauptmann, "DELIVERED_RANSOM_TO", 1.5);
        createEdge(c, nKoehler, nHauptmann, "MATCHED_WOOD_TOOLMARKS_TO", 2.0);
        createEdge(c, nLumber, nHauptmann, "SUPPLIED_LADDER_PINE_TO", 1.2);

        createAnomaly(c, "DISCREPANCY", "License Plate Annotation on Gold Certificate", "Attendant pencil annotation on $10 bill matched New York registration of Bruno Hauptmann's Dodge sedan.", AnomalySeverity.HIGH, 0.98, "Serial B08241094A confirmed in Treasury ransom circular");
        createAnomaly(c, "VALUE_SPIKE", "Attic Floorboard Growth Ring Discontinuity", "Rail 16 of the ladder exhibited 38 growth rings matching severed attic floorboard with identical planer cutter marks.", AnomalySeverity.HIGH, 0.96, "Botanical wood grain alignment probability > 99.99%");

        createSummary(c,
                "On March 1, 1932, the child of aviator Charles Lindbergh was kidnapped from his home in East Amwell, New Jersey. The kidnapper demanded $50,000 paid in U.S. Gold Certificates. Over two years later, tracking gold certificate serial numbers and botanical tool-mark analysis cracked the case.",
                "Forensic wood technologist Arthur Koehler mapped the unique cutter marks of the handmade ladder back to a Bronx lumberyard and proved that rail 16 had been sawed directly from the attic floorboard of suspect Bruno Richard Hauptmann.",
                "Serial number monitoring established a cluster of gold certificate transactions across Bronx merchants, culminating in a recorded license plate at a Warner-Quinlan service station that identified Hauptmann's vehicle.",
                "Forensic Guardrail Notice: All timeline events and technical wood matches reflect the historical trial records of the State of New Jersey v. Bruno Richard Hauptmann (1935). Demo tracking tables replicate official Treasury serialization logs."
        );
    }

    private void seedHarshadMehtaCase() {
        CaseEntity c = new CaseEntity(
                "CAS-1992-001",
                "Harshad Mehta Securities Scam (1992)",
                "Systemic financial manipulation of the Bombay Stock Exchange (BSE) through fraudulent inter-bank Bank Receipts (BRs) and ready-forward government securities trading. Exposed by journalist Sucheta Dalal.",
                CaseCategory.FINANCIAL_FRAUD,
                CaseStatus.CLOSED,
                true,
                Instant.parse("1992-04-23T09:00:00Z")
        );
        c = caseRepository.save(c);

        EvidenceEntity e1 = createEvidence(c, "data/samples/harshad_mehta_1992/harshad_mehta_interbank_br_transactions.csv", "harshad_mehta_interbank_br_transactions.csv", 5, true);
        EvidenceEntity e2 = createEvidence(c, "data/samples/harshad_mehta_1992/harshad_mehta_cheque_flows.csv", "harshad_mehta_cheque_flows.csv", 4, true);

        createTimeline(c, e1, Instant.parse("1992-01-14T11:00:00Z"), "Uncollateralized Ready Forward Deal", "Bank of Karad issues ₹50 crore Ready Forward deal to SBI with missing underlying security reconciliation.", "FINANCIAL", true);
        createTimeline(c, e1, Instant.parse("1992-02-03T14:30:00Z"), "Fake BR Issued by Metropolitan Co-op", "Metropolitan Cooperative Bank issues Bank Receipt MCB-BR-4011 for ₹75 crore without government bonds backing it.", "FINANCIAL", true);
        createTimeline(c, e2, Instant.parse("1992-02-05T12:30:00Z"), "Diversion to Stock Speculation", "₹48 crore routed from inter-bank treasury accounts into Growmore accounts to aggressively corner ACC shares.", "FINANCIAL", false);
        createTimeline(c, null, Instant.parse("1992-04-23T09:00:00Z"), "Sucheta Dalal Expose in Times of India", "Investigative report reveals ₹500 crore shortfall in State Bank of India government securities portfolio.", "COMMUNICATION", false);
        createTimeline(c, null, Instant.parse("1992-06-04T10:00:00Z"), "CBI Raids and BSE Suspension", "CBI registers multiple criminal FIRs against Harshad Mehta and treasury officials; BSE suspends trading.", "FORENSIC", false);

        GraphNodeEntity nMehta = createNode(c, "Harshad Mehta", NodeType.PERSON);
        GraphNodeEntity nGrowmore = createNode(c, "Growmore Research & Assets Management", NodeType.ORGANIZATION);
        GraphNodeEntity nSBI = createNode(c, "State Bank of India", NodeType.BANK_ACCOUNT);
        GraphNodeEntity nKarad = createNode(c, "Bank of Karad", NodeType.BANK_ACCOUNT);
        GraphNodeEntity nMetropolitan = createNode(c, "Metropolitan Cooperative Bank", NodeType.BANK_ACCOUNT);
        GraphNodeEntity nDalal = createNode(c, "Sucheta Dalal (Journalist)", NodeType.PERSON);

        createEdge(c, nMehta, nGrowmore, "FOUNDER_AND_CONTROLLER", 2.0);
        createEdge(c, nKarad, nGrowmore, "ISSUED_UNBACKED_BR_TO", 2.0);
        createEdge(c, nMetropolitan, nGrowmore, "DIVERTED_FUNDS_TO", 1.8);
        createEdge(c, nGrowmore, nSBI, "ROUTED_CAPITAL_THROUGH", 1.5);
        createEdge(c, nDalal, nSBI, "INVESTIGATED_DEFICIT_IN", 1.2);

        createAnomaly(c, "AMOUNT_OUTLIER", "Zero-Collateral Bank Receipt Issuance", "Bank of Karad issued ready-forward receipts for ₹1.2 billion without corresponding central government bond allocations in SGL register.", AnomalySeverity.HIGH, 0.99, "SGL ledger variance: ₹1,200,000,000 deficit");
        createAnomaly(c, "FREQUENCY_SURGE", "Cyclical Fund Routing between Broker and Banks", "Cheque inflows from interbank money market accounts matched same-day stock market purchasing volume within 2.4 hours.", AnomalySeverity.HIGH, 0.94, "Correlation coefficient r = 0.985 across BSE scrip purchases");

        createSummary(c,
                "In 1992, stockbroker Harshad Mehta exploited structural loopholes in the inter-bank ready-forward market to divert funds from public sector and cooperative banks into aggressive stock market speculation, ramping equity valuations such as ACC Limited by several hundred percent.",
                "The scheme relied upon issuing fraudulent Bank Receipts (BRs) from compliant institutions like Bank of Karad and Metropolitan Cooperative Bank. The issuing banks had no underlying government securities, yet counterparty banks disbursed hundreds of crores against the receipts.",
                "Forensic ledger auditing demonstrates astronomical deficits between issued BR amounts and official Subsidiary General Ledger (SGL) registers maintained by the Reserve Bank of India.",
                "Forensic Guardrail Notice: Based on the findings of the Janakiraman Committee Reports (1992-1993) and the Joint Parliamentary Committee inquiry into irregularities in securities transactions."
        );
    }

    private void seedBhopalCase() {
        CaseEntity c = new CaseEntity(
                "CAS-1984-001",
                "Bhopal Gas Tragedy (1984)",
                "Catastrophic industrial disaster caused by the release of over 40 metric tons of toxic Methyl Isocyanate (MIC) gas from the Union Carbide India Limited (UCIL) pesticide plant in Bhopal, Madhya Pradesh, on December 2-3, 1984.",
                CaseCategory.INDUSTRIAL_DISASTER,
                CaseStatus.CLOSED,
                true,
                Instant.parse("1984-12-02T23:00:00Z")
        );
        c = caseRepository.save(c);

        EvidenceEntity e1 = createEvidence(c, "data/samples/bhopal_1984/bhopal_tank610_telemetry_log.csv", "bhopal_tank610_telemetry_log.csv", 6, true);
        EvidenceEntity e2 = createEvidence(c, "data/samples/bhopal_1984/bhopal_safety_maintenance_deferrals.csv", "bhopal_safety_maintenance_deferrals.csv", 4, true);

        createTimeline(c, e2, Instant.parse("1982-06-15T00:00:00Z"), "Refrigeration Unit Decommissioned", "Freon chilling unit designed to maintain MIC below 5°C was turned off to reduce electricity overhead.", "SYSTEM_LOG", true);
        createTimeline(c, e2, Instant.parse("1984-10-22T00:00:00Z"), "Scrubber Standby Without Caustic Charge", "Vent gas scrubber system deactivated from operational circuit and left under-charged with neutralizing caustic soda.", "SYSTEM_LOG", true);
        createTimeline(c, e2, Instant.parse("1984-11-20T00:00:00Z"), "Flare Tower Line Dismantled", "Flare tower connecting pipe disconnected for corrosion repairs with no backup bypass installed.", "PHYSICAL", true);
        createTimeline(c, e1, Instant.parse("1984-12-02T21:00:00Z"), "Water Washing Operation Begun", "Routine water flushing of header pipes started without mandatory slip-blind safety seals in place.", "PHYSICAL", false);
        createTimeline(c, e1, Instant.parse("1984-12-02T22:30:00Z"), "Pressure Escalation in Tank E610", "Telemetry records gauge rising from 2 psi to 10 psi; water contamination reaction initiates exothermic release.", "SYSTEM_LOG", true);
        createTimeline(c, e1, Instant.parse("1984-12-03T00:15:00Z"), "Safety Valve Rupture and Release", "Pressure breaches 55 psi gauge limit; bursting disc ruptures; over 40 tons of MIC vapor discharges into atmosphere.", "FORENSIC", false);

        GraphNodeEntity nUCIL = createNode(c, "Union Carbide India Limited (UCIL)", NodeType.ORGANIZATION);
        GraphNodeEntity nTank = createNode(c, "MIC Storage Tank E610", NodeType.EVIDENCE_OBJECT);
        GraphNodeEntity nScrubber = createNode(c, "Vent Gas Scrubber Unit", NodeType.EVIDENCE_OBJECT);
        GraphNodeEntity nFlare = createNode(c, "Flare Stack Tower", NodeType.EVIDENCE_OBJECT);
        GraphNodeEntity nBhopal = createNode(c, "Bhopal UCIL Pesticide Plant", NodeType.LOCATION);

        createEdge(c, nUCIL, nBhopal, "OPERATED_FACILITY", 2.0);
        createEdge(c, nTank, nBhopal, "LOCATED_WITHIN", 1.0);
        createEdge(c, nScrubber, nTank, "FAILED_TO_SCRUB", 2.0);
        createEdge(c, nFlare, nTank, "DISCONNECTED_FROM", 2.0);

        createAnomaly(c, "VALUE_SPIKE", "Exothermic Runaway Reaction in Tank E610", "Pressure escalated by over 2,500% in under two hours, indicating massive exothermic water-MIC reaction without cooling.", AnomalySeverity.HIGH, 0.99, "Pressure delta: +53 psi, Temp > 200°C");
        createAnomaly(c, "DISCREPANCY", "Complete Simultaneous Failure of Defense-in-Depth Barriers", "Refrigeration system, vent gas scrubber, and flare stack were simultaneously incapacitated at time of runaway.", AnomalySeverity.HIGH, 0.98, "Defense-in-depth availability: 0/3 active barriers");

        createSummary(c,
                "On the night of December 2-3, 1984, water entered Tank E610 containing 42 metric tons of liquid Methyl Isocyanate at the UCIL pesticide facility in Bhopal, triggering an uncontrolled exothermic reaction that vaporized toxic gas across dense residential neighborhoods.",
                "Forensic safety analysis confirms the complete collapse of secondary and tertiary containment barriers: the 30-ton refrigeration unit was turned off in 1982, the vent gas scrubber caustic wash pump was uncharged, and the flare stack was disconnected for maintenance.",
                "Telemetry records from Tank E610 show rapid exponential pressure increases from 2 psi to exceeding the gauge's maximum 55 psi rating, destroying the concrete casing and lifting the safety relief valve.",
                "Forensic Guardrail Notice: Factual parameters adhere to technical investigation reports published by the Council of Scientific and Industrial Research (CSIR) and official Indian judicial inquiries."
        );
    }

    private void seedMundhraCase() {
        CaseEntity c = new CaseEntity(
                "CAS-1957-001",
                "Mundhra Scandal (1957)",
                "Independent India's first major financial scandal involving the fraudulent investment of ₹1.24 crore by the state-owned Life Insurance Corporation (LIC) into distressed and shell companies owned by industrialist Haridas Mundhra. Exposed by Feroze Gandhi.",
                CaseCategory.FINANCIAL_FRAUD,
                CaseStatus.CLOSED,
                true,
                Instant.parse("1957-06-25T11:00:00Z")
        );
        c = caseRepository.save(c);

        EvidenceEntity e1 = createEvidence(c, "data/samples/mundhra_1957/mundhra_lic_share_purchases.csv", "mundhra_lic_share_purchases.csv", 5, true);
        EvidenceEntity e2 = createEvidence(c, "data/samples/mundhra_1957/mundhra_corporate_circular_holdings.csv", "mundhra_corporate_circular_holdings.csv", 5, true);

        createTimeline(c, e1, Instant.parse("1957-06-25T11:00:00Z"), "Bypassed LIC Investment Committee", "LIC orders purchase of ₹1.24 crore worth of shares in six troubled Mundhra companies without consulting statutory investment committee.", "FINANCIAL", true);
        createTimeline(c, e2, Instant.parse("1957-06-25T12:00:00Z"), "Circular Ownership Identified", "Audit reveals cross-holdings between Richardson & Cruddas, Jessop & Co, and Mundhra Concerns inflating book equity.", "FINANCIAL", false);
        createTimeline(c, null, Instant.parse("1957-12-16T14:00:00Z"), "Feroze Gandhi Expose in Parliament", "MP Feroze Gandhi delivers landmark parliamentary address exposing collusion and inflated share purchases.", "COMMUNICATION", false);
        createTimeline(c, null, Instant.parse("1958-01-15T10:00:00Z"), "Chagla Commission Public Inquiry", "Justice M. C. Chagla conducts first open, transparent public commission of inquiry in Bombay.", "FORENSIC", false);
        createTimeline(c, null, Instant.parse("1958-02-13T17:00:00Z"), "Resignation of Finance Minister", "Finance Minister T. T. Krishnamachari resigns following Chagla Commission report; Haridas Mundhra sentenced to prison.", "FORENSIC", false);

        GraphNodeEntity nMundhra = createNode(c, "Haridas Mundhra", NodeType.PERSON);
        GraphNodeEntity nLIC = createNode(c, "Life Insurance Corporation of India (LIC)", NodeType.ORGANIZATION);
        GraphNodeEntity nFeroze = createNode(c, "Feroze Gandhi (MP)", NodeType.PERSON);
        GraphNodeEntity nChagla = createNode(c, "Justice M. C. Chagla", NodeType.PERSON);
        GraphNodeEntity nRichardson = createNode(c, "Richardson & Cruddas Ltd", NodeType.ORGANIZATION);
        GraphNodeEntity nJessop = createNode(c, "Jessop & Company", NodeType.ORGANIZATION);

        createEdge(c, nMundhra, nRichardson, "CONTROLLED", 1.8);
        createEdge(c, nMundhra, nJessop, "CONTROLLED", 1.8);
        createEdge(c, nLIC, nRichardson, "PURCHASED_OVERVALUED_SHARES", 2.0);
        createEdge(c, nLIC, nJessop, "PURCHASED_OVERVALUED_SHARES", 2.0);
        createEdge(c, nFeroze, nLIC, "EXPOSED_CORRUPT_DEAL", 1.5);
        createEdge(c, nChagla, nLIC, "SUBPOENAED_INVESTMENT_DESK", 1.5);

        createAnomaly(c, "AMOUNT_OUTLIER", "Artificial Purchase Premium Above Market Bid", "LIC purchased 1.4 million shares at premiums ranging from 20% to 45% above the prevailing Calcutta Stock Exchange closing bids.", AnomalySeverity.HIGH, 0.95, "Total excess outlay: ₹2,480,000 above market value");
        createAnomaly(c, "DISCREPANCY", "Circular Corporate Shareholding Web", "Mundhra shell companies held reciprocal equity stakes in each other, fabricating apparent liquidity.", AnomalySeverity.MEDIUM, 0.91, "Closed loop cross-holding cycle: 5 entities");

        createSummary(c,
                "In June 1957, the newly nationalized Life Insurance Corporation of India (LIC) invested ₹1.24 crore of public insurance policyholders' money into six financially distressed companies controlled by speculator Haridas Mundhra without convening the statutory LIC Investment Committee.",
                "The purchases were ostensibly justified as stabilizing the market, but forensic auditing of transaction slips revealed that LIC paid inflated prices substantially above the prevailing Calcutta Stock Exchange market prices.",
                "The scandal led to the establishment of the Chagla Commission—the first public commission of inquiry in post-independence India—which established ministerial accountability and the standard of public fund fiduciary duty.",
                "Forensic Guardrail Notice: Grounded strictly in the Chagla Commission Report (1958) and official Lok Sabha parliamentary debate records."
        );
    }

    private void seedLufthansaCase() {
        CaseEntity c = new CaseEntity(
                "CAS-1978-001",
                "Lufthansa Heist (1978)",
                "Armed robbery of approximately $5.875 million ($5 million cash and $875,000 in jewelry) from Lufthansa Cargo Building 261 at John F. Kennedy International Airport, New York, on December 11, 1978, orchestrated by Jimmy Burke.",
                CaseCategory.HEIST_THEFT,
                CaseStatus.CLOSED,
                true,
                Instant.parse("1978-12-11T03:12:00Z")
        );
        c = caseRepository.save(c);

        EvidenceEntity e1 = createEvidence(c, "data/samples/lufthansa_1978/lufthansa_cargo_manifest_vault_log.csv", "lufthansa_cargo_manifest_vault_log.csv", 4, true);
        EvidenceEntity e2 = createEvidence(c, "data/samples/lufthansa_1978/lufthansa_associate_phone_records.csv", "lufthansa_associate_phone_records.csv", 4, true);

        createTimeline(c, e1, Instant.parse("1978-12-10T18:30:00Z"), "Inbound Currency Shipment Lands", "Lufthansa Flight 496 lands from Frankfurt containing $5 million in untraceable U.S. currency destined for exchange.", "PHYSICAL", false);
        createTimeline(c, e2, Instant.parse("1978-12-10T22:40:00Z"), "Inside Code Leak via Cargo Agent", "Lufthansa cargo agent Louis Werner conveys safe combinations and duty schedules to Jimmy Burke's crew.", "COMMUNICATION", true);
        createTimeline(c, e1, Instant.parse("1978-12-11T03:12:00Z"), "Armed Vault Breach", "Six masked gunmen armed with Colt .45s enter Building 261, round up ten employees, and unlock double-vault doors.", "PHYSICAL", false);
        createTimeline(c, e1, Instant.parse("1978-12-11T04:16:00Z"), "Getaway Van Departs JFK", "Gunmen load 72 cartons of cash and jewelry into black Ford Econoline van driven by Parnell 'Stacks' Edwards.", "PHYSICAL", false);
        createTimeline(c, e2, Instant.parse("1978-12-11T08:10:00Z"), "Van Abandoned in Canarsie", "Parnell Edwards fails to deliver van to New Jersey scrap crusher; parks van in a no-parking zone in Canarsie, Brooklyn.", "COMMUNICATION", true);
        createTimeline(c, null, Instant.parse("1978-12-13T10:00:00Z"), "FBI Discovers Fingerprint Evidence", "Abandoned van located by NYPD; FBI latent fingerprint forensics identifies Edwards, unraveling the Burke crew.", "FORENSIC", false);

        GraphNodeEntity nBurke = createNode(c, "James 'Jimmy the Gent' Burke", NodeType.PERSON);
        GraphNodeEntity nWerner = createNode(c, "Louis Werner (Cargo Agent)", NodeType.PERSON);
        GraphNodeEntity nEdwards = createNode(c, "Parnell 'Stacks' Edwards", NodeType.PERSON);
        GraphNodeEntity nBldg = createNode(c, "Lufthansa Cargo Bldg 261", NodeType.LOCATION);
        GraphNodeEntity nLucchese = createNode(c, "Lucchese Crime Family", NodeType.ORGANIZATION);

        createEdge(c, nWerner, nBurke, "PROVIDED_INSIDER_VAULT_CODES", 2.0);
        createEdge(c, nBurke, nEdwards, "ORDERED_VAN_CRUSHING", 1.5);
        createEdge(c, nBurke, nLucchese, "ASSOCIATE_OF", 1.8);
        createEdge(c, nEdwards, nBldg, "DROVE_GETAWAY_VEHICLE_FROM", 2.0);

        createAnomaly(c, "DISCREPANCY", "Vault Double-Key Authentication Without Alarm Trip", "High-security double-key vault breached in under 12 minutes without silent alarm activation, confirming inside insider intelligence.", AnomalySeverity.HIGH, 0.97, "Alarm bypass probability without combination < 0.001%");
        createAnomaly(c, "FREQUENCY_SURGE", "Surge in Queens-Brooklyn Payphone Toll Activity", "Concentration of short-duration calls between Robert's Lounge (Burke) and Brooklyn safehouse immediately prior to breach.", AnomalySeverity.MEDIUM, 0.89, "Call density: 4 calls in 45 min vs baseline 0");

        createSummary(c,
                "On December 11, 1978, an armed robbery at the Lufthansa cargo facility at JFK Airport netted an estimated $5.875 million in cash and gems—at the time the largest cash robbery committed on American soil.",
                "The heist succeeded due to inside information supplied by Lufthansa cargo agent Louis Werner, who owed heavy gambling debts. Werner provided precise schedules and vault combination procedures to James Burke's crew.",
                "The plot unraveled when getaway driver Parnell Edwards failed to deliver the black Ford Econoline van to an auto salvage yard in New Jersey for immediate crushing, abandoning it instead in Canarsie where NYPD recovered fingerprints.",
                "Forensic Guardrail Notice: Compiled from FBI declassified investigative files (Code Name: LUFHEIST) and Federal Court prosecution records (Eastern District of New York)."
        );
    }

    private EvidenceEntity createEvidence(CaseEntity c, String relativePath, String fileName, int records, boolean isSimulated) {
        File file = new File(relativePath);
        long size = file.exists() ? file.length() : (records * 128L);
        String sha256 = computeSha256(file);

        EvidenceEntity e = new EvidenceEntity(
                c,
                fileName,
                "text/csv",
                size,
                sha256,
                file.getAbsolutePath(),
                "timestamp,data",
                records,
                isSimulated
        );
        return evidenceRepository.save(e);
    }

    private void createTimeline(CaseEntity c, EvidenceEntity e, Instant time, String title, String desc, String cat, boolean isAnomaly) {
        TimelineEventEntity te = new TimelineEventEntity(
                c,
                e,
                time,
                title,
                desc,
                cat,
                e != null ? e.getFileName() : "Historical Judicial Record",
                isAnomaly
        );
        timelineEventRepository.save(te);
    }

    private GraphNodeEntity createNode(CaseEntity c, String label, NodeType type) {
        GraphNodeEntity node = new GraphNodeEntity(c, label, type, "{}");
        return graphNodeRepository.save(node);
    }

    private void createEdge(CaseEntity c, GraphNodeEntity source, GraphNodeEntity target, String relationship, double weight) {
        GraphEdgeEntity edge = new GraphEdgeEntity(c, source, target, relationship, weight, "");
        graphEdgeRepository.save(edge);
    }

    private void createAnomaly(CaseEntity c, String type, String title, String desc, AnomalySeverity sev, double conf, String metrics) {
        AnomalyEntity a = new AnomalyEntity(
                c,
                type,
                title,
                desc,
                sev,
                conf,
                metrics
        );
        anomalyRepository.save(a);
    }

    private void createSummary(CaseEntity c, String overview, String findings, String discrepancies, String confidence) {
        AiSummaryEntity s = new AiSummaryEntity(
                c,
                overview,
                findings,
                discrepancies,
                confidence
        );
        aiSummaryRepository.save(s);
    }

    private String computeSha256(File file) {
        if (!file.exists()) {
            return "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(file.toPath());
            byte[] hash = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "0000000000000000000000000000000000000000000000000000000000000000";
        }
    }
}
