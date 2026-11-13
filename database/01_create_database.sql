-- Snap & Pack: step 1. Run this once in SQL Server Management Studio (or Azure Data Studio).
-- It creates an empty database named SnapAndPack.

IF DB_ID(N'SnapAndPack') IS NULL
    CREATE DATABASE SnapAndPack;
GO
