# Driver Management Analysis - Executive Summary

**Related**: [Full Analysis](OPEN_SOURCE_DRIVERS_IN_OJP_LIBS.md)

## Question

Can we use the ojp-libs directory (currently used for proprietary drivers) to also load open source drivers, removing them from pom.xml?

## Answer

**Yes, and we're implementing it immediately.** The infrastructure already exists and works well for proprietary drivers.

## Implementation Plan

**5 Phases** - Each testable in isolation with existing integration tests:

1. **Update DriverUtils** - Consistent error messages for all drivers
2. **Remove Drivers from pom.xml** - H2, PostgreSQL, MySQL, MariaDB
3. **Create Download Script** - Helper to download drivers from Maven Central
4. **Update CI/CD** - All workflows download drivers (same pattern as Oracle/DB2/SQL Server)
5. **Update Documentation** - README, guides, Docker configs

See [full analysis](OPEN_SOURCE_DRIVERS_IN_OJP_LIBS.md) for detailed phase specifications.

## Key Benefits

1. **Customer Control**: Customers can update, remove, or replace any driver without rebuilding OJP
2. **Smaller Base Artifact**: OJP JAR reduces from ~70MB to ~30MB
3. **Security**: Customers control exact driver versions, can remove unused drivers
4. **Consistency**: All drivers loaded the same way (no special cases)
5. **Flexibility**: Easy to test multiple driver versions

## Key Challenges

1. **CI/CD Updates**: All workflows need driver download steps (pattern exists for proprietary drivers)
2. **Documentation Updates**: Extensive doc changes across README, guides, etc.
3. **Docker Images**: Must include drivers by default for "batteries included" experience

## Implementation Approach

**Immediate implementation with 5 testable phases** (no phased release plan):

### Phase 1: Update DriverUtils (Consistent Messages)
- Make all driver error messages helpful and consistent
- Test: `mvn test -pl ojp-jdbc-driver -DenableH2Tests=true`

### Phase 2: Remove Drivers from pom.xml
- Remove H2, PostgreSQL, MySQL, MariaDB dependencies
- Test: `mvn clean install -pl ojp-server -DskipTests`
- Result: JAR size 70MB → 30MB

### Phase 3: Create Download Script
- `download-drivers.sh` - Downloads all open source drivers
- Test: Run script, verify 4 JARs downloaded

### Phase 4: Update CI/CD Workflows
- Add driver download to all jobs (same as Oracle/DB2/SQL Server pattern)
- Test: All database integration tests pass

### Phase 5: Update Documentation & Docker
- Update README, guides, Docker config
- Configure Jib to include ojp-libs in Docker image
- Test: Docker build + integration tests

**Each phase validated independently with existing tests. No new test infrastructure needed.**

## Customer Impact

### Quick Start (After Implementation)

```bash
# Download OJP
wget https://github.com/Open-J-Proxy/ojp/releases/download/v0.3.2/ojp-server-shaded.jar

# Download driver bundle
wget https://github.com/Open-J-Proxy/ojp/releases/download/v0.3.2/ojp-drivers-bundle.zip
unzip ojp-drivers-bundle.zip

# Run (auto-discovers drivers in ./ojp-libs/)
java -jar ojp-server-shaded.jar
```

### Docker (No Change to User Experience)

```bash
# Batteries included - drivers pre-installed in image
docker run -d -p 1059:1059 rrobetti/ojp:0.3.2-snapshot

# Or mount custom drivers
docker run -d -p 1059:1059 -v $(pwd)/ojp-libs:/opt/ojp/ojp-libs rrobetti/ojp:0.3.2-snapshot
```

## Customer Use Cases

Customers gain flexibility to:

1. **Remove unwanted drivers** - Security/compliance
```bash
mkdir ojp-libs
cp postgresql-42.7.8.jar ojp-libs/  # Only PostgreSQL
java -jar ojp-server.jar
```

2. **Use specific driver versions** - Compatibility
```bash
mkdir ojp-libs
cp postgresql-42.5.0.jar ojp-libs/  # Older version
java -jar ojp-server.jar
```

3. **Use custom/patched drivers** - Enterprise needs
```bash
mkdir ojp-libs
cp custom-postgresql-driver.jar ojp-libs/
java -jar ojp-server.jar
```

4. **Security scanning** - Audit dependencies
```bash
wget ojp-drivers-bundle.zip
unzip ojp-drivers-bundle.zip
scan-tool ./ojp-libs/*.jar  # Scan before deployment
```

## Implementation Files

### Files to Modify
1. `DriverUtils.java` - Consistent error messages (Phase 1)
2. `ojp-server/pom.xml` - Remove 4 dependencies, update Jib (Phase 2)  
3. `.github/workflows/main.yml` - Add driver downloads (Phase 4)
4. `README.md`, `DRIVERS_AND_LIBS.md`, `runnable-jar/README.md` (Phase 5)
5. `Dockerfile.proprietary` → `Dockerfile.with-drivers` (Phase 5)

### Files to Create
- `ojp-server/download-drivers.sh` (Phase 3)
- `ojp-server/docker-build.sh` (Phase 5)

### No Changes Needed
- `DriverLoader.java` - Already supports external drivers
- `ServerConfiguration.java` - Already has ojp.libs.path
- `GrpcServer.java` - Already calls DriverLoader  
- `.gitignore` - Already excludes ojp-libs

## Validation Strategy

| Phase | Test Command | Validates |
|-------|--------------|-----------|
| 1 | `mvn test -pl ojp-jdbc-driver -DenableH2Tests=true` | Error messages helpful |
| 2 | `mvn clean install -pl ojp-server -DskipTests` | JAR size reduced (70MB→30MB) |
| 3 | `./download-drivers.sh && ls ojp-libs/` | 4 drivers downloaded |
| 4 | `mvn test -Denable*Tests=true` | All DB tests pass |
| 5 | Docker build + tests | Image includes drivers |

**No new tests required** - uses existing integration tests.

# Run
java -jar ojp-server-shaded.jar
# (auto-discovers drivers in ./ojp-libs/)
```

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Build failures | Low | Medium | Phase 2 tested independently |
| Test failures | Low | Medium | Each phase validated with existing tests |
| CI issues | Low | Medium | Pattern already used for Oracle/DB2/SQL Server |
| Docker issues | Low | Low | Jib extraDirectories is well-tested |
| User confusion | Medium | Low | "Batteries included" Docker + clear docs |

All risks manageable with 5-phase approach.

## Summary

**Status**: Ready for immediate implementation

**Approach**: 5 phases, each testable in isolation with existing tests

**User Impact**: 
- Docker users: No change (drivers pre-installed)
- JAR users: One extra step (download driver bundle)

**Benefits**: 
- Customer flexibility (version control, security scanning)
- Smaller base JAR (30MB vs 70MB)
- Consistent driver loading for all databases

See [full analysis](OPEN_SOURCE_DRIVERS_IN_OJP_LIBS.md) for complete implementation specifications.
