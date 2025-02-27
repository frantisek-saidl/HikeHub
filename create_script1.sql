-- MariaDB Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema HikeHub
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `HikeHub` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `HikeHub`;

-- -----------------------------------------------------
-- Table: users
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
  `idusers` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `profile_picture` LONGBLOB NULL,  -- Changed to store binary image data
  `bio` TEXT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`idusers`),
  UNIQUE INDEX `username_UNIQUE` (`username`)
) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table: hikes
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hikes` (
  `idhikes` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `users_idusers` INT UNSIGNED NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT NULL,
  `length_km` DECIMAL(7,2) NULL,  -- Supports up to 99,999.99 km
  `elevation_gain_m` MEDIUMINT UNSIGNED NULL, -- UNSIGNED (0 to 16.7 million meters)
  `highest_point_m` MEDIUMINT UNSIGNED NULL,  -- UNSIGNED for higher altitudes
  `start_location` POINT NOT NULL,  -- Changed to NOT NULL
  `end_location` POINT NOT NULL,    -- Changed to NOT NULL
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`idhikes`),
  INDEX `fk_hikes_users_idx` (`users_idusers` ASC),
  SPATIAL INDEX `start_location_spatial_idx` (`start_location`),
  SPATIAL INDEX `end_location_spatial_idx` (`end_location`),
  CONSTRAINT `fk_hikes_users`
    FOREIGN KEY (`users_idusers`)
    REFERENCES `users` (`idusers`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- Restore settings
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- Optimize the tables for performance
OPTIMIZE TABLE `users`, `hikes`;
