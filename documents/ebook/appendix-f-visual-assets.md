# Appendix F: Visual Asset Prompts {#appendix-f}

This appendix contains all **139 visual asset prompts** referenced throughout the e-book. Each prompt is designed for
generation using AI image services (DALL-E, Midjourney, Stable Diffusion) with the corporate template
specifications documented in `documents/ebook/scripts/IMAGE-GENERATION-GUIDE.md`.

## Purpose

These prompts are extracted from the main chapters to:

- Keep chapter content focused on technical information
- Provide a centralized reference for image generation
- Enable batch processing with AI services
- Maintain consistent visual style across the e-book

## Corporate Template Specifications

**Color Scheme**:
- **Primary Blues**: #0277BD, #0288D1, #03A9F4
- **Secondary Greens**: #388E3C, #4CAF50
- **Accent Orange**: #F57C00, #FF9800
- **Neutral Grays**: #424242, #616161, #9E9E9E

**Image Format**: PNG (1920x1080 for diagrams, 1200x800 for charts)
**Style**: Professional, clean, modern with OJP branding
**Naming Convention**: `{chapter}-{prompt-number}.png`

## How to Use This Appendix

Each prompt entry includes:
- **Prompt Number**: Sequential number for reference
- **Chapter & Section**: Where the image should appear in the main text
- **Prompt Text**: Detailed description for image generation

To integrate generated images back into the e-book chapters, use the `update-image-references.py` script in
`documents/ebook/scripts/`. See the IMAGE-GENERATION-GUIDE.md for complete workflow instructions.

---

## Image Prompts by Chapter

### Chapter 1: Introduction

**File**: `part1-chapter1-introduction.md`
**Total Images**: 12 visual assets

#### Section: 1.1 What is Open J Proxy?

**Prompt 1**
Create a diagram showing the three types of JDBC drivers:

**Prompt 2**
Create an illustration showing the OSI model layers (1-7) on the left side, with Layer 7 (Application Layer) highlighted. On the right, show OJP operating at Layer 7, intercepting JDBC/SQL operations and intelligently routing them to a database pool. Use professional network diagram style with clear layer separation. Include labels: "HTTP", "SQL", "JDBC" at Layer 7.

#### Section: 1.2 The Problem OJP Solves

**Prompt 3**
Create a dramatic "before and after" comparison:

#### Section: 1.3 How OJP Works

**Prompt 4**
Create a detailed technical diagram:

**Prompt 5**
Create an infographic showing a flow control system:

**Prompt 6**
Create a diagram showing OJP Server at the center connected to multiple different databases:

#### Section: 1.4 Key Features and Benefits

**Prompt 7**
Create a comparison chart/graph:

#### Section: 1.7 Is OJP Right for You? Workload Fit and Risk Assessment

**Prompt 16**
Create a comparison matrix showing three workload types (OLTP, Mixed, Batch) rated across multiple dimensions: Connection Efficiency, Latency Sensitivity, Transaction Patterns, Scale Elasticity. Use traffic light colors (green/yellow/red) to show fit. OLTP and Mixed workloads should be mostly green, while Batch should show mixed results. Professional business matrix style.

**Prompt 17**
Create warning-style infographic showing anti-patterns: ultra-low latency trading (with clock icon showing microseconds), data warehousing (with large database icon), single monolithic app (one big server), embedded systems (small device icon). Use red/orange warning colors. Professional warning poster style.

**Prompt 18**
Create a balanced scale diagram showing trade-offs: Left side "What You Gain" (scalability, centralization, backpressure), Right side "What You Accept" (latency overhead, complexity, dependency). Use professional illustration style with icons representing each concept.

**Prompt 19**
Create a fault-tree diagram showing new failure scenarios: OJP Server down, network partition, gRPC issues, pool exhaustion. Use red indicators for failure points and yellow for degraded states. Include mitigation strategies in green boxes. Professional risk assessment diagram style.

