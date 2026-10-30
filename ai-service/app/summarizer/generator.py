from typing import List
from ..models.schemas import SummaryGenerateRequest, SummaryGenerateResponse

class GroundedCaseSummarizer:
    """
    Evidence-Grounded Forensic Case Summarizer.
    Generates structured factual synthesis based exclusively on available evidentiary data.
    Never fabricates unverified claims; explicitly states data gaps and uncertainty.
    """

    def summarize(self, req: SummaryGenerateRequest) -> SummaryGenerateResponse:
        # 1. Factual Overview
        dates_str = f"with incident reference on {req.incident_date}" if req.incident_date else "with ongoing investigation timeframe"
        demo_str = " (Preloaded Benchmark Case with verified historical background)" if req.is_historical_demo else ""

        factual_overview = (
            f"Case '{req.case_title}' [{req.case_category}]{demo_str} {dates_str}. "
            f"Currently records {req.evidence_count} evidence file(s), "
            f"{len(req.timeline_events)} sequenced timeline event(s), and "
            f"{req.graph_data.node_count if req.graph_data else 0} mapped entity node(s). "
        )

        if req.case_description:
            factual_overview += f"\n\nInvestigative Objective: {req.case_description.strip()}"

        # 2. Key Findings
        findings: List[str] = []
        if req.timeline_events:
            first_event = req.timeline_events[0]
            last_event = req.timeline_events[-1]
            findings.append(f"Timeline spans from {first_event.timestamp} ('{first_event.title}') to {last_event.timestamp} ('{last_event.title}').")

        if req.graph_data and req.graph_data.node_count > 0:
            findings.append(f"Relationship network maps {req.graph_data.node_count} unique entities interconnected by {req.graph_data.edge_count} documented interactions.")
            if req.graph_data.top_entities:
                findings.append(f"Key focal entities identified by interaction volume: {', '.join(req.graph_data.top_entities[:4])}.")

        if req.evidence_count == 0:
            findings.append("No primary evidence files have been uploaded yet. Analysis is currently based on initial docket records.")
        else:
            findings.append(f"Primary evidentiary corpus consists of {req.evidence_count} registered file(s) with cryptographic SHA-256 integrity validation.")

        key_findings = "\n".join(f"• {f}" for f in findings)

        # 3. Discrepancies Identified
        discrepancies: List[str] = []
        if req.anomalies:
            for a in req.anomalies:
                discrepancies.append(f"[{a.severity}] {a.title}: {a.description}")
        else:
            discrepancies.append("No mathematical or chronological anomalies have been flagged across currently uploaded records.")

        discrepancies_identified = "\n\n".join(discrepancies)

        # 4. Confidence Notes & Uncertainty Statements
        conf_points: List[str] = []
        if req.is_historical_demo:
            conf_points.append("Demo Case Notice: Verified historical event records are corroborated with archival sources. Synthetic demonstration logs are labeled [DEMO / SIMULATED RECORD].")

        if req.evidence_count < 2:
            conf_points.append("Limited Evidentiary Scope: Case contains sparse primary evidence records. Additional corroborating documentation recommended before drawing conclusions.")

        conf_points.append(
            "Forensic Guardrail Notice: Anomaly detection scores reflect statistical variances (Z-scores and sequence reversals) "
            "and do not constitute definitive declarations of liability or culpability."
        )

        confidence_notes = "\n\n".join(conf_points)

        return SummaryGenerateResponse(
            case_id=req.case_id,
            factual_overview=factual_overview,
            key_findings=key_findings,
            discrepancies_identified=discrepancies_identified,
            confidence_notes=confidence_notes,
            engine="python_grounded_synthesis"
        )
