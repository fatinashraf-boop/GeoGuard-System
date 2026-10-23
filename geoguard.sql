-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 23, 2026 at 02:56 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `geoguard`
--

-- --------------------------------------------------------

--
-- Table structure for table `incident_reports`
--

CREATE TABLE `incident_reports` (
  `report_id` int(10) UNSIGNED NOT NULL,
  `user_id` int(10) UNSIGNED NOT NULL,
  `title` varchar(150) NOT NULL,
  `description` text NOT NULL,
  `incident_type` varchar(100) NOT NULL,
  `status` enum('submitted','in_progress','resolved','rejected') NOT NULL DEFAULT 'submitted',
  `latitude` decimal(10,7) DEFAULT NULL,
  `longitude` decimal(10,7) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `image_path` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `incident_reports`
--

INSERT INTO `incident_reports` (`report_id`, `user_id`, `title`, `description`, `incident_type`, `status`, `latitude`, `longitude`, `address`, `image_path`, `created_at`, `updated_at`) VALUES
(1, 1, 'Pothole on main road', 'A pothole requires inspection.', 'Road Damage', 'rejected', 3.0738000, 101.5183000, 'Cyberjaya, Selangor, Malaysia', NULL, '2026-10-22 06:35:28', '2026-10-23 08:37:34'),
(2, 1, 'Broken streetlight', 'Streetlight is not working at night.', 'Public Infrastructure', 'resolved', 3.0645000, 101.6030000, 'Balakong, Selangor, Malaysia', NULL, '2026-10-22 06:35:38', '2026-10-23 08:37:40'),
(3, 1, 'Broken street light', 'The street light in my area is broken and glass is shattered everywhere', 'Safety hazard', 'in_progress', 4.5711461, 101.1418038, '3, Laluan Wira Jaya Timur 24, Taman Rapat Perdana, 31350 Ipoh, Perak, Malaysia', '/storage/emulated/0/Android/data/com.example.geoguard/files/evidence/watermarked_1792680227657.jpg', '2026-10-22 14:43:50', '2026-10-23 08:37:42'),
(4, 1, 'Structural Wall Crack', 'There is a deep crack in the old government building wall.', 'Public Safety Hazard', 'in_progress', 4.5585586, 101.1488004, 'Jalan Tanpa Nama, Perak, Malaysia', '/storage/emulated/0/Android/data/com.example.geoguard/files/evidence/watermarked_1792743026369.jpg', '2026-10-23 08:10:29', '2026-10-23 12:06:23'),
(5, 1, 'Broken Traffic Lights', 'The traffic light is blinking irregularly especially the red and yellow light.', 'Safety Hazard', 'submitted', 4.5585586, 101.1488004, 'Jalan Tanpa Nama, Perak, Malaysia', '/storage/emulated/0/Android/data/com.example.geoguard/files/evidence/watermarked_1792744948561.jpg', '2026-10-23 08:42:36', '2026-10-23 08:42:36'),
(6, 1, 'Tree Falls on House', 'After a windy storm, a tree falls on a property.', 'Property Damage', 'submitted', 4.5585586, 101.1488004, 'Jalan Tanpa Nama, Perak, Malaysia', '/storage/emulated/0/Android/data/com.example.geoguard/files/evidence/watermarked_1792757488314.jpg', '2026-10-23 12:12:38', '2026-10-23 12:12:38');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(10) UNSIGNED NOT NULL,
  `username` varchar(80) NOT NULL,
  `email` varchar(190) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('inspector','admin') NOT NULL DEFAULT 'inspector',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `email`, `password_hash`, `role`, `created_at`) VALUES
(1, 'testinspector', 'inspector@geoguard.test', '$2b$12$5zinchsD2PQ3haEYoASHPuIInJpsypa6EhShlLa0gv/wfy5S1iY7u', 'inspector', '2026-10-22 06:35:05'),
(2, 'testadmin', 'admin@geoguard.test', '$2b$12$fxjW2l9SyIQ/W96e0DP7h.ylVyYhBR7KlQIYGmmr4kA5uUstC1OE6', 'admin', '2026-10-22 15:01:19');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `incident_reports`
--
ALTER TABLE `incident_reports`
  ADD PRIMARY KEY (`report_id`),
  ADD KEY `idx_reports_user` (`user_id`),
  ADD KEY `idx_reports_status` (`status`),
  ADD KEY `idx_reports_created` (`created_at`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `incident_reports`
--
ALTER TABLE `incident_reports`
  MODIFY `report_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `incident_reports`
--
ALTER TABLE `incident_reports`
  ADD CONSTRAINT `fk_reports_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
