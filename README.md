# AMRIT - Common Service 
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![DeepWiki](https://img.shields.io/badge/DeepWiki-PSMRI/Common--API-blue)](https://deepwiki.com/PSMRI/Common-API)


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
Enable git hooks (run once after cloning):
   - Run the command `git config core.hooksPath .git-hooks`.

## Usage
All features have been exposed as REST endpoints. Refer to the SWAGGER API specification for details.

## Filing Issues

If you encounter any issues, bugs, or have feature requests, please file them in the [main AMRIT repository](https://github.com/PSMRI/AMRIT/issues). Centralizing all feedback helps us streamline improvements and address concerns efficiently.  

## Join Our Community

We’d love to have you join our community discussions and get real-time support!  
Join our [Discord server](https://discord.gg/FVQWsf5ENS) to connect with contributors, ask questions, and stay updated.  
