# Analysis: Moving Open Source Drivers to ojp-libs Directory

**Date**: 2025-12-30  
**Status**: Implementation Plan Ready  
**Related**: [DRIVERS_AND_LIBS.md](../configuration/DRIVERS_AND_LIBS.md), [ADR-XXX: External Driver Loading](../ADRs/)

## Executive Summary

This analysis explores moving all open source JDBC drivers (H2, PostgreSQL, MySQL, MariaDB) from the `pom.xml` to the `ojp-libs` directory, leveraging the existing drop-in driver loading mechanism currently used for proprietary drivers (Oracle, SQL Server, DB2).

**Decision**: Implement this change immediately in a single release. The infrastructure already exists, and the benefits justify proceeding without a phased rollout.

**Implementation**: 5 phases designed for isolated testing with existing integration tests.

## Current State

### Driver Management Today

**Open Source Drivers (in pom.xml)**:
- H2 (2.3.232)
- PostgreSQL (42.7.8)
- MySQL (9.5.0)
- MariaDB (3.5.2)

**Proprietary Drivers (ojp-libs)**:
- Oracle JDBC
- SQL Server JDBC
- IBM DB2 JDBC

### Current Loading Mechanism

1. **Open Source Drivers**: Compiled into the shaded JAR via Maven dependencies in `ojp-server/pom.xml`
2. **Proprietary Drivers**: Loaded at runtime from `ojp-libs` directory using `DriverLoader.loadDriversFromPath()`
3. **Driver Registration**: All drivers registered via `DriverUtils.registerDrivers()` which:
   - Attempts to load each driver class with `Class.forName()`
   - Logs errors for open source drivers if missing
   - Logs helpful instructions for proprietary drivers if missing

### Key Infrastructure

**Code Components**:
- `DriverLoader.java` - Loads JARs from ojp-libs directory using URLClassLoader
- `DriverUtils.java` - Registers JDBC drivers with DriverManager
- `ServerConfiguration.java` - Manages `ojp.libs.path` configuration (default: `./ojp-libs`)
- `GrpcServer.java` - Orchestrates driver loading on server startup

**Build Components**:
- Maven Shade Plugin - Creates shaded JAR with all dependencies
- Jib Maven Plugin - Builds Docker images
- CI/CD workflows - Test against multiple databases

## Proposed Change

### Goal

Remove all JDBC driver dependencies from `pom.xml` and place them in the `ojp-libs` directory, making the driver loading mechanism uniform for both open source and proprietary drivers.

### Benefits

1. **Customer Flexibility**:
   - Easy driver version upgrades without rebuilding OJP
   - Mix and match driver versions (e.g., use older PostgreSQL driver for compatibility)
   - Remove unwanted drivers to reduce attack surface
   - Add custom/modified drivers for specific needs

2. **Simplified Licensing**:
   - Clearer separation between OJP code and driver licenses
   - Customers explicitly choose which drivers to use
   - Easier compliance audits

3. **Reduced JAR Size**:
   - Base OJP JAR becomes smaller (currently ~60-80MB with drivers)
   - Drivers only downloaded/included when needed
   - Better for environments with limited storage

4. **Consistent Architecture**:
   - Single mechanism for all driver loading
   - Same documentation applies to all drivers
   - Reduces code complexity (no separate handling)

5. **Testing Flexibility**:
   - Test against multiple driver versions without rebuilding
   - Easier reproduction of customer environments
   - Faster CI iteration (download drivers as needed)

### Implementation Considerations

1. **Setup Simplification**:
   - Provide driver bundle for easy download
   - Create helper scripts for driver management
   - Docker images will include drivers by default

2. **CI/CD Updates**:
   - Must download drivers in each CI job (pattern already used for Oracle/DB2/SQL Server)
   - Cache drivers for faster builds
   - Document driver versions in CI

3. **Distribution Strategy**:
   - GitHub Releases: Include driver bundle alongside OJP JAR
   - Docker Images: Include drivers in base image for "batteries included" experience
   - Maven Central: OJP JAR without drivers (cleaner artifact)