**Prompt 20**
Create a decision tree flowchart starting with "Considering OJP?" and branching through key questions: "Multiple app instances?", "OLTP workload?", "Can accept 1-3ms latency?", "Need elastic scaling?". Green paths lead to "OJP is a good fit", red paths lead to "Consider alternatives". Professional flowchart style with clear yes/no branches.

---

### Chapter 2: Architecture

**File**: `part1-chapter2-architecture.md`
**Total Images**: 13 visual assets

#### Section: 2.1 System Components

**Prompt 1**
Create a detailed component diagram of ojp-server showing:

**Prompt 2**
Create a layered diagram showing:

**Prompt 3**
Create a diagram showing Protocol Buffers (.proto files) in the center, with arrows pointing to:

#### Section: 2.2 Communication Protocol

**Prompt 4**
Create a comparison infographic:

**Prompt 5**
Create a detailed sequence diagram showing:

**Prompt 6**
Create a diagram showing:

**Prompt 7**
Create a technical diagram showing:

#### Section: 2.3 Connection Pool Management

**Prompt 8**
Create a performance comparison chart:

**Prompt 9**
Create an infographic showing optimal pool sizing:

**Prompt 10**
Create an architecture diagram showing:

#### Section: 2.4 Architecture Diagrams

**Prompt 11**
Create a comprehensive end-to-end architecture diagram showing:

**Prompt 12**
Create a detailed component interaction diagram showing:

**Prompt 13**
Create a data flow diagram (DFD) showing:

---

### Chapter 3: Quickstart

**File**: `part1-chapter3-quickstart.md`
**Total Images**: 9 visual assets

#### Section: 3.1 Prerequisites

**Prompt 1**
Create a simple requirements checklist infographic:

#### Section: 3.2 Installation Options

**Prompt 2**
Create a step-by-step visual guide showing:

**Prompt 3**
Create a visual guide for JAR deployment showing:

**Prompt 4**
Create a development workflow diagram showing:

#### Section: 3.3 Your First OJP Connection

**Prompt 5**
Create a side-by-side comparison showing:

**Prompt 6**
Create a before/after transformation diagram:

**Prompt 7**
Create a complete code example visualization showing:

#### Section: 3.4 Common Gotchas

**Prompt 8**
Create a troubleshooting flowchart or FAQ-style infographic:

#### Section: Testing Your Setup

**Prompt 9**
Create a verification checklist infographic:

---

### Chapter 3a: Kubernetes Helm

**File**: `part1-chapter3a-kubernetes-helm.md`
**Total Images**: 18 visual assets

#### Section: 3a.1 Kubernetes Prerequisites

**Prompt 1**
Create a prerequisites checklist infographic showing:

**Prompt 2**
Create a visual guide showing Helm installation on different platforms:

#### Section: 3a.2 Installing OJP Server with Helm

**Prompt 3**
Create a step-by-step visual guide showing:

**Prompt 4**
Create a Kubernetes deployment visualization showing:

**Prompt 5**
Create a side-by-side comparison showing:

**Prompt 6**
Create a visual representation of Helm upgrade process:

#### Section: 3a.3 Helm Chart Configuration

**Prompt 7**
Create an infographic showing configuration categories:

**Prompt 8**
Create a diagram showing OJP StatefulSet service architecture:

#### Section: 3a.4 Advanced Kubernetes Deployment

**Prompt 9**
Create a diagram showing:

**Prompt 10**
Create a security-focused diagram showing:

**Prompt 11**
Create a diagram showing:

**Prompt 12**
Create a network security diagram showing:

**Prompt 13**
Create an architecture diagram showing:

#### Section: 3a.5 Kubernetes Best Practices

**Prompt 14**
Create a diagram showing Kubernetes health check lifecycle:

**Prompt 15**
Create a visual representation of rolling update strategy for OJP StatefulSet:

