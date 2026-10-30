@echo off
echo ========================================================
echo Starting Digital Investigation Platform - Backend API
echo Module: backend/ (Spring Boot 3.3.4, Java 21)
echo Database: H2 in-memory (dev) / PostgreSQL (prod)
echo Historical Data Seeder: ACTIVE (6 preloaded cases)
echo ========================================================
cd backend
mvn spring-boot:run
pause