## Implementation Plan (5 Phases)

This implementation can be completed immediately with 5 distinct, testable phases. Each phase can be validated using existing integration tests without requiring new test infrastructure.

### Phase 1: Update DriverUtils for Consistent Error Handling

**Goal**: Make driver registration messages consistent for all drivers (open source and proprietary).

**Changes**:
- `ojp-server/src/main/java/org/openjproxy/grpc/server/utils/DriverUtils.java`
  - Change all open source driver errors from `log.error()` to `log.info()` with helpful instructions
  - Make message format consistent with proprietary driver messages
  - All drivers should have same pattern: "Driver not found" → helpful instructions

**Testing**: Run existing H2 tests
```bash
mvn test -pl ojp-jdbc-driver -DenableH2Tests=true
```

**Expected Result**: Tests pass, logs show helpful messages instead of errors for missing drivers.

---

### Phase 2: Remove Driver Dependencies from pom.xml

**Goal**: Remove H2, PostgreSQL, MySQL, and MariaDB dependencies from ojp-server/pom.xml.

**Changes**:
- `ojp-server/pom.xml`
  - Remove `com.h2database:h2:2.3.232`
  - Remove `org.postgresql:postgresql:42.7.8`
  - Remove `com.mysql:mysql-connector-j:9.5.0`
  - Remove `org.mariadb.jdbc:mariadb-java-client:3.5.2`

**Testing**: Build should succeed without drivers
```bash
mvn clean install -pl ojp-server -DskipTests
```

**Expected Result**: OJP server JAR builds successfully, size reduced from ~70MB to ~30MB.

---

### Phase 3: Create Driver Download Script

**Goal**: Provide helper script to download open source drivers to ojp-libs directory.

**Changes**:
- Create `ojp-server/download-drivers.sh`
  - Script downloads H2, PostgreSQL, MySQL, MariaDB from Maven Central
  - Places drivers in specified directory (default: `./ojp-libs`)
  - Makes script executable

**Testing**: Run script and verify drivers downloaded
```bash
cd ojp-server
./download-drivers.sh
ls -lh ojp-libs/*.jar
```

**Expected Result**: Four driver JARs downloaded to ojp-libs directory.

---

### Phase 4: Update CI/CD Workflows

**Goal**: Update all GitHub Actions workflows to download drivers before running tests.

**Changes**:
- `.github/workflows/main.yml`
  - Add "Download Open Source Drivers" step before running tests in ALL jobs
  - Use same pattern as proprietary drivers (Oracle, DB2, SQL Server)
  - Download H2, PostgreSQL, MySQL, MariaDB to `ojp-server/ojp-libs/`
  - Update server startup to use `-Dojp.libs.path=ojp-server/ojp-libs`

**Testing**: Run existing integration tests for each database
```bash
# H2 tests
mvn test -pl ojp-jdbc-driver -DenableH2Tests=true

# PostgreSQL tests (requires PostgreSQL container)
mvn test -pl ojp-jdbc-driver -DenablePostgresTests=true

# MySQL tests (requires MySQL container)
mvn test -pl ojp-jdbc-driver -DenableMySQLTests=true

# MariaDB tests (requires MariaDB container)
mvn test -pl ojp-jdbc-driver -DenableMariaDBTests=true
```

**Expected Result**: All existing integration tests pass with drivers loaded from ojp-libs.

---

### Phase 5: Update Documentation and Docker Configuration

**Goal**: Update all documentation and Docker images to reflect external driver loading.

**Changes**:

**Documentation**:
- `README.md` - Update Quick Start to include driver download step
- `documents/configuration/DRIVERS_AND_LIBS.md` - Update to include open source drivers
- `documents/runnable-jar/README.md` - Add driver download instructions
- `Dockerfile.proprietary` - Rename to `Dockerfile.with-drivers` (more accurate name)