**Prompt 16**
Create a comprehensive monitoring stack diagram:

**Prompt 17**
Create a high-availability deployment diagram showing:

**Prompt 18**
Create a production readiness checklist infographic:

---

### Chapter 4: Database Drivers

**File**: `part2-chapter4-database-drivers.md`
**Total Images**: 17 visual assets

#### Section: 4.1 Open Source Drivers

**Prompt 1**
Create an infographic showing the four included open-source databases:

**Prompt 2**
Create a step-by-step visual guide showing:

**Prompt 3**
Create a visual showing the download-drivers.sh script in action:

**Prompt 4**
Create a verification checklist infographic showing:

#### Section: 4.2 Proprietary Database Drivers

**Prompt 5**
Create a database support matrix infographic:

**Prompt 6**
Create a step-by-step Oracle setup guide:

**Prompt 7**
Create a diagram showing Oracle UCP integration:

**Prompt 8**
Create a SQL Server setup guide:

**Prompt 9**
Create a DB2 setup guide:

**Prompt 10**
Create an infographic showing CockroachDB compatibility:

#### Section: 4.3 Drop-In External Libraries

**Prompt 11**
Create an architecture diagram showing:

**Prompt 12**
Create a use case diagram showing different types of libraries:

#### Section: 4.4 Testing Database Connections

**Prompt 13**
Create a testing workflow diagram:

**Prompt 14**
Create a quick H2 testing guide:

**Prompt 15**
Create a Docker Compose architecture diagram:

**Prompt 16**
Create a Testcontainers workflow diagram:

**Prompt 17**
Create a visual test utility interface mockup:

---

### Chapter 5: Jdbc Configuration

**File**: `part2-chapter5-jdbc-configuration.md`
**Total Images**: 11 visual assets

#### Section: 5.1 JDBC URL Format

**Prompt 1**
Create a detailed URL anatomy diagram:

**Prompt 2**
Create a reference card showing URL examples for all major databases:

**Prompt 3**
Create an infographic showing how parameters are passed through:

#### Section: 5.2 Connection Pool Settings

**Prompt 4**
Create a file location diagram showing:

**Prompt 5**
Create a configuration example showing:

**Prompt 6**
Create a comprehensive configuration reference table:

**Prompt 7**
Create an infographic showing pool sizing recommendations:

#### Section: 5.3 Client-Side Configuration

**Prompt 8**
Create a code example showing:

**Prompt 9**
Create a multi-environment configuration diagram:

#### Section: 5.4 Framework Integration Best Practices

**Prompt 10**
Create a "do's and don'ts" infographic:

**Prompt 12**
Create a transaction flow diagram:

---

### Chapter 6: Server Configuration

**File**: `part2-chapter6-server-configuration.md`
**Total Images**: 12 visual assets

#### Section: 6.1 Understanding Configuration Hierarchy

**Prompt 1**
Create a layered diagram showing configuration hierarchy with three levels: "JVM System Properties" at the top (highest priority, shown in bold color), "Environment Variables" in the middle (medium priority), and "Default Values" at the bottom (lowest priority, shown in faded color). Use arrows flowing upward labeled "Overrides" to show precedence. Include example values at each level like `-Dojp.server.port=8080`, `OJP_SERVER_PORT=1059`, and `default: 1059`. Style: Clean, hierarchical infographic with color-coded priority levels.

#### Section: 6.2 Core Server Settings

**Prompt 2**
Create a technical server architecture diagram showing OJP Server as a central component with two network interfaces: one labeled "gRPC Port :1059" (shown with database connection icons) and another labeled "Prometheus Port :9159" (shown with metrics/monitoring icons). Include a thread pool visualization showing multiple worker threads (default: 200) handling concurrent requests. Use professional blue and gray color scheme with clear labels and connection lines. Style: Modern technical architecture diagram.

#### Section: 6.3 Security Configuration

