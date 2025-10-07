# AMRIT - Common Service 
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)  ![branch parameter](https://github.com/PSMRI/Common-API/actions/workflows/sast-and-package.yml/badge.svg) 


Common API is a microservice whch acts as a gateway for AMRIT. There are many APIs that are exposed by Common-API. It contains APIs of common integrators like c-Zentrix, Everwell, Openkm and some master APIs like location master, alerts,  notification,language and location messages.
### Primary Features
* Beneficiary Registration
* User authorisation and authentication
* Call handling (107,1097 & mcts)
* Covid Vaccine status
* Email Service
* SMS Service
* OTP Service
* Feedback service
* Beneficiary Medical History maintenance 
* Finding Institutions
* KM file management
* Fetching data from POCT devices
* Report APIs
* Language API
* Notification service
* CRM Reports
* Appointment Scheduling

## Environment and Setup
For setting up the development environment, please refer to the [Developer Guide](https://piramal-swasthya.gitbook.io/amrit/developer-guide/development-environment-setup) .
Prerequisites 
* JDK 17
* Maven 
* Springboot V2
* MySQL

## API Guide
Detailed information on API endpoints can be found in the [API Guide](https://piramal-swasthya.gitbook.io/amrit/architecture/api-guide).

## Setting Up Commit Hooks

This project uses Git hooks to enforce consistent code quality and commit message standards. Even though this is a Java project, the hooks are powered by Node.js. Follow these steps to set up the hooks locally:

### Prerequisites
- Node.js (v18 or later)
- npm (comes with Node.js)

### Setup Steps

1. **Install Node.js and npm**
   - Download and install from [nodejs.org](https://nodejs.org/)
   - Verify installation with:
     ```
     node --version
     npm --version
     ```
2. **Install dependencies**
   - From the project root directory, run:
     ```
     npm ci
     ```
   - This will install all required dependencies including Husky and commitlint
3. **Verify hooks installation**
   - The hooks should be automatically installed by Husky
   - You can verify by checking if the `.husky` directory contains executable hooks
### Commit Message Convention
This project follows a specific commit message format:
- Format: `type(scope): subject`
- Example: `feat(login): add remember me functionality`
Types include:
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code changes that neither fix bugs nor add features
- `perf`: Performance improvements
- `test`: Adding or fixing tests
- `build`: Changes to build process or tools
- `ci`: Changes to CI configuration
- `chore`: Other changes (e.g., maintenance tasks, dependencies)
Your commit messages will be automatically validated when you commit, ensuring project consistency.

## Usage
All features have been exposed as REST endpoints. Refer to the SWAGGER API specification for details.

## Filing Issues

If you encounter any issues, bugs, or have feature requests, please file them in the [main AMRIT repository](https://github.com/PSMRI/AMRIT/issues). Centralizing all feedback helps us streamline improvements and address concerns efficiently.  

## Join Our Community

We’d love to have you join our community discussions and get real-time support!  
Join our [Discord server](https://discord.gg/FVQWsf5ENS) to connect with contributors, ask questions, and stay updated.  