**Docker/Build**:
- `ojp-server/pom.xml` (Jib configuration)
  - Configure Jib to copy ojp-libs directory into Docker image at `/opt/ojp/ojp-libs`
  - Ensures "batteries included" Docker experience
  
- Create `ojp-server/docker-build.sh` helper script
  - Downloads drivers
  - Builds Docker image with drivers

**Testing**: Build and run Docker image
```bash
# Download drivers
cd ojp-server
./download-drivers.sh

# Build Docker image with Jib
mvn compile jib:dockerBuild -pl ojp-server

# Run Docker container
docker run -d -p 1059:1059 --name ojp-test rrobetti/ojp:0.3.2-snapshot

# Test H2 connection through Docker
mvn test -pl ojp-jdbc-driver -DenableH2Tests=true
```

**Expected Result**: Docker image includes drivers, all tests pass through containerized OJP server.

---

## Phase Dependencies

- **Phase 1** → Independent (can run first)
- **Phase 2** → Depends on Phase 1 (error messages should be helpful before removing drivers)
- **Phase 3** → Independent (helper script, doesn't affect code)
- **Phase 4** → Depends on Phase 2 and 3 (needs drivers removed and download script available)
- **Phase 5** → Depends on all previous phases (documentation reflects final state)

## Validation Strategy

Each phase is validated using **existing integration tests**:

| Phase | Test Command | What It Validates |
|-------|--------------|-------------------|
| 1 | `mvn test -pl ojp-jdbc-driver -DenableH2Tests=true` | Driver error messages are helpful |
| 2 | `mvn clean install -pl ojp-server -DskipTests` | Build succeeds without drivers |
| 3 | `./download-drivers.sh && ls ojp-libs/` | Download script works |
| 4 | `mvn test -pl ojp-jdbc-driver -Denable*Tests=true` | All database tests pass with external drivers |
| 5 | Docker build + integration tests | Docker image works with external drivers |

**No new tests required** - all validation uses existing test infrastructure.

## Rollback Strategy

If issues are discovered:
1. **Phase 1-3**: Simply revert commits (no breaking changes yet)
2. **Phase 4**: Revert workflow changes, add drivers back to pom.xml
3. **Phase 5**: Revert documentation, use old Docker config

Since all phases are tested in isolation, issues can be identified and fixed early.

## Impact Analysis

### Build Process

**Maven Build**:
```bash
# Current (drivers included)
mvn clean install -pl ojp-server
# Result: ojp-server-0.3.2-snapshot-shaded.jar (~70MB)

# Proposed (no drivers)
mvn clean install -pl ojp-server -Pno-drivers
# Result: ojp-server-0.3.2-snapshot-shaded.jar (~30MB)
```

**Docker Build**:
```dockerfile
# Current: Dockerfile uses Jib (drivers in JAR)
FROM eclipse-temurin:22-jre
COPY target/ojp-server-shaded.jar /app/ojp-server.jar

# Proposed Option 1: Drivers in image
FROM eclipse-temurin:22-jre
COPY target/ojp-server-shaded.jar /app/ojp-server.jar
COPY ojp-libs/*.jar /opt/ojp/ojp-libs/

# Proposed Option 2: Drivers via volume
FROM eclipse-temurin:22-jre
COPY target/ojp-server-shaded.jar /app/ojp-server.jar
VOLUME /opt/ojp/ojp-libs
# Customers mount ojp-libs directory
```

### CI/CD Changes

**Current Workflow**:
```yaml
- name: Build (ojp-server)
  run: mvn clean install -pl ojp-server

- name: Run (ojp-server)
  run: java -jar ojp-server/target/ojp-server-shaded.jar
```

**Proposed Workflow**:
```yaml
- name: Build (ojp-server)
  run: mvn clean install -pl ojp-server -Pno-drivers

- name: Download Open Source Drivers
  run: |
    mkdir -p ojp-server/ojp-libs
    # Download H2
    mvn dependency:copy \
      -Dartifact=com.h2database:h2:2.3.232 \
      -DoutputDirectory=ojp-server/ojp-libs
    # Download PostgreSQL
    mvn dependency:copy \
      -Dartifact=org.postgresql:postgresql:42.7.8 \
      -DoutputDirectory=ojp-server/ojp-libs
    # ... other drivers ...

- name: Run (ojp-server)
  run: java -Dojp.libs.path=ojp-server/ojp-libs -jar ojp-server/target/ojp-server-shaded.jar
```

**Note**: This is already similar to how proprietary drivers are handled in CI (Oracle, DB2, SQL Server tests).

### Testing Impact

**Positive**:
- Can test against multiple driver versions in same CI run
- Easier to reproduce customer environments
- Faster iteration (no rebuild needed)

**Challenges**:
- All CI jobs must download drivers
- Need reliable driver source (Maven Central)
- Network dependency for every test run

**Mitigation**:
- Cache drivers in CI
- Create driver bundle artifact
- Document offline testing approach

### Docker Images

**Current State**:
```bash
docker run rrobetti/ojp:0.3.2-snapshot
# Drivers: H2, PostgreSQL, MySQL, MariaDB built-in
# Size: ~200MB
```

**Proposed Options**:

**Option 1: Batteries Included (Recommended)**
```bash
docker run rrobetti/ojp:0.3.2-snapshot
# Drivers: All open source drivers in /opt/ojp/ojp-libs
# Size: ~200MB (same)
# Best for: Quick start, evaluation, production
```

**Option 2: Minimal Base**
```bash
docker run -v ./ojp-libs:/opt/ojp/ojp-libs rrobetti/ojp:0.3.2-snapshot-minimal
# Drivers: None (customer provides via volume mount)
# Size: ~150MB
# Best for: Security-conscious, custom setups
```

**Option 3: Driver-Specific Tags**
```bash
docker run rrobetti/ojp:0.3.2-snapshot-h2
docker run rrobetti/ojp:0.3.2-snapshot-postgresql
docker run rrobetti/ojp:0.3.2-snapshot-all
# Drivers: Specified by tag
# Best for: Minimal attack surface, specific use cases
```

### File Structure

**Before** (current):
```
ojp-server/
├── pom.xml (includes H2, PostgreSQL, MySQL, MariaDB dependencies)
├── src/
└── target/
    └── ojp-server-0.3.2-snapshot-shaded.jar (includes all drivers)
```

**After** (proposed):
```
ojp-server/
├── pom.xml (no driver dependencies)
├── src/
├── target/
│   └── ojp-server-0.3.2-snapshot-shaded.jar (no drivers)
└── ojp-libs/  (created by user or CI)
    ├── h2-2.3.232.jar
    ├── postgresql-42.7.8.jar
    ├── mysql-connector-j-9.5.0.jar
    └── mariadb-java-client-3.5.2.jar
```

## Customer Impact Guide

### Quick Start for New Users

**Step 1: Download OJP**:
```bash
# Download OJP server JAR
wget https://github.com/Open-J-Proxy/ojp/releases/download/v0.3.2/ojp-server-0.3.2-snapshot-shaded.jar
```

**Step 2: Download Drivers**:
```bash
# Option A: Download driver bundle
wget https://github.com/Open-J-Proxy/ojp/releases/download/v0.3.2/ojp-drivers-bundle.zip
unzip ojp-drivers-bundle.zip

# Option B: Use download script (if building from source)
cd ojp-server
./download-drivers.sh
```

**Step 3: Run**:
```bash
java -jar ojp-server-0.3.2-snapshot-shaded.jar
# Drivers auto-loaded from ./ojp-libs/
```

### Docker Usage

**Pre-built image with drivers included**:
```bash
# Batteries included - drivers already in image
docker run -d -p 1059:1059 rrobetti/ojp:0.3.2-snapshot
```

**Custom driver setup**:
```bash
# Mount your own ojp-libs directory
docker run -d -p 1059:1059 -v $(pwd)/ojp-libs:/opt/ojp/ojp-libs rrobetti/ojp:0.3.2-snapshot
```

### Customer Use Cases

**Use Case 1: Remove Unwanted Drivers**
```bash
# Customer only needs PostgreSQL
mkdir ojp-libs
cp ~/Downloads/postgresql-42.7.8.jar ojp-libs/
java -jar ojp-server.jar
# Result: Only PostgreSQL driver loaded, others not available
```

**Use Case 2: Use Older Driver Version**
```bash
# Customer needs PostgreSQL 42.5.0 for compatibility
mkdir ojp-libs
cp ~/Downloads/postgresql-42.5.0.jar ojp-libs/
java -jar ojp-server.jar
# Result: Uses customer's specific driver version
```

**Use Case 3: Custom Driver Build**
```bash
# Customer has modified PostgreSQL driver
mkdir ojp-libs
cp ~/custom-drivers/postgresql-42.7.8-custom.jar ojp-libs/
java -jar ojp-server.jar
# Result: Uses customer's custom driver
```

**Use Case 4: Security Scanning**
```bash
# Customer must scan all JARs before deployment
# Download drivers separately
wget https://github.com/Open-J-Proxy/ojp/releases/download/v0.3.2/ojp-drivers-bundle.zip
unzip ojp-drivers-bundle.zip -d ./ojp-libs

# Scan drivers
scan-tool ./ojp-libs/*.jar

# Deploy if clean
docker run -d -p 1059:1059 -v $(pwd)/ojp-libs:/opt/ojp/ojp-libs rrobetti/ojp:0.3.2-snapshot
```

## Documentation Changes Required

### Documents to Update (Phase 5)
1. **README.md** - Add driver download step to Quick Start
2. **DRIVERS_AND_LIBS.md** - Extend to include open source drivers  
3. **runnable-jar/README.md** - Add driver download instructions
4. **Dockerfile.proprietary** - Rename to `Dockerfile.with-drivers`
   - Tested driver versions
   - Compatibility notes
   - Known issues

### Updated Documents
1. **README.md**
   - Update Quick Start section
   - Add driver download step
   - Update Docker examples

2. **DRIVERS_AND_LIBS.md**
   - Add section on open source drivers
   - Update from "proprietary only" to "all drivers"
   - Add driver bundle information

3. **runnable-jar/README.md**
   - Add driver download instructions
   - Update build instructions
   - Update troubleshooting

4. **CI/CD Examples**
   - Update GitHub Actions examples
   - Update GitLab CI examples
   - Add driver download steps

## Code Changes Required

### 1. Maven POM Changes

**ojp-server/pom.xml**:
```xml
<!-- Remove dependencies -->
<!-- 
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.3.232</version>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.8</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.5.0</version>
</dependency>
<dependency>
    <groupId>org.mariadb.jdbc</groupId>
    <artifactId>mariadb-java-client</artifactId>
    <version>3.5.2</version>
</dependency>
-->
```

### 2. DriverUtils Changes

**Update error messages** to be consistent for all drivers:

```java
// Before: Different handling for open source vs proprietary
try {
    Class.forName(H2_DRIVER_CLASS);
} catch (ClassNotFoundException e) {
    log.error("Failed to register H2 JDBC driver.", e); // Error for open source
}

try {
    Class.forName(ORACLE_DRIVER_CLASS);
    log.info("Oracle JDBC driver loaded successfully");
} catch (ClassNotFoundException e) {
    log.info("Oracle JDBC driver not found. To use Oracle databases:"); // Info for proprietary
    log.info("  1. Download ojdbc*.jar...");
}

// After: Consistent handling for all drivers
try {
    Class.forName(H2_DRIVER_CLASS);
    log.info("H2 JDBC driver loaded successfully");
} catch (ClassNotFoundException e) {
    log.info("H2 JDBC driver not found. To use H2 databases:");
    log.info("  1. Download h2-*.jar from https://mvnrepository.com/artifact/com.h2database/h2");
    log.info("  2. Place it in: {}", driverPathMessage);
    log.info("  3. Restart OJP Server");
}

// Same pattern for all drivers: H2, PostgreSQL, MySQL, MariaDB, Oracle, SQL Server, DB2
```

### 3. No Changes Needed

These components already support external driver loading:
- `DriverLoader.java` - Already loads from ojp-libs
- `ServerConfiguration.java` - Already has ojp.libs.path config
- `GrpcServer.java` - Already calls DriverLoader
- `.gitignore` - Already ignores ojp-libs directory

### 4. Build Script Changes

**Create download-drivers.sh**:
```bash
#!/bin/bash
# Download open source JDBC drivers to ojp-libs directory

DRIVERS_DIR="${1:-./ojp-libs}"
mkdir -p "$DRIVERS_DIR"

echo "Downloading open source JDBC drivers to $DRIVERS_DIR..."

# H2
mvn dependency:copy \
  -Dartifact=com.h2database:h2:2.3.232 \
  -DoutputDirectory="$DRIVERS_DIR" \
  -Dmdep.stripVersion=false

# PostgreSQL
mvn dependency:copy \
  -Dartifact=org.postgresql:postgresql:42.7.8 \
  -DoutputDirectory="$DRIVERS_DIR" \
  -Dmdep.stripVersion=false

# MySQL
mvn dependency:copy \
  -Dartifact=com.mysql:mysql-connector-j:9.5.0 \
  -DoutputDirectory="$DRIVERS_DIR" \
  -Dmdep.stripVersion=false

# MariaDB
mvn dependency:copy \
  -Dartifact=org.mariadb.jdbc:mariadb-java-client:3.5.2 \
  -DoutputDirectory="$DRIVERS_DIR" \
  -Dmdep.stripVersion=false

echo "Downloaded drivers:"
ls -lh "$DRIVERS_DIR"/*.jar
```

### 5. CI/CD Workflow Changes

**Update .github/workflows/main.yml**:
```yaml
# Add this step before "Test and Run (ojp-server)" in all jobs

- name: Download Open Source Drivers to ojp-libs
  run: |
    mkdir -p ojp-server/ojp-libs
    mvn dependency:copy -Dartifact=com.h2database:h2:2.3.232 \
      -DoutputDirectory=ojp-server/ojp-libs -Dmdep.stripVersion=false
    mvn dependency:copy -Dartifact=org.postgresql:postgresql:42.7.8 \
      -DoutputDirectory=ojp-server/ojp-libs -Dmdep.stripVersion=false
    mvn dependency:copy -Dartifact=com.mysql:mysql-connector-j:9.5.0 \
      -DoutputDirectory=ojp-server/ojp-libs -Dmdep.stripVersion=false
    mvn dependency:copy -Dartifact=org.mariadb.jdbc:mariadb-java-client:3.5.2 \
      -DoutputDirectory=ojp-server/ojp-libs -Dmdep.stripVersion=false
    echo "Downloaded drivers:"
    ls -lh ojp-server/ojp-libs/

# This already exists for proprietary drivers (Oracle, DB2, SQL Server)
# Now we apply the same pattern to open source drivers
```

**Note**: This adds a few seconds to each CI job but provides consistency.

### 6. Docker Changes

**Update Dockerfile and Jib configuration**:

**Option 1: Include drivers in base image (recommended)**:
```dockerfile
# Dockerfile.with-drivers
FROM rrobetti/ojp:0.4.0-base

# Copy open source drivers
COPY ojp-libs/*.jar /opt/ojp/ojp-libs/

# Server will auto-load drivers from /opt/ojp/ojp-libs
```

**Option 2: Minimal base image**:
```dockerfile
# Dockerfile (base image, no drivers)
FROM eclipse-temurin:22-jre
COPY target/ojp-server-shaded.jar /app/ojp-server.jar
VOLUME /opt/ojp/ojp-libs
ENV OJP_LIBS_PATH=/opt/ojp/ojp-libs
```

**Jib configuration** (ojp-server/pom.xml):
```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <configuration>
        <from>
            <image>eclipse-temurin:22-jre</image>
        </from>
        <to>
            <image>rrobetti/ojp:0.4.0</image>
        </to>
        <container>
            <mainClass>org.openjproxy.grpc.server.GrpcServer</mainClass>
            <!-- Add drivers to image -->
            <extraDirectories>
                <paths>
                    <path>
                        <from>ojp-libs</from>
                        <into>/opt/ojp/ojp-libs</into>
                    </path>
                </paths>
            </extraDirectories>
        </container>
    </configuration>
</plugin>
```

## Testing Strategy

### Unit Tests
No changes needed - unit tests don't depend on driver loading mechanism.

### Integration Tests

Tests validate each phase independently using existing test infrastructure (see Implementation Plan above for phase-specific tests).

**Pattern for all tests**:
```bash
# Download drivers first
./download-drivers.sh ojp-server/ojp-libs

# Run tests with driver path
mvn test -pl ojp-jdbc-driver -Dojp.libs.path=ojp-server/ojp-libs -Denable*Tests=true
```

CI workflows will handle this automatically (Phase 4 implementation).
mvn test -pl ojp-jdbc-driver -DenableH2Tests=true
# Drivers in classpath, tests run immediately
```

**After** (proposed):
```bash
# Download drivers first
./download-drivers.sh ojp-server/ojp-libs

# Run tests with driver path
mvn test -pl ojp-jdbc-driver -DenableH2Tests=true -Dojp.libs.path=ojp-server/ojp-libs
```

**CI/CD**: Already downloading proprietary drivers, extend to open source drivers.

### Compatibility Tests

Create test matrix:
- OJP 0.4.0 + H2 2.3.232 ✓
- OJP 0.4.0 + H2 2.2.x ✓
- OJP 0.4.0 + PostgreSQL 42.7.8 ✓
- OJP 0.4.0 + PostgreSQL 42.5.0 ✓
- etc.

## Security Considerations

### Benefits
1. **Reduced Attack Surface**: Customers can remove unused drivers
2. **Faster Patching**: Update drivers without rebuilding OJP
3. **Audit Trail**: Explicit driver versions in deployment

### Risks
1. **Supply Chain**: Customers must download drivers from trusted sources
2. **Version Drift**: Different deployments may use different driver versions
3. **Compatibility**: Untested driver versions may cause issues

### Mitigations
1. **Document trusted sources**: Maven Central, official vendor sites
2. **Provide checksums**: Verify driver integrity
3. **Test matrix**: Document tested driver versions
4. **Driver bundle**: Provide pre-tested driver packages

## Performance Considerations

### Build Time
- **Current**: ~30s (includes driver compilation)
- **Proposed**: ~25s (no drivers) + 10s (download drivers) = ~35s
- **Impact**: Minimal, one-time cost per environment

### Runtime
- **No difference**: Driver loading mechanism already exists and is fast
- **ojp-libs loading**: ~100ms for 4 drivers
- **Class.forName**: Same performance either way

### JAR Size
- **Current**: ~70MB (includes drivers)
- **Proposed**: ~30MB (no drivers) + ~10MB (drivers separate) = ~40MB total
- **Benefit**: Smaller base artifact, flexible driver selection

## Open Questions

1. **Should we maintain two distribution models long-term?**
   - "Batteries included" for easy start
   - "Minimal" for security-conscious deployments
   - Recommendation: Yes, both have value

2. **Should we auto-download missing drivers?**
   - Pro: Better user experience
   - Con: Security/trust concerns
   - Recommendation: Make it opt-in, document manual process as default

3. **What's the support policy for driver versions?**
   - Test against latest stable versions?
   - Support N-2 versions?
   - Recommendation: Document tested versions, allow any version

4. **How do we handle driver dependencies?**
   - Some drivers have transitive dependencies
   - Do we bundle them?
   - Recommendation: Use fat JARs when possible, document dependencies

5. **Should we version the driver bundle separately?**
   - Bundle version independent of OJP version?
   - Recommendation: Yes, allows driver updates without OJP releases

## Implementation Summary

This change is **immediately implementable** with the 5-phase plan described above. The infrastructure already exists and has been proven with proprietary drivers.

**Key Points**:
1. **Testable in isolation** - Each phase can be validated independently with existing integration tests
2. **No breaking changes concern** - Docker images will include drivers by default for "batteries included" experience
3. **Clear rollback path** - Each phase can be reverted if issues are discovered
4. **Immediate benefits** - Customers gain flexibility, smaller base JAR, consistent driver loading

**Success Criteria**:
- All existing integration tests pass after each phase
- Docker image includes drivers and maintains current user experience
- Documentation clearly explains driver management
- Download script works reliably
- CI workflows complete successfully with external drivers

---

## Appendix A: Code Changes Summary

### Files to Modify
1. `ojp-server/src/main/java/org/openjproxy/grpc/server/utils/DriverUtils.java` - Consistent error messages
2. `ojp-server/pom.xml` - Remove 4 driver dependencies, update Jib config
3. `.github/workflows/main.yml` - Add driver download steps to all jobs
4. `README.md` - Update Quick Start
5. `documents/configuration/DRIVERS_AND_LIBS.md` - Extend to open source drivers
6. `documents/runnable-jar/README.md` - Add driver instructions
7. `Dockerfile.proprietary` - Rename to `Dockerfile.with-drivers`

### Files to Create
1. `ojp-server/download-drivers.sh` - Helper script to download drivers
2. `ojp-server/docker-build.sh` - Docker build helper

### No Changes Needed
- `DriverLoader.java` - Already supports external drivers
- `ServerConfiguration.java` - Already has ojp.libs.path config
- `GrpcServer.java` - Already calls DriverLoader
- `.gitignore` - Already excludes ojp-libs

## Appendix B: Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Build failures | Low | Medium | Phase 2 tested in isolation with existing build |
| Test failures | Low | Medium | Each phase validated with existing tests |
| CI issues | Low | Medium | Pattern already used for proprietary drivers |
| Docker image issues | Low | Low | Jib extraDirectories well-tested feature |
| User confusion | Medium | Low | Clear documentation, "batteries included" Docker |

All risks are manageable with the 5-phase implementation approach.

## Appendix C: Comparison with Other Projects

Many popular open source projects use similar approaches:

**Elasticsearch**: Plugins downloaded separately  
**Kafka**: Connect plugins separate from core  
**Jenkins**: Plugins managed externally  
**Tomcat**: JDBC drivers in separate lib directory  

This pattern is well-established and understood in the Java ecosystem.

## Appendix D: Sample Commands Reference

### Download Drivers
```bash
# All open source drivers
./download-drivers.sh

# Specific driver
mvn dependency:copy -Dartifact=com.h2database:h2:2.3.232 -DoutputDirectory=./ojp-libs

# From GitHub release
wget https://github.com/Open-J-Proxy/ojp/releases/download/v0.3.2/ojp-drivers-bundle.zip
unzip ojp-drivers-bundle.zip
```

### Build Without Drivers
```bash
# Maven (after Phase 2)
mvn clean install -pl ojp-server -DskipTests

# Result: Smaller JAR (~30MB vs ~70MB)
```

### Run with External Drivers
```bash
# JAR
java -Dojp.libs.path=./ojp-libs -jar ojp-server.jar

# Docker (batteries included)
docker run -d -p 1059:1059 rrobetti/ojp:0.3.2-snapshot

# Docker (custom drivers)
docker run -d -p 1059:1059 -v $(pwd)/ojp-libs:/opt/ojp/ojp-libs rrobetti/ojp:0.3.2-snapshot
```

### Verify Drivers
```bash
# List JARs in ojp-libs
ls -lh ./ojp-libs/*.jar

# Check server logs for loaded drivers
grep "driver loaded successfully" ojp-server.log
```