**Prompt 3**
Create a network security diagram showing OJP Server at the center with two separate firewalls/shields. The left shield guards "gRPC Endpoint" with rules like "192.168.1.0/24" and "10.0.0.0/8" (labeled "Application Network"). The right shield guards "Prometheus Endpoint" with rules like "192.168.100.0/24" (labeled "Monitoring Network"). Show blocked connections (red X) and allowed connections (green checkmark) from different IP ranges. Style: Professional security diagram with red, green, and blue color coding.

**Prompt 4**
Create a flowchart diagram showing IP access control decision process. Start with "Incoming Connection" at top, flow through "Extract Client IP", then "Check Against Whitelist Rules" (shown as a list icon with CIDR rules), then a diamond decision "Match Found?". On "Yes" path: "Allow Connection" (green box), on "No" path: "Reject Connection" (red box) with "Log Security Event" beneath it. Style: Clean technical flowchart with green/red color coding for accept/reject paths.

#### Section: 6.4 Logging and Debugging

**Prompt 5**
Create a layered visualization showing three log level views of the same event. Top layer labeled "INFO" shows a single line: "Connection established to PostgreSQL". Middle layer labeled "DEBUG" shows INFO plus 3-4 additional lines with connection details. Bottom layer labeled "TRACE" shows DEBUG plus many detailed lines including raw protocol messages and timestamps. Use different background shades (light, medium, dark) to distinguish layers. Style: Code editor-style display with monospace font and subtle line numbers.

#### Section: 6.5 OpenTelemetry Integration

**Prompt 6**
Create a distributed tracing visualization showing a waterfall diagram of nested spans. Start with "HTTP Request" span at top, followed by nested "JDBC Query" span, then "OJP Server Processing" span (highlighted), then "Database Query" span at bottom. Show timing bars for each span with duration labels. Include trace ID and span IDs. Use different colors for each service layer (blue for app, orange for OJP, green for database). Style: Modern APM tool waterfall display with timing metrics.

#### Section: 6.6 Circuit Breaker Configuration

**Prompt 7**
Create a state machine diagram showing circuit breaker states. Three circles labeled "CLOSED" (green), "OPEN" (red), and "HALF-OPEN" (yellow). Arrows between states: "CLOSED to OPEN" labeled "3 failures" (configurable threshold), "OPEN to HALF-OPEN" labeled "60s timeout", "HALF-OPEN to CLOSED" labeled "Success", "HALF-OPEN to OPEN" labeled "Failure". Include small icons: green checkmark for CLOSED, red X for OPEN, yellow caution symbol for HALF-OPEN. Style: Clean state diagram with color-coded states and clear transition labels.

#### Section: 6.7 Slow Query Segregation

**Prompt 8**
Create a side-by-side comparison showing connection pool behavior. Left side labeled "Without Segregation": single queue with fast queries (lightning bolt icons) blocked behind slow queries (turtle icons), showing red warning indicators. Right side labeled "With Segregation": two separate queues, top queue "Fast Slots (80%)" with lightning bolts flowing freely, bottom queue "Slow Slots (20%)" with turtle icons, showing green success indicators. Style: Before/after comparison with color-coded performance indicators.

**Prompt 9**
Create a dynamic allocation diagram showing how idle slots can be borrowed between pools. Show two pools: "Fast Slots" (4 boxes, 3 active, 1 idle) and "Slow Slots" (2 boxes, 1 active, 1 idle). Draw a curved arrow labeled "Temporary Borrow (if idle >10s)" from the idle slow slot to fast pool. Include a timer icon and "Returns when fast demand drops" annotation. Use green for active, gray for idle, and dotted lines for temporary borrowing. Style: Technical system diagram with clear state visualization.

#### Section: 6.8 Configuration Best Practices

