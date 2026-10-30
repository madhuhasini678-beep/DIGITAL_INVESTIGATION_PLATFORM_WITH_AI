# HISTORICAL CASES DOSSIER & VERIFIED EVIDENCE SPECIFICATION

The platform preloads six historical investigation cases. To uphold forensic and academic integrity, **verified historical facts** are distinguished from **[DEMO / SIMULATED RECORDS]**.

---

## 1. Mona Lisa Theft (1911)
- **Historical Event**: On August 21, 1911, Leonardo da Vinci's *Mona Lisa* was stolen from the Salon Carré at the Musée du Louvre, Paris.
- **Category**: `HEIST_THEFT`
- **Key Historic Figures**:
  - Vincenzo Peruggia (former Louvre employee/glazier who removed the painting)
  - Louis Béroud (painter who discovered the empty space on August 22)
  - Alfredo Geri (Florentine art dealer contacted by Peruggia in December 1913)
- **Verified Historic Facts**:
  - Stolen on a Monday when the museum was closed for cleaning.
  - The wooden frame and protective glass were discarded on an internal service staircase.
  - Recovery occurred in Florence, Italy, in December 1913.
- **Demo / Simulated Records**:
  - `mona_lisa_museum_guard_rounds.csv`: Simulated timestamped security checkpoints indicating missing rounds between 06:45 and 07:45 CET on 1911-08-21.
  - `mona_lisa_worker_logs.csv`: Worker ingress/egress records flagged with an unmatched exit token.

---

## 2. Lindbergh Kidnapping (1932)
- **Historical Event**: On March 1, 1932, 20-month-old Charles Augustus Lindbergh Jr. was abducted from his nursery in East Amwell, New Jersey.
- **Category**: `KIDNAPPING_ABDUCTION`
- **Key Historic Figures**:
  - Charles Lindbergh & Anne Morrow Lindbergh (Parents)
  - Bruno Richard Hauptmann (Convicted kidnapper)
  - Arthur Koehler (US Forest Service wood technologist)
- **Verified Historic Facts**:
  - A handmade wooden ladder was abandoned 60 to 70 feet from the house.
  - A $50,000 ransom was paid in US Gold Certificates, whose serial numbers were recorded.
  - Wood forensics traced the attic floorboard grain to the ladder rungs.
- **Demo / Simulated Records**:
  - `lindbergh_gold_certificate_serial_tracker.csv`: Tracking transactions of intercepted gold certificates at Bronx gas stations and merchants in 1933–1934.
  - `lindbergh_ransom_notes_analysis.csv`: Linguistic and handwriting discrepancy matrix.

---

## 3. Harshad Mehta Securities Scam (1992)
- **Historical Event**: Massive financial manipulation of the Bombay Stock Exchange (BSE) securities market through fake Bank Receipts (BRs) and ready forward (RF) inter-bank deals.
- **Category**: `FINANCIAL_FRAUD`
- **Key Historic Figures**:
  - Harshad Mehta (Stockbroker)
  - Sucheta Dalal (Times of India investigative journalist who broke the story on April 23, 1992)
  - Key Banks: Bank of Karad, Metropolitan Cooperative Bank, National Housing Bank (NHB), State Bank of India (SBI)
- **Verified Historic Facts**:
  - Fake Bank Receipts (BRs) were issued with no underlying government securities backing them.
  - Funds were routed into broker accounts to aggressively ramp stock prices (e.g. ACC from ₹200 to ₹9,000).
- **Demo / Simulated Records**:
  - `harshad_mehta_interbank_br_transactions.csv`: Simulated ledger of 184 ready-forward deals showing non-reconciled collateral obligations.
  - `harshad_mehta_cheque_flows.csv`: Cheque routing showing cyclical fund flows between bank treasury desks and broker accounts.

---

## 4. Bhopal Gas Tragedy (1984)
- **Historical Event**: On the night of December 2–3, 1984, over 40 metric tons of toxic Methyl Isocyanate (MIC) gas leaked from the Union Carbide India Limited (UCIL) pesticide plant in Bhopal, Madhya Pradesh, India.
- **Category**: `INDUSTRIAL_DISASTER`
- **Verified Historic Facts**:
  - Water ingress into Tank E610 triggered an exothermic runaway reaction.
  - Critical safety barriers were non-operational or offline:
    - Tank refrigeration system was shut down to save electricity.
    - Vent gas scrubber was deactivated or under-charged with caustic soda.
    - Flare tower was dismantled for pipe maintenance.
- **Demo / Simulated Records**:
  - `bhopal_tank610_telemetry_log.csv`: Sensor records showing pressure escalating from 2 psi to 55+ psi between 22:30 and 00:45 IST.
  - `bhopal_safety_maintenance_deferrals.csv`: Tabular audit of maintenance deferrals on safety-critical equipment.

---

## 5. Mundhra Scandal (1957)
- **Historical Event**: Financial irregularity wherein the state-owned Life Insurance Corporation of India (LIC) invested ₹1.24 crore (12.4 million rupees) into fraudulent or financially distressed companies controlled by industrialist Haridas Mundhra.
- **Category**: `FINANCIAL_FRAUD`
- **Key Historic Figures**:
  - Haridas Mundhra (Calcutta industrialist/financier)
  - Feroze Gandhi (Member of Parliament who exposed the scandal in the Lok Sabha on Dec 16, 1957)
  - Justice M. C. Chagla (Chaired the landmark open public commission of inquiry)
  - T. T. Krishnamachari (Finance Minister who subsequently resigned)
- **Verified Historic Facts**:
  - Bypassed the LIC Investment Committee regulations.
  - Purchases targeted companies like Richardson & Cruddas, Jessop & Co., and BIC with inflated market valuations.
- **Demo / Simulated Records**:
  - `mundhra_lic_share_purchases.csv`: Purchase ledger detailing stock transaction prices vs. prevailing fair-market closing bids.
  - `mundhra_corporate_circular_holdings.csv`: Network data tracking cross-holdings between Mundhra-controlled shell firms.

---

## 6. Lufthansa Heist (1978)
- **Historical Event**: On December 11, 1978, armed masked gunmen stole approximately $5.875 million ($5 million cash and $875,000 jewelry) from Lufthansa Cargo Building 261 at JFK International Airport, New York.
- **Category**: `HEIST_THEFT`
- **Key Historic Figures**:
  - Jimmy Burke ("Jimmy the Gent", Lucchese crime family associate and mastermind)
  - Louis Werner (Lufthansa cargo agent / inside informant)
  - Parnell "Stacks" Edwards (Getaway van driver)
- **Verified Historic Facts**:
  - Inside information on vault security codes and flight currency shipments was provided by cargo insider Louis Werner.
  - Getaway Ford Econoline van was supposed to be taken to a New Jersey scrap yard for immediate crushing, but was instead abandoned in Canarsie, Brooklyn, leading to identification of the crew.
- **Demo / Simulated Records**:
  - `lufthansa_cargo_manifest_vault_log.csv`: Simulated vault ledger for Building 261 recording inbound flight arrivals and customs handoffs.
  - `lufthansa_associate_phone_records.csv`: Simulated telecommunications metadata between Jamaica, Queens and Canarsie, Brooklyn around the time of the heist.
