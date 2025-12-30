package org.openjproxy.grpc.server.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static org.openjproxy.grpc.server.Constants.H2_DRIVER_CLASS;
import static org.openjproxy.grpc.server.Constants.MARIADB_DRIVER_CLASS;
import static org.openjproxy.grpc.server.Constants.MYSQL_DRIVER_CLASS;
import static org.openjproxy.grpc.server.Constants.ORACLE_DRIVER_CLASS;
import static org.openjproxy.grpc.server.Constants.POSTGRES_DRIVER_CLASS;
import static org.openjproxy.grpc.server.Constants.SQLSERVER_DRIVER_CLASS;
import static org.openjproxy.grpc.server.Constants.DB2_DRIVER_CLASS;

@Slf4j
@UtilityClass
public class DriverUtils {
    
    /**
     * Register all JDBC drivers supported.
     * @param driversPath Optional path to external libraries directory for user guidance in error messages
     */
    public void registerDrivers(String driversPath) {
        String driverPathMessage = (driversPath != null && !driversPath.trim().isEmpty()) 
            ? driversPath 
            : "./ojp-libs";
            
        //Register open source drivers
        try {
            Class.forName(H2_DRIVER_CLASS);
            log.info("H2 JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            log.info("H2 JDBC driver not found. To use H2 databases:");
            log.info("  1. Download h2-*.jar from Maven Central (https://mvnrepository.com/artifact/com.h2database/h2)");
            log.info("  2. Place it in: {}", driverPathMessage);
            log.info("  3. Restart OJP Server");
        }
        try {
            Class.forName(POSTGRES_DRIVER_CLASS);
            log.info("PostgreSQL JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            log.info("PostgreSQL JDBC driver not found. To use PostgreSQL databases:");
            log.info("  1. Download postgresql-*.jar from Maven Central (https://mvnrepository.com/artifact/org.postgresql/postgresql)");
            log.info("  2. Place it in: {}", driverPathMessage);
            log.info("  3. Restart OJP Server");
        }
        try {
            Class.forName(MYSQL_DRIVER_CLASS);
            log.info("MySQL JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            log.info("MySQL JDBC driver not found. To use MySQL databases:");
            log.info("  1. Download mysql-connector-j-*.jar from Maven Central (https://mvnrepository.com/artifact/com.mysql/mysql-connector-j)");
            log.info("  2. Place it in: {}", driverPathMessage);
            log.info("  3. Restart OJP Server");
        }
        try {
            Class.forName(MARIADB_DRIVER_CLASS);
            log.info("MariaDB JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            log.info("MariaDB JDBC driver not found. To use MariaDB databases:");
            log.info("  1. Download mariadb-java-client-*.jar from Maven Central (https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client)");
            log.info("  2. Place it in: {}", driverPathMessage);
            log.info("  3. Restart OJP Server");
        }
        //Register proprietary drivers (if present)
        try {
            Class.forName(ORACLE_DRIVER_CLASS);
            log.info("Oracle JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            log.info("Oracle JDBC driver not found. To use Oracle databases:");
            log.info("  1. Download ojdbc*.jar from Oracle (https://www.oracle.com/database/technologies/jdbc-downloads.html)");
            log.info("  2. Place it in: {}", driverPathMessage);
            log.info("  3. Restart OJP Server");
        }
        try {
            Class.forName(SQLSERVER_DRIVER_CLASS);
            log.info("SQL Server JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            log.info("SQL Server JDBC driver not found. To use SQL Server databases:");
            log.info("  1. Download mssql-jdbc-*.jar from Microsoft (https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server)");
            log.info("  2. Place it in: {}", driverPathMessage);
            log.info("  3. Restart OJP Server");
        }
        try {
            Class.forName(DB2_DRIVER_CLASS);
            log.info("DB2 JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            log.info("DB2 JDBC driver not found. To use DB2 databases:");
            log.info("  1. Download db2jcc*.jar from IBM");
            log.info("  2. Place it in: {}", driverPathMessage);
            log.info("  3. Restart OJP Server");
        }
    }
    
    /**
     * Register all JDBC drivers supported without path information.
     * @deprecated Use {@link #registerDrivers(String)} instead to provide better error messages
     */
    @Deprecated
    public void registerDrivers() {
        // Delegate to main method with default path
        registerDrivers(null);
    }
}