**Prompt 10**
Create a comparison table visualization showing recommended configurations for three environments. Three columns labeled "Development", "Staging", and "Production". Rows for key settings like Log Level (DEBUG/INFO/INFO), Security (Open/Restricted/Locked Down), Telemetry (Optional/Enabled/Required), Thread Pool (Low/Medium/High), Circuit Breaker (Tolerant/Balanced/Strict). Use color coding: green for development-friendly, yellow for balanced, red for production-strict. Style: Professional configuration matrix with clear visual hierarchy.

#### Section: 6.9 Configuration Validation and Troubleshooting

**Prompt 11**
Create a troubleshooting flowchart for common configuration issues. Start with "Server Won't Start" diamond. Branch to "Check Logs" which shows code snippet of startup logs. From there, multiple paths: "Port Conflict?" leads to "Change Port", "Invalid IP Format?" leads to "Fix CIDR Syntax", "Missing Required Value?" leads to "Set Property". Each resolution path leads back to "Restart Server" then "Verify Success" (green checkmark). Style: Decision tree flowchart with code snippets and clear resolution paths.

#### Section: Summary

**Prompt 12**
Create a summary mind map with "OJP Server Configuration" at the center. Six main branches radiating outward: "Core Settings" (server icon), "Security" (lock icon), "Logging" (document icon), "Telemetry" (graph icon), "Circuit Breaker" (shield icon), and "Slow Query Segregation" (speedometer icon). Each branch has 2-3 sub-branches with key points. Use colors to group related concepts and make it visually hierarchical. Style: Modern mind map with icons and color coding.

---

### Chapter 7: Framework Integration

**File**: `part2-chapter7-framework-integration.md`
**Total Images**: 8 visual assets

#### Section: 7.1 Understanding Framework Integration Patterns

**Prompt 1**
Create a before/after architecture diagram comparing traditional vs OJP connection management. Top half "Before OJP": Show 3 application instances, each with its own HikariCP pool (mini pool icons) connecting to database, resulting in 3 separate connection pools. Label: "Each app maintains separate pool, difficult to coordinate". Bottom half "With OJP": Show 3 application instances with thin dotted lines to central OJP Server (large pool icon), which connects to database with single consolidated pool. Label: "Centralized pooling, coordinated management". Style: Clear before/after comparison with color coding—red for problematic pattern, green for improved pattern.

#### Section: 7.2 Spring Boot Integration

**Prompt 2**
Create a side-by-side code comparison showing application.properties transformation. Left side labeled "Before OJP": Shows traditional config with `spring.datasource.url=jdbc:postgresql://localhost:5432/mydb` and default HikariCP settings. Right side labeled "With OJP": Shows `spring.datasource.url=jdbc:ojp[localhost:1059]_postgresql://localhost:5432/mydb`, `spring.datasource.driver-class-name=org.openjproxy.jdbc.Driver`, and `spring.datasource.type=SimpleDriverDataSource`. Highlight the changes with arrows and annotations. Style: Clean code comparison with syntax highlighting and change indicators.

#### Section: 7.3 Quarkus Integration

**Prompt 3**
Create a Quarkus-specific configuration diagram showing the application.properties file with key settings highlighted. Show three configuration layers: top layer "JDBC Enabled" (checkmark icon), middle layer "Unpooled Mode" (crossed-out pool icon), bottom layer "OJP URL & Driver" (connection icon). Use arrows to show how these settings work together to disable local pooling while enabling OJP connectivity. Style: Layered technical diagram with clear annotations.

#### Section: 7.4 Micronaut Integration

**Prompt 4**
Create a code visualization showing the Micronaut DataSource factory pattern. Show a class diagram-style representation with DataSourceFactory at the top, connecting to a simple DataSource implementation below, which connects to DriverManager. Highlight that this pattern bypasses HikariCP pooling. Use arrows labeled "Creates", "Returns", and "Uses" to show relationships. Include code snippets for key methods. Style: Clean UML-style class diagram with code integration.

