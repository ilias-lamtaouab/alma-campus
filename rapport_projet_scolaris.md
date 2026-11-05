# RAPPORT DE PROJET DE DÉVELOPPEMENT LOGICIEL

## Conception et Réalisation d'une Application Desktop de Gestion de Scolarité (JScolaris)

**Réalisé par :** [Votre Nom/Prénom]
**Technologies :** Java, JavaFX, MySQL
**Environnement de Développement :** Visual Studio Code

---

## TABLE DES MATIÈRES

1. [Introduction Générale](#1-introduction-générale)
2. [Technologies Utilisées](#2-technologies-utilisées)
3. [Architecture Générale](#3-architecture-générale)
4. [Analyse de la Base de Données](#4-analyse-de-la-base-de-données)
5. [Modules de l'Application](#5-modules-de-lapplication)
    - 5.1. Module d'Authentification
    - 5.2. Module Espace Étudiant
    - 5.3. Module Espace Enseignant
    - 5.4. Module Espace Administrateur
6. [Interfaces Utilisateur et Workflows](#6-interfaces-utilisateur-et-workflows)
7. [Analyse des Boutons et Fonctionnalités](#7-analyse-des-boutons-et-fonctionnalités)
8. [Sécurité et Gestion des Données](#8-sécurité-et-gestion-des-données)
9. [Points Forts de l'Application](#9-points-forts-de-lapplication)
10. [Conclusion](#10-conclusion)

---

## 1. INTRODUCTION GÉNÉRALE

### Contexte du Projet
Dans l'ère numérique actuelle, la gestion administrative et pédagogique des établissements d'enseignement nécessite des outils robustes, rapides et intuitifs. Le projet **JScolaris** s'inscrit dans cette dynamique de transformation digitale en proposant une solution logicielle Desktop complète pour la gestion de la scolarité.

### Objectifs
L'objectif principal de ce projet est de concevoir et développer une application de bureau performante permettant de centraliser les données scolaires. Les objectifs spécifiques incluent :
- La numérisation des dossiers étudiants et enseignants.
- La gestion fluide des notes, des modules et des matières.
- La mise en place d'un système de demande de documents administratifs (attestations, bulletins).
- Le partage de ressources pédagogiques entre enseignants et étudiants.

### Utilisateurs Cibles
L'application cible trois types d'utilisateurs avec des droits d'accès distincts :
1. **L'Administrateur** : Supervise l'ensemble du système, gère les utilisateurs, les inscriptions et valide les demandes de documents.
2. **L'Enseignant** : Gère ses modules, dépose des ressources pédagogiques et suit les matières qu'il enseigne.
3. **L'Étudiant** : Consulte ses notes, télécharge les cours, suit ses enseignements et effectue des demandes de documents administratifs.

---

## 2. TECHNOLOGIES UTILISÉES

Le développement de JScolaris repose sur une stack technologique moderne et éprouvée pour les applications de bureau :

### Java (JDK 17+)
Le langage de programmation principal choisi est Java, reconnu pour sa robustesse, sa portabilité, son approche orientée objet stricte et sa vaste communauté. Il permet de garantir la stabilité de la logique métier et des interactions avec la base de données.

### JavaFX
Pour la conception de l'Interface Utilisateur (UI), JavaFX a été préféré à Swing pour ses capacités graphiques avancées. JavaFX permet l'utilisation de fichiers FXML pour séparer la vue du contrôleur, et intègre le support du CSS pour un design moderne, réactif et esthétiquement plaisant.

### Base de données : MySQL
Le stockage des données relationnelles est assuré par MySQL. Il offre une excellente gestion de l'intégrité référentielle, des performances élevées pour les requêtes complexes et une scalabilité adaptée aux besoins d'un établissement scolaire.

### Outils de Développement
- **Visual Studio Code (VS Code)** : L'éditeur de code utilisé pour le développement, avec les extensions Java Extension Pack et JavaFX pour une productivité optimale.
- **Maven** : Utilisé pour la gestion des dépendances (JDBC, BCrypt, FontAwesome) et la compilation du projet.

### Bibliothèques et Frameworks
- **BCrypt** : Pour le hachage sécurisé des mots de passe.
- **FontAwesome (de.jensd.fx.glyphs)** : Pour l'intégration d'icônes vectorielles modernes dans l'interface utilisateur.

---

## 3. ARCHITECTURE GÉNÉRALE

L'application respecte l'architecture logicielle **MVC (Model-View-Controller)** couplée avec le pattern **DAO (Data Access Object)**, garantissant un code propre, modulaire et maintenable.

### Structure du Projet
- `com.scolaris.model` : Contient les classes représentant les entités de la base de données (User, Student, Module, Grade, etc.).
- `com.scolaris.dao` : Contient les interfaces et implémentations pour l'accès aux données, encapsulant toutes les requêtes SQL (CRUD).
- `com.scolaris.service` : Contient la logique métier de l'application, servant de pont intermédiaire entre les contrôleurs et les DAO.
- `com.scolaris.controller` : Gère les interactions avec l'utilisateur et met à jour les vues JavaFX.
- `com.scolaris.util` : Utilitaires système comme la connexion à la base de données (Singleton) et le Seeder pour initialiser la base.

### Flux de données (Data Flow)
1. L'utilisateur interagit avec la Vue (fichier FXML rendu par JavaFX).
2. Le Controller capte l'événement (ex: clic sur "Se connecter").
3. Le Controller appelle le Service correspondant (`AuthenticationService`).
4. Le Service applique la logique métier (vérification du statut, hachage) et interroge le DAO (`UserDao`).
5. Le DAO exécute la requête SQL via JDBC et retourne un Objet (Modèle) au Service, qui le remonte au Controller pour mettre à jour la Vue.

---

## 4. ANALYSE DE LA BASE DE DONNÉES

La base de données relationnelle est conçue pour éviter la redondance et garantir l'intégrité des données grâce aux clés étrangères.

### Structure et Tables Principales
- **users** : Gère les comptes de connexion (`username`, `password_hash`, `role`, `status`).
- **students** : Dossiers des étudiants (`cne`, informations personnelles, date d'inscription).
- **teachers** : Dossiers des enseignants (`cin`, spécialité).
- **modules** : Les unités d'enseignement (ex: M1 - JAVA).
- **subjects** : Les matières spécifiques liées à un module et affectées à un enseignant.
- **grades** : Stocke les notes obtenues par les étudiants pour chaque matière.
- **certificate_requests** : Gère les demandes de documents administratifs avec un statut (PENDING, APPROVED, REJECTED).
- **module_resources** : Référence les fichiers (cours, TP, examens) uploadés par les enseignants.
- **system_notifications** : Conserve l'historique des alertes système envoyées aux utilisateurs.

---

## 5. MODULES DE L'APPLICATION

### 5.1. Module d'Authentification

**Objectif :** Sécuriser l'accès à l'application et diriger l'utilisateur vers le bon tableau de bord selon son rôle.

![Figure 1 : Interface de Connexion]([Insérer Capture d'écran de la page login.fxml ici])
*Figure 1 : Interface de Connexion - Design moderne avec effet Glassmorphism.*

**Fonctionnalités et Workflow :**
- L'utilisateur entre son nom d'utilisateur (CNE pour étudiant, CIN pour enseignant, ou "admin") et son mot de passe.
- L'algorithme **BCrypt** vérifie le mot de passe haché.
- En cas de succès, le système vérifie le rôle de l'utilisateur (`role`) et charge le fichier `dashboard.fxml`. Le menu latéral s'adapte dynamiquement pour n'afficher que les boutons autorisés pour ce rôle.

---

### 5.2. Module Espace Étudiant

L'espace étudiant est conçu pour offrir une expérience utilisateur (UX) optimale, permettant à l'étudiant de suivre son parcours académique en toute simplicité.

#### A. Tableau de Bord Étudiant
![Figure 2 : Tableau de Bord Étudiant]([Insérer Capture d'écran de student_home_view.fxml ici])
*Figure 2 : Tableau de bord de l'étudiant affichant les statistiques globales.*

**Analyse :** Ce widget affiche un résumé rapide : le nombre de matières suivies, la moyenne générale estimée et le nombre de demandes de certificats en attente. L'interface inclut des animations de flottement sur la photo de profil pour une sensation de dynamisme.

#### B. Mes Enseignements (Modules & Matières)
![Figure 3 : Liste des Enseignements]([Insérer Capture d'écran de student_courses_view.fxml ici])
*Figure 3 : Vue des matières et modules affectés à l'étudiant.*

**Analyse :** Affiche sous forme de tableau interactif la liste des matières, le module d'appartenance, le coefficient et le nom de l'enseignant responsable. Cela permet à l'étudiant d'avoir une vue claire de son cursus.

#### C. Demandes de Certificats (Nouveau Système)
![Figure 4 : Système de demande de documents]([Insérer Capture d'écran de student_cert_requests_view.fxml ici])
*Figure 4 : Interface de demande d'attestation ou de bulletin.*

**Analyse :** Le système de téléchargement direct a été remplacé par un système de demande administrative professionnel. 
- L'étudiant clique sur "Nouvelle Demande".
- Il choisit le type de document (Attestation de scolarité, Bulletin, Emploi du Temps).
- La demande passe en statut "EN ATTENTE" (Pending) jusqu'à ce que l'administrateur la valide. L'étudiant peut télécharger le document uniquement après approbation.

#### D. Mes Cours (Ressources Pédagogiques)
![Figure 5 : Mes Cours - Ressources]([Insérer Capture d'écran de student_resources_view.fxml ici])
*Figure 5 : Interface des ressources avec système d'accordéon et icônes vectorielles.*

**Analyse :** C'est ici que l'étudiant consulte et télécharge les cours. 
- Les modules sont présentés sous forme de cartes élégantes avec des badges "S3" (Semestre 3).
- Un clic sur le titre de la matière (Top Row) déploie un accordéon révélant les fichiers déposés par l'enseignant (ex: "EXAMIN PHP").
- L'icône de téléchargement en SVG moderne et le clic sur la ligne entière permettent d'ouvrir automatiquement le fichier via le lecteur par défaut du système d'exploitation (`Desktop.getDesktop().open()`). Une résolution dynamique des chemins de fichiers a été implémentée pour garantir que les fichiers s'ouvrent même si le projet est déplacé.

---

### 5.3. Module Espace Enseignant

Ce module est l'outil de travail du corps professoral pour la gestion pédagogique.

![Figure 6 : Espace Enseignant]([Insérer Capture d'écran de teacher_module_view.fxml ici])
*Figure 6 : Interface de gestion pour l'enseignant.*

**Fonctionnalités :**
- **Dépôt de ressources :** L'enseignant sélectionne le module qu'il enseigne et upload des fichiers (PDF, Word, Images). Le système copie physiquement le fichier dans un dossier sécurisé (`resources/modules/ID/`) et enregistre le lien dans la base de données.
- **Suivi des matières :** Un tableau de bord affiche à l'enseignant toutes les matières qui lui sont assignées avec les coefficients correspondants.

---

### 5.4. Module Espace Administrateur

C'est le centre de contrôle de l'application, réservé au personnel de l'administration.

![Figure 7 : Espace Administrateur]([Insérer Capture d'écran de admin_dashboard.fxml ici])
*Figure 7 : Tableau de bord d'administration.*

**Fonctionnalités :**
1. **Gestion des Utilisateurs :** Création, modification, et blocage de comptes. Le statut "BLOCKED" empêche instantanément une connexion future.
2. **Gestion des Étudiants et Enseignants :** Ajout de profils, mise à jour des coordonnées.
3. **Traitement des Demandes :** Un onglet dédié permet à l'administrateur de voir toutes les requêtes de documents des étudiants. Il possède des boutons d'action "Approuver" ou "Rejeter".
4. **Gestion des Notes :** Saisie centralisée des notes pour les examens et contrôles continus.

---

## 6. INTERFACES UTILISATEUR ET WORKFLOWS

### Design System et UI/UX
L'application JScolaris a bénéficié d'une conception graphique visant un standard "Entreprise 2026" :
- **Couleurs :** Utilisation de palettes premium (Deep Blue `#1e293b`, Indigo `#5b4db6`, Slate `#f8fafc`).
- **Typographie :** L'interface utilise des polices modernes garantissant une excellente lisibilité.
- **Animations :** Les boutons, les cartes de cours et la photo de profil (Avatar) intègrent des effets de survol (`hover`) fluides et des micro-animations de flottement (`TranslateTransition`) qui rendent l'interface vivante.
- **Menu Latéral (Sidebar) :** Un menu de navigation persistant avec des icônes alignées à gauche et une largeur dynamique offrant une expérience de navigation consistante.

### Workflow d'un Étudiant (Cas d'utilisation type)
1. **Connexion :** L'étudiant lance l'application, saisit son CNE et mot de passe sur la page Login.
   ![Workflow 1 - Connexion]([Insérer Capture Connexion ici])
2. **Accueil :** Il arrive sur le Dashboard, visualise sa moyenne et ses notifications.
   ![Workflow 2 - Accueil]([Insérer Capture Accueil ici])
3. **Téléchargement de Cours :** Il navigue vers "Mes Cours", clique sur le module "M2 - PHP", et clique sur le fichier "EXAMIN PHP" pour l'ouvrir.
   ![Workflow 3 - Cours]([Insérer Capture Cours ici])
4. **Demande de Document :** Il se rend sur "Demandes de Certificats", génère une nouvelle requête pour une Attestation de scolarité.
   ![Workflow 4 - Demandes]([Insérer Capture Demandes ici])
5. **Déconnexion :** Un clic sur le bouton "Déconnexion" en bas du menu latéral ferme sa session, nettoie le cache d'authentification et le ramène à l'écran de connexion de manière sécurisée.

---

## 7. ANALYSE DES BOUTONS ET FONCTIONNALITÉS CLÉS

| Bouton / Élément | Emplacement | Fonction | Résultat Attendu |
|------------------|-------------|----------|------------------|
| **Connexion** | `login.fxml` | Valider les identifiants | Redirection vers le Dashboard approprié. |
| **Mes Enseignements** | Sidebar | Accès aux matières | Affiche la liste des cours de l'étudiant. |
| **Mes Cours** | Sidebar | Accès aux ressources | Affiche l'accordéon des modules avec badges S3. |
| **Nouvelle Demande** | Demandes Certificats | Initier une requête administrative | Ouvre une boîte de dialogue (ComboBox) pour choisir le type de document. |
| **Icône Télécharger (SVG)**| Cartes de Cours | Ouvrir un fichier de cours | Le fichier PDF/Doc s'ouvre dans le lecteur natif de l'ordinateur. |
| **Approuver / Rejeter** | Espace Admin | Valider une demande | Change le statut de la demande et notifie l'étudiant. |
| **Déconnexion** | Sidebar (Bas) | Quitter la session en toute sécurité | Retour sécurisé à l'écran de login. |

---

## 8. SÉCURITÉ ET GESTION DES DONNÉES

- **Chiffrement des mots de passe :** L'utilisation de **BCrypt** garantit qu'aucun mot de passe n'est stocké en clair dans la base de données, rendant l'application résistante aux cyber-attaques.
- **Protection des Routes (RBAC) :** Le contrôleur principal `DashboardController` instancie dynamiquement les boutons du menu en fonction du rôle (Role Based Access Control). Un étudiant ne peut physiquement pas voir ni cliquer sur le bouton d'administration.
- **Intégrité Référentielle MySQL :** En base de données, l'utilisation de `ON DELETE CASCADE` assure que si un étudiant est supprimé, ses notes et demandes de certificats le sont également, évitant les fuites de données (Data Leaks).
- **Résolution Dynamique des Chemins (Path Resolution) :** Les fichiers pédagogiques téléversés par les enseignants sont stockés localement. Le contrôleur résout dynamiquement le chemin `resources/modules/...` par rapport au répertoire courant (`user.dir`), ce qui permet au projet d'être déplacé sur n'importe quel ordinateur Windows sans casser les liens de téléchargement.

---

## 9. POINTS FORTS DE L'APPLICATION

1. **Expérience Utilisateur (UX) Exceptionnelle :** Le design n'a rien à envier aux applications Web modernes grâce à une maîtrise poussée du CSS sous JavaFX (effets Glassmorphism, ombres portées douces, bordures arrondies).
2. **Architecture Logicielle Solide :** La séparation stricte entre Vues (FXML), Contrôleurs, Services et Modèles (MVC) rend l'application hautement maintenable et évolutive par d'autres développeurs.
3. **Résolution Automatique de Problèmes (Self-Healing) :** Le code Java intègre des scripts de migration (ex: `DatabaseSeeder.java`) capables de détecter des incohérences de base de données (mise à jour automatique vers le Semestre 3) et d'appliquer des corrections SQL automatiquement au lancement.
4. **Gestion Multimédia Native :** Contrairement à de simples applications CRUD textuelles, JScolaris interagit avec le système de fichiers de l'OS (`Desktop.getDesktop()`) pour le stockage et l'ouverture native de documents pédagogiques (PDF, Word, Images).

---

## 10. CONCLUSION

Le projet **JScolaris** démontre une application concrète, complète et réussie des concepts avancés de l'ingénierie logicielle en Java. En partant d'un cahier des charges exigeant, l'application a été structurée pour répondre avec précision aux besoins réels de gestion d'un établissement scolaire.

Les objectifs initiaux ont été largement dépassés, tout particulièrement en ce qui concerne la qualité visuelle de l'interface qui offre un rendu "Entreprise Premium", ainsi que la robustesse des fonctionnalités en arrière-plan (système d'approbation de documents, architecture MVC stricte, sécurité BCrypt).

**Perspectives d'évolution à court terme :**
À l'avenir, l'application pourrait être enrichie par l'ajout d'un module de messagerie instantanée en temps réel entre enseignants et étudiants (via Sockets), ainsi que la génération automatisée de fichiers PDF officiels pour les bulletins de notes et les attestations.

---
*Ce rapport certifie l'achèvement du projet de développement logiciel JScolaris et documente l'intégralité du code source livré.*
