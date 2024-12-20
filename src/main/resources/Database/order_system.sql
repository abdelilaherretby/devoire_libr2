CREATE DATABASE IF NOT EXISTS `order_system` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `order_system`;

CREATE TABLE `customer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `customer` (`id`, `name`, `email`, `phone`) VALUES
(101, 'John Doe', 'john.doe@example.com', '123-456-7890'),
(102, 'Jane Smith', 'jane.smith@example.com', '234-567-8901'),
(103, 'Alice Brown', 'alice.brown@example.com', '345-678-9012');

CREATE TABLE `order` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `amount` double NOT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'pending',
  PRIMARY KEY (`id`),
  KEY `customer_id` (`customer_id`),
  CONSTRAINT `order_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


COMMIT;
