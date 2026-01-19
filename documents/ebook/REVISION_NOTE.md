# E-book Revision Notes

## Latest Update: 2026-01-19 (PR #282)

### Changes from Commit c0b37ae
This update addresses 15 review comments focusing on technical accuracy and realistic documentation:

**Technical Accuracy Corrections:**
- **Connection lifecycle (Chapter 7)**: Clarified that local pooling issue is connection closure (lifecycle signal), not connection multiplication. Updated language from "double-pooling" to accurately describe how local pools break OJP's lifecycle management.
- **Realistic scenarios (Chapter 8)**: Updated slow query example from single query to concurrent load scenario (25 reporting queries consuming 30-connection pool).
- **Monitoring capabilities (Chapter 13)**: Simplified claims to match actual implementation - changed to log-based monitoring instead of referencing non-existent Prometheus/OpenTelemetry segregation metrics.

**Content Adjustments:**
- **Product neutrality**: Removed specific HikariCP brand mentions where appropriate, using generic "connection pool" / "maxPoolSize" terminology (3 instances updated).
- **Feature claims**: Removed documentation for unsupported time-based configuration.
- **Tone calibration**: Changed "production experience" to "our experience" (acknowledging beta product status).
- **Marketing language**: Replaced "game-changer" with measured descriptions of applicability.

**Additional Fixes:**
- Removed orphaned mermaid diagram nodes
- Eliminated inconsistent fast query timing (5s vs milliseconds)

**Files Changed:**
- `part2-chapter7-framework-integration.md`
- `part3-chapter8-slow-query-segregation.md`

---

# E-book Conversational Revision In Progress

## Status
Currently revising all completed chapters (Phases 1, 1a, 2) to be more conversational and narrative-driven, reducing heavy reliance on bullet lists and tables.

## Scope
- **6 chapters** to revise (~149KB total)
- **262 bullet lists** to convert to narrative
- **71 tables** to integrate more naturally
- Maintain all technical accuracy, code examples, image prompts, and Mermaid diagrams

## Revision Principles
1. **Narrative Flow**: Transform lists into flowing paragraphs that tell a story
2. **Conversational Tone**: Write as if explaining to a colleague over coffee
3. **Natural Integration**: Keep essential reference tables but weave them into the narrative
4. **Technical Accuracy**: Maintain all technical details and examples
5. **Visual Elements**: Preserve all image prompts and Mermaid diagrams

## Progress
- [ ] Chapter 1: Introduction to OJP
- [ ] Chapter 2: Architecture Deep Dive
- [ ] Chapter 3: Quick Start Guide
- [ ] Chapter 3a: Kubernetes Deployment with Helm
- [ ] Chapter 4: Database Driver Configuration
- [ ] Chapter 5: OJP JDBC Driver Configuration

## Future Phases
All future phases (3-10) will follow this conversational, narrative-driven style from the start.

---
*Note: This is a comprehensive revision that requires careful rewriting while maintaining technical precision. The revised chapters will be committed as they are completed.*