#### Section: 7.5 Framework Comparison and Trade-offs

**Prompt 5**
Create a comparison matrix showing the three frameworks. Three columns (Spring Boot, Quarkus, Micronaut), rows for: "Configuration Complexity" (Low/Low/Medium), "Custom Code Required" (None/None/Factory Class), "Native Compilation" (Limited/Excellent/Good), "Ecosystem Maturity" (Highest/Growing/Moderate), "Integration Smoothness" (Smoothest/Smooth/Moderate). Use color coding: green for best, yellow for good, orange for moderate. Style: Professional comparison matrix with visual indicators.

#### Section: 7.6 Integration Best Practices

**Prompt 6**
Create a timing diagram showing timeout coordination. Show three horizontal bars representing different timeout layers from top to bottom: "Framework Datasource Timeout" (90s), "OJP Connection Acquisition" (60s), and "Database Query Timeout" (30s). Use arrows and labels to show cascading timeout behavior: if database times out, OJP catches it; if OJP times out, framework catches it. Style: Technical timing diagram with color-coded layers and clear duration labels.

#### Section: 7.7 Migration from Existing Applications

**Prompt 7**
Create a migration roadmap showing five phases as connected blocks: "1. Deploy OJP Server" → "2. Test Connection" → "3. Update Config" → "4. Test Application" → "5. Monitor Production". Each block contains 2-3 checkpoints. Use a timeline-style layout with checkmarks for completed steps and circles for pending steps. Include risk indicators (red/yellow/green) for each phase. Style: Project management roadmap with clear progression.

#### Section: Summary

**Prompt 8**
Create a summary diagram showing the three frameworks (Spring Boot, Quarkus, Micronaut logos) all connecting to a central OJP Server icon, which then connects to a database. Above each framework, show the key integration requirements in small text: "Exclude HikariCP + SimpleDriverDataSource", "Unpooled=true", "Custom DataSource Factory". Below the database, show benefits: "Centralized Pooling", "Coordinated Management", "Transparent to App Code". Style: Clean architectural summary with icons and clear relationships.

---

### Chapter 10: Xa Transactions

**File**: `part3-chapter10-xa-transactions.md`
**Total Images**: 10 visual assets

#### Section: Understanding XA Transactions

**Prompt 1**
Two-Phase Commit Protocol Flow Diagram

#### Section: OJP's XA Architecture

**Prompt 2**
Client-Side XA Component Architecture

**Prompt 3**
Dual-Condition Lifecycle State Diagram

#### Section: Configuration and Setup

**Prompt 4**
Spring XA Transaction Flow

#### Section: Performance Characteristics

**Prompt 5**
Performance Comparison Chart

#### Section: Multinode XA Coordination

**Prompt 6**
Multinode XA Coordination Diagram

#### Section: XA Pool Housekeeping

**Prompt 7**
XA Pool Housekeeping Dashboard

**Prompt 8**
Housekeeping Features Comparison Matrix

#### Section: 10.9 XA Guarantees and Limitations: What OJP Can and Cannot Promise

**Prompt 9**
XA Guarantees Spectrum Diagram

#### Section: Summary

**Prompt 10**
Chapter Summary Infographic

---

### Chapter 11: Security

**File**: `part3-chapter11-security.md`
**Total Images**: 8 visual assets

#### Section: 11.1 Security Overview

**Prompt 1**
Create a multi-layered security diagram showing OJP Server at the center with four concentric rings/shields: outermost ring labeled "Network Segregation" (showing isolated network zones), second ring "IP Whitelisting" (showing firewall rules), third ring "TLS Encryption" (showing encrypted tunnel), innermost ring "mTLS Authentication" (showing certificate exchange). Use blue/green color gradient from outer to inner rings. Show threat types blocked at each layer (network intrusion, unauthorized access, eavesdropping, impersonation). Style: Professional infographic with clear layer separation and security shield icons.

