# NearNeed System Verification Checklist

Use this checklist to verify that all backend integrations and real-time features are functioning correctly.

## 1. Authentication & Onboarding
- [ ] **Signup**: Creating a new account adds a document to the `Users` collection in Firestore.
- [ ] **Login**: Correct credentials allow access; incorrect credentials show an error toast.
- [ ] **Role Selection**: Switching roles in the drawer updates `RoleManager` and changes the UI (Blue for Seeker, Teal for Provider).
- [ ] **Profile Setup**: Profile details (name, phone, bio) are correctly saved to Firestore and persist across sessions.

## 2. Post Creation (Seeker)
- [ ] **Gig Post**: Creating a Gig (Step 1 & 2) saves a document to `posts` with `type: "GIG"`.
- [ ] **Community Post**: Creating a Community request saves a document to `posts` with `type: "COMMUNITY"`.
- [ ] **Media Upload**: Selected images for a post are uploaded to Firebase Storage and URLs are added to the post document.
- [ ] **Real-time Update**: New posts immediately appear in the "Nearby Requests" list on the Home screen.

## 3. Discovery & Maps
- [ ] **Nearby List**: Home screen displays real posts from Firestore, not dummy data.
- [ ] **Map Markers**: `MapsFragment` shows markers for active posts.
- [ ] **Marker Interaction**: Clicking a marker shows correct post details (Title, Price/Type).
- [ ] **Real-time Map**: If a post is deleted or its status changes, it should disappear from the map.

## 4. Application Flow (Provider)
- [ ] **Apply for Gig**: Clicking "Apply" on a Gig creates a document in the `applications` collection.
- [ ] **Volunteer for Community**: Clicking "Volunteer" creates an application with a $0 budget.
- [ ] **Status Notification**: The Seeker receives an in-app notification immediately after an application is submitted.
- [ ] **Applicants List**: Seeker can see the live list of applicants in `ResponsesActivity` or `VolunteersActivity`.

## 5. Real-Time Messaging
- [ ] **Text Messaging**: Messages appear instantly on both devices.
- [ ] **Image Sending**: Images are uploaded to Storage and rendered in the chat bubble.
- [ ] **Voice Notes**: Recording a voice note uploads an `.m4a` file and provides a playable player for the receiver.
- [ ] **Inbox (MessagesFragment)**: Displays the latest message snippet and an unread indicator (Blue dot).

## 6. Booking Lifecycle
- [ ] **Acceptance**: Seeker accepting an applicant updates application status to `accepted` and creates a `bookings` document.
- [ ] **Completion**: Provider marking a job as complete updates the booking and application status to `completed`.
- [ ] **Live Wallet**: Provider's "Total Balance" in `MyEarningsActivity` increases instantly after a job is marked completed.

## 7. Notifications & Security
- [ ] **Notification Center**: Bell icon shows red dot for new notifications.
- [ ] **Persistence**: Refreshing the app or logging out/in retains all previous notifications and earnings data.
- [ ] **Access Control**: Users can only see messages/applications they are participants in (Verified via Firestore Rules).
