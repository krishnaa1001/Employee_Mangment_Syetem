# KrrishIt Employee & Client Management System

This is a Spring Boot web application for managing Employees, Clients, and Projects. It features a robust, role-based system with full CRUD (Create, Read, Update, Delete) operations, search and pagination capabilities, and block/unblock functionality for user accounts.

## Features

- **Employee Management**
  - Create, view, edit, update, and delete employee records.
  - Automatic generation of unique employee IDs (e.g., `krrishitEmp001`).
  - Email notifications with default password and login link.
  - Block/Unblock functionality to restrict employee login.
  - Search and pagination (5 records per page) in the view page.
  
- **Client Management**
  - Create, view, edit, update, and delete client records.
  - Unique client ID generation (e.g., `krrishitCli001`).
  - Email notifications with default password and login link.
  - Block/Unblock functionality to restrict client login.
  - Search and pagination (5 records per page) in the view page.

- **Project Management**
  - Create, view, edit, update, and delete project records.
  - Unique project ID generation (e.g., `krrishitProj001`).
  - Project details include project name, company name, company email, company address, start date, and deadline date.
  - Search and pagination (5 records per page) in the view page.

- **User Authentication**
  - Role-based login with separate dashboards for admin, employees, and clients.
  - OTP verification for secure access.
  - Integration with Spring Security, including a check on a user's `enabled` status.

## Technologies Used

- **Spring Boot** for rapid application development.
- **Spring Data JPA** for database interaction.
- **Spring Security** for authentication and authorization.
- **Thymeleaf** for server-side view rendering.
- **Bootstrap 5** for responsive UI design.
- **JavaMailSender** for sending email notifications.
- **Lombok** to reduce boilerplate code.

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven
- A running instance of a database (e.g., MySQL, PostgreSQL, or H2 for development)
- An SMTP server for sending emails (configure in `application.properties`)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/krishnaa1001/Employee_Mangment_Syetem.git
   cd krrishit-management-system
