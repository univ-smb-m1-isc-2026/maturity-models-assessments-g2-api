# Maturity Models Assessments Groupe 2 — API (Back-end)

> API REST Spring Boot pour l'évaluation d'équipes vis-à-vis de modèles de maturité.

**Authors**:
- Taina Duquenoy
- Cassandre Kant

---

## Table des matières

- [Présentation](#présentation)
- [Stack technique](#stack-technique)
- [Architecture du projet](#architecture-du-projet)
- [Modèle de données](#modèle-de-données)
- [Rôles et permissions](#rôles-et-permissions)
- [Endpoints API](#endpoints-api)
- [Authentification](#authentification)
- [Système d'invitation](#système-dinvitation)
- [Installation et lancement](#installation-et-lancement)
- [Variables d'environnement](#variables-denvironnement)
- [CI/CD](#cicd)

---

## Présentation

Ce projet constitue la partie **back-end** de l'application d'évaluation de maturité. Il expose une API REST permettant de :

- Gérer des **modèles de maturité** (Scrum, Cybersécurité, Qualité, Agile…)
- Gérer des **équipes** et leurs membres
- Lancer des **sessions d'évaluation** auxquelles les membres répondent
- Consulter les **résultats** par session

---

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| Langage | Java 25 |
| Framework | Spring Boot 4.0.3 |
| Sécurité | Spring Security + JWT (jjwt 0.12.6) |
| Persistance | Spring Data JPA + PostgreSQL 16 |
| Documentation API | SpringDoc OpenAPI (Swagger UI) |
| Email | Spring Boot Mail (SMTP Gmail) |
| Build | Maven (Wrapper inclus) |
| Containerisation | Docker + Docker Compose |
| Réduction boilerplate | Lombok |

---

## Architecture du projet

```
src/main/java/fr/univ_smb/info803/maturitymodelsassessmentsapi/
├── config/
│   ├── JwtAuthFilter.java       # Filtre JWT sur chaque requête
│   ├── SecurityConfig.java      # Configuration Spring Security
│   └── WebConfig.java           # Configuration CORS
├── controller/
│   ├── AuthController.java      # Inscription / connexion
│   ├── MaturityModelController.java
│   ├── SessionController.java
│   ├── SessionResultController.java
│   ├── TeamController.java
│   └── UserController.java
├── dto/                         # Objets de transfert (Request / Response)
├── model/                       # Entités JPA
│   ├── Invitation.java
│   ├── MaturityModel.java
│   ├── Question.java
│   ├── QuestionAnswer.java
│   ├── Role.java                # Enum : PMO | TEAM_LEAD | TEAM_MEMBER
│   ├── Session.java
│   ├── SessionResult.java
│   ├── SessionStatus.java       # Enum : PENDING | OPEN | CLOSED
│   ├── Team.java
│   └── User.java
├── repository/                  # Interfaces Spring Data JPA
└── service/                     # Logique métier
```

---

## Modèle de données

```
MaturityModel
  ├── title, description, category, icon
  ├── createdBy → User (PMO)
  └── questions[]
        ├── text, questionOrder
        └── answers[]
              └── value (1–5), answerOrder

Team
  ├── name
  ├── lead → User (TEAM_LEAD)
  └── members[] → User (TEAM_MEMBER)

Session
  ├── name, deadline
  ├── status : PENDING | OPEN | CLOSED
  ├── model → MaturityModel
  └── team → Team

SessionResult
  ├── session → Session
  └── user → User

Invitation
  ├── email, token, expiresAt
  ├── status : PENDING | ACCEPTED | EXPIRED
  ├── team → Team
  └── invitedBy → User
```

---

## Rôles et permissions

| Rôle | Description | Accès |
|------|-------------|-------|
| `PMO` | Propriétaire de modèle | Créer / modifier / supprimer des modèles de maturité |
| `TEAM_LEAD` | Responsable d'équipe | Créer des équipes, inviter des membres, lancer des sessions |
| `TEAM_MEMBER` | Membre d'équipe | Participer aux sessions, consulter les résultats |

> **Note :** un utilisateur invité par lien s'inscrit obligatoirement avec le rôle `TEAM_MEMBER`. La sélection de profil est désactivée pour ce parcours.

---

## Endpoints API

La documentation interactive (Swagger UI) est disponible à : `http://localhost:8080/swagger-ui.html`

### Authentification — `/api/auth`

| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| `POST` | `/api/auth/register` | Public | Inscription avec choix du profil (PMO, TEAM_LEAD) |
| `POST` | `/api/auth/register/{token}` | Public | Inscription via lien d'invitation (rôle forcé à TEAM_MEMBER) |
| `GET` | `/api/auth/invite/{token}` | Public | Validation d'un token d'invitation |
| `POST` | `/api/auth/login` | Public | Connexion, retourne un JWT |

### Utilisateurs — `/api/users`

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `GET` | `/api/users` | Liste tous les utilisateurs |
| `GET` | `/api/users/{id}` | Récupère un utilisateur par ID |
| `GET` | `/api/users/me` | Retourne l'utilisateur connecté |
| `GET` | `/api/users?role={role}` | Filtre les utilisateurs par rôle |
| `POST` | `/api/users` | Crée un utilisateur |
| `POST` | `/api/users/batch` | Récupère plusieurs utilisateurs par IDs |
| `PUT` | `/api/users/{id}` | Met à jour un utilisateur |
| `DELETE` | `/api/users/{id}` | Supprime un utilisateur |

### Modèles de maturité — `/api/models`

| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| `GET` | `/api/models` | Authentifié | Liste tous les modèles |
| `GET` | `/api/models/{id}` | Authentifié | Récupère un modèle avec ses questions et réponses |
| `POST` | `/api/models` | `PMO` | Crée un modèle (titre, description, catégorie, icône, questions) |
| `PUT` | `/api/models/{id}` | `PMO` | Met à jour un modèle |
| `DELETE` | `/api/models/{id}` | `PMO` | Supprime un modèle |

### Équipes — `/api/teams`

| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| `GET` | `/api/teams` | Authentifié | Liste toutes les équipes |
| `GET` | `/api/teams/{id}` | Authentifié | Récupère une équipe |
| `GET` | `/api/teams/my-teams` | `TEAM_LEAD` | Équipes dont l'utilisateur est lead |
| `GET` | `/api/teams/my-memberships` | `TEAM_MEMBER` | Équipes dont l'utilisateur est membre |
| `POST` | `/api/teams` | `TEAM_LEAD` | Crée une équipe (le créateur est automatiquement lead) |
| `POST` | `/api/teams/{id}/invitations` | `TEAM_LEAD` | Envoie une invitation par email |
| `PUT` | `/api/teams/{id}` | `TEAM_LEAD` (owner) | Met à jour une équipe |
| `DELETE` | `/api/teams/{id}` | `TEAM_LEAD` (owner) | Supprime une équipe |

### Sessions — `/api/sessions`

| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| `GET` | `/api/sessions` | `ADMIN`, `TEAM_LEAD` | Liste toutes les sessions |
| `GET` | `/api/sessions/{id}` | `ADMIN`, `TEAM_LEAD` | Récupère une session |
| `GET` | `/api/sessions/by-team/{teamId}` | `ADMIN`, `TEAM_LEAD` | Sessions d'une équipe |
| `GET` | `/api/sessions/by-model/{modelId}` | `ADMIN`, `TEAM_LEAD` | Sessions d'un modèle |
| `POST` | `/api/sessions` | `TEAM_LEAD` | Crée une session (status initial : `PENDING`) |
| `DELETE` | `/api/sessions/{id}` | `TEAM_LEAD` (owner) | Supprime une session |

---

## Authentification

L'API utilise **JWT (JSON Web Token)** pour l'authentification :

1. Le client s'authentifie via `POST /api/auth/login` et reçoit un token JWT.
2. Ce token doit être inclus dans le header `Authorization` de chaque requête protégée :
   ```
   Authorization: Bearer <token>
   ```
3. Le token a une durée de validité de **24 heures** (configurable via `jwt.expiration`).

---

## Système d'invitation

Le flow d'invitation d'un membre est le suivant :

```
TEAM_LEAD  →  POST /api/teams/{id}/invitations  →  Email envoyé
                                                         ↓
                                              Lien contenant un token UUID
                                                         ↓
INVITÉ     →  GET /api/auth/invite/{token}    →  Validation du token
           →  POST /api/auth/register/{token} →  Inscription (rôle = TEAM_MEMBER)
```

- Le token est à usage unique et a une date d'expiration.
- Un utilisateur inscrit via invitation ne peut pas choisir son rôle.
- L'envoi d'email est configuré via SMTP Gmail.

---

## Installation et lancement

### Prérequis

- Docker et Docker Compose

### Lancement avec Docker Compose

```bash
# Cloner le dépôt
git clone <url-du-repo>
cd maturity-models-assessments-g2-api

# Créer le fichier d'environnement
cp .env.example .env
# Remplir GMAIL_USERNAME et GMAIL_APP_PASSWORD dans .env

# Lancer l'application et la base de données
docker compose up --build
```

L'API sera disponible sur `http://localhost:8080`.

### Lancement en développement (sans Docker)

```bash
# Démarrer une instance PostgreSQL (port 5432, user/password: admin/admin, db: postgres)
# puis :

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Variables d'environnement

| Variable | Description | Exemple |
|----------|-------------|---------|
| `GMAIL_USERNAME` | Adresse Gmail pour l'envoi des invitations | `monapp@gmail.com` |
| `GMAIL_APP_PASSWORD` | Mot de passe d'application Google | `xxxx xxxx xxxx xxxx` |
| `SPRING_PROFILES_ACTIVE` | Profil Spring actif (`dev` ou `prod`) | `dev` |

> Les variables `jwt.secret` et `jwt.expiration` sont définies dans `application-dev.properties`. **Ne pas committer de secrets en production.**

---

## CI/CD

Un pipeline GitHub Actions est configuré (`.github/workflows/build-pipeline.yml`). Il se déclenche à chaque push sur `main` et effectue les étapes suivantes :

1. **Build Maven** — compilation et packaging du JAR (sans tests)
2. **Build & Push Docker** — construction de l'image et publication sur GitHub Container Registry (`ghcr.io`)

L'image produite est disponible à : `ghcr.io/<owner>/<repo>:latest`