#### Section: 11.2 SSL/TLS Between OJP Server and Databases

**Prompt 2**
Create a before/after comparison diagram. LEFT side shows "Without TLS" - OJP Server connected to Database with transparent pipe showing readable SQL queries, passwords, and data flowing (labeled "Plaintext - Vulnerable to Interception"). RIGHT side shows "With TLS" - same connection but with opaque encrypted tunnel, showing padlock icons and encrypted data symbols (labeled "Encrypted - Protected from Eavesdropping"). Include a hacker icon with X on the right side showing blocked attack. Use red for left, green for right. Style: Clear visual comparison with security indicators.

#### Section: 11.3 mTLS Between JDBC Driver and OJP Server

**Prompt 3**
Create a sequence diagram showing mTLS handshake between Client Application and OJP Server. Show 6 steps: 1) Client initiates connection, 2) Server sends its certificate, 3) Client validates server certificate against CA, 4) Client sends its certificate, 5) Server validates client certificate against CA, 6) Encrypted channel established. Use different colors for client-to-server (blue) and server-to-client (green) messages. Show certificate icons and CA validation steps. Include padlock icon when channel is established. Style: Clear technical sequence diagram with labeled steps.

**Prompt 4**
Create a certificate lifecycle flowchart showing: "Certificate Generation" (day 0) → "Deployment" (day 1) → "Active Use" (days 2-335) → "Expiration Warning" (day 335, yellow alert) → "Rotation Begins" (day 350, orange) → "New Certificate Active" (day 360) → "Old Certificate Removed" (day 365+30). Show calendar icons and color progression from green (early) to yellow (warning) to orange (critical). Include automation arrows for monitoring and rotation. Style: Process flowchart with time-based progression and alert indicators.

#### Section: 11.4 Network Segregation Patterns

**Prompt 5**
Create a network topology diagram showing three zones from top to bottom: "Internet" (cloud icon) → "Application Network" zone (containing 3 app servers and 1 OJP server in light blue box) → "Database Network" zone (containing 2 database servers in darker blue box). Show firewall icons between zones. Highlight the connection path from App Servers → OJP (local network, green) and OJP → Database (cross-zone, orange with firewall). Include latency indicators (~0.5ms local, ~2ms cross-zone). Style: Clean network diagram with zone separation and traffic flow indicators.

**Prompt 6**
Create a network topology diagram showing: "Application Network" zone (top, containing 4 app servers in light gray box) connected via firewall to "Database Network" zone (bottom, containing 1 OJP server and 3 database servers in dark blue box). Show firewall rules explicitly: "Allow gRPC :1059 from App Network" and "Deny All Other Traffic". Highlight App→OJP path (cross-boundary, orange with latency ~3ms) and OJP→DB path (local, green with latency <1ms). Show shield icons on Database Network. Style: Professional architecture diagram with security emphasis and clear zone boundaries.

**Prompt 7**
Create a three-tier network diagram showing: "Application Network" (top tier, light blue with app servers) → Firewall 1 → "Middleware Network" (middle tier, green with OJP servers) → Firewall 2 → "Database Network" (bottom tier, dark blue with databases). Show traffic flow vertically with latency annotations (~2ms per firewall hop). Include monitoring icons on each firewall. Show "Security Zone 1", "Security Zone 2", "Security Zone 3" labels. Highlight that app-to-database requires crossing two boundaries. Style: Enterprise architecture diagram with emphasis on defense in depth and zone isolation.

#### Section: Summary

**Prompt 8**
Create a comprehensive security checklist infographic with 5 main sections arranged vertically, each with icons and checkboxes: 1) "Transport Security" (padlock icon) - TLS encryption enabled, Certificates valid, Strong cipher suites; 2) "Authentication" (key icon) - mTLS configured, Client certificates issued, Certificate rotation scheduled; 3) "Network Security" (firewall icon) - IP whitelist configured, Network segregation implemented, Firewall rules tested; 4) "Operations" (gear icon) - Secrets in vault, Audit logging enabled, Monitoring configured; 5) "Compliance" (clipboard icon) - Regular assessments, Policies documented, Team trained. Use green checkmarks for completed items. Style: Professional checklist poster with clear hierarchy and actionable items.

