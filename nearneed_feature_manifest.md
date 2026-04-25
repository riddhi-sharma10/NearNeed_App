# NearNeed: The Complete Technical Manifest

A comprehensive breakdown of the entire NearNeed ecosystem, spanning frontend aesthetics, backend logic, machine learning, and infrastructure.

---

## 🎨 1. Frontend & UI/UX Features
The visual layer designed to be "Premium, Modern, and Dynamic."

### Core Layouts & Components
*   **Glassmorphism Navigation**: Floating, semi-transparent bottom navigation using blur-style backgrounds.
*   **Material 3 Design System**: Utilization of modern Material components (Cards, Buttons, Chips) with custom Sapphire/Teal color palettes.
*   **Dual-Interface Dashboard**: Separate, high-fidelity homescreens for Seekers (Service discovery) and Providers (Task/Earnings management).
*   **Custom Bottom Sheets**: Interactive sheets for applying to Gigs and volunteering for community help.
*   **StatusBar Sync**: Dynamic coloring of the Android status bar to match app themes (Transparent/Light/Dark).
*   **Empty State Illustrations**: Custom graphics and text that appear during initial loads or empty search results.
*   **Multi-Step Wizard**: Linear workflows for "Create Post" and "Profile Setup" to reduce user cognitive load.

### Interactive Elements
*   **Real-time Search Prediction**: List filtering as you type in the search bar.
*   **Multimedia Chat Bubbles**: Distinctive styling for text, image-sharing, and voice-note players.
*   **Role-Switching Animation**: Smooth transitions when toggling between Seeker and Provider roles.
*   **Floating Action Buttons (FAB)**: Contextual buttons (e.g., "Post Now") that appear only when needed.
*   **Dynamic Word Counters**: Real-time feedback in text inputs for Gig descriptions.
*   **Unread Indicators**: Blue dots and badge counters on message and notification icons.

---

## ⚙️ 2. Backend & Business Logic
The "Brain" of the application handling data flow and rules.

*   **Repository Pattern**: Centralized data management (`PostRepository`, `UserRepository`, `ChatRepository`, etc.) decoupling UI from Data sources.
*   **ViewModel Architecture**: Lifecycle-aware data observation using `LiveData` to prevent memory leaks and handle rotation.
*   **Haversine Distance Engine**: Client-side calculation of the distance between user GPS and job locations.
*   **Role Manager**: Singleton utility that governs the global state and permissions of the logged-in user.
*   **Notification Center**: Unified logic for managing in-app notifications and unread counts.
*   **Booking Lifecycle Controller**: State machine logic for jobs (Requested -> Accepted -> In Progress -> Completed).
*   **Wallet Calculation Engine**: Real-time aggregation of "Completed" application values to calculate user balance.

---

## 🤖 3. Machine Learning (ML) Integration
Intelligence layers for safety and automation.

*   **OCR ID Verification**: Using **Google ML Kit Text Recognition** to extract Name/ID from physical documents to prevent fraud.
*   **Category Predictor (NLP)**: A text-classification utility that analyzes titles/descriptions to suggest service categories (e.g., "Plumbing").
*   **Image Moderation Pipeline**: A safety check in `StorageRepository` that pre-scans media for inappropriate content before upload.

---

## 🔥 4. Firebase Infrastructure
The cloud backbone of the application.

*   **Firebase Authentication**: Secure user identity management (Email/Password & OTP placeholders).
*   **Cloud Firestore**: NoSQL real-time database for all app data (Users, Posts, Messages).
*   **Firebase Cloud Storage**: Secure hosting for profile pictures, post images, and voice notes.
*   **Firebase Cloud Messaging (FCM)**: Infrastructure for sending push notifications when the app is closed.
*   **Security Rules**: Granular JSON-based rules to ensure users can only access their own private data and messages.

---

## 💾 5. Database Schema & Architecture
How data is structured and cached.

### Cloud (Firestore Collections)
*   `Users`: Profiles, roles, verification status, aggregate ratings, and FCM tokens.
*   `posts`: All Gigs and Community requests with geo-coordinates and media URLs.
*   `applications`: Links between Providers and Posts with custom budgets and status.
*   `bookings`: Active job contracts between Seekers and Providers.
*   `messages`: Real-time chat threads with participant IDs and message sub-collections.
*   `notifications`: Per-user alert history.
*   `reviews`: User feedback and star-ratings.

### Local (Room Database - SQLite)
*   **Offline Caching**: Locally stores the most recent posts and messages.
*   **PostEntity**: Structured schema for fast, offline-first home screen loading.
*   **DAO (Data Access Objects)**: Optimized SQL queries for local data retrieval.

---

## 🔐 6. OAuth & Auth Flows
*   **Email/Password Flow**: Traditional secure authentication.
*   **OTP UI Workflow**: Prepared interface for 6-digit SMS verification.
*   **User Persistence**: Logic to keep users logged in across app restarts using Encrypted Preferences.

---

## 📚 7. Library & Module Dependency Guide

| Library | Use Case |
| :--- | :--- |
| **Firebase BOM** | Centralized versioning for all Firebase services (Auth, Firestore, Storage, FCM). |
| **Google ML Kit** | On-device machine learning for OCR and Text Classification. |
| **MapLibre GL** | High-performance, vector-based interactive maps (replacing Google Maps for flexibility). |
| **Glide** | Efficient image loading, caching, and circular transformations for profile pictures. |
| **Room Persistence** | SQLite abstraction for local data storage and offline-first support. |
| **LiveData / ViewModel** | Reactive programming components for real-time UI updates. |
| **OkHttp** | Underlying network client for secure API and image requests. |
| **GMS Location** | `FusedLocationProviderClient` for fetching precise GPS coordinates. |
| **GSON** | Fast JSON serialization/deserialization for complex data models. |
| **Material Components** | Standard library for modern Android UI elements (Material 3). |

---
**Total Components: 70+**