---

### Chapter 12: Pool Provider Spi

**File**: `part3-chapter12-pool-provider-spi.md`
**Total Images**: 8 visual assets

#### Section: 12.1 Understanding the Pool Abstraction

**Prompt 1**
Connection Pool Abstraction Architecture

#### Section: 12.2 Built-in Pool Providers

**Prompt 2**
HikariCP vs DBCP Comparison Matrix

#### Section: 12.3 Configuration and Discovery

**Prompt 3**
Configuration Flow Diagram

#### Section: 12.4 Monitoring and Statistics

**Prompt 4**
Monitoring Dashboard Mockup

#### Section: 12.5 Building Custom Providers

**Prompt 5**
Custom Provider Development Workflow

#### Section: 12.6 Real-World Custom Provider Examples

**Prompt 6**
Circuit Breaker State Machine

#### Section: 12.9 SQL Enhancer Engine

**Prompt 7**
SQL Enhancement Flow Diagram

**Prompt 8**
SQL Enhancer Monitoring Dashboard Concept

---

### Chapter 20: Implementation Analysis

**File**: `part6-chapter20-implementation-analysis.md`
**Total Images**: 5 visual assets

#### Section: 20.1 Driver Externalization

**Prompt 1**
Driver Externalization Architecture

#### Section: 20.2 Pool Disable Feature

**Prompt 2**
Pool Disable Configuration Options

**Prompt 3**
Pooling Decision Flow

#### Section: 20.3 XA Pool SPI

**Prompt 4**
XA Pool SPI Architecture

**Prompt 5**
Provider Selection and Configuration

---

### Chapter 21: Lessons Learned

**File**: `part6-chapter21-lessons-learned.md`
**Total Images**: 4 visual assets

#### Section: 21.1 Issue #29: The Indefinite Blocking Crisis

**Prompt 1**
Thread Blocking Visualization

**Prompt 2**
Before and After Comparison

#### Section: 21.2 Multinode Connection Redistribution

**Prompt 3**
Connection Redistribution Diagram

#### Section: 21.3 Production Patterns and Insights

**Prompt 4**
Lessons Learned Infographic

---

### Chapter 22: Vision Future

**File**: `part7-chapter22-vision-future.md`
**Total Images**: 4 visual assets

#### Section: 22.1 The Vision: Database-Agnostic Connection Management

**Prompt 1**
Vision Illustration - Decoupled Scaling

#### Section: 22.2 Current Limitations and Trade-offs

**Prompt 2**
Limitations vs Solutions Matrix

#### Section: 22.3 Future Enhancements and Roadmap

**Prompt 3**
Roadmap Timeline

#### Section: 22.4 Community and Ecosystem

**Prompt 4**
Community and Ecosystem Map

---

## Summary

**Total Visual Assets**: 139 prompts across 14 chapters

### Batch Image Generation

For batch image generation, use the extraction script:
```bash
cd documents/ebook/scripts
bash extract-image-prompts.sh > ../image-prompts/all-prompts.json
```

### Cost Estimates

- **DALL-E 3**: ~$20-25 (HD quality, 139 images)
- **Midjourney**: ~$30/month (unlimited generations)
- **Stable Diffusion**: ~$5-10 (GPU compute)

### Integration Workflow

1. Generate images using AI service with prompts from this appendix
2. Save images to `documents/ebook/images/` subdirectories
3. Run `python3 update-image-references.py` to update chapter markdown
4. Review and commit integrated images

See `documents/ebook/scripts/IMAGE-GENERATION-GUIDE.md` for complete instructions and code examples.