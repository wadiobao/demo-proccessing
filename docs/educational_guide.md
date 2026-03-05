# 🧠 Project Knowledge & Architectural Deep Dive

This guide explains the core technologies and algorithms used in the project, ranging from basic concepts to advanced mathematical implementations.

---

## 1. Item Response Theory (IRT) & Adaptive Testing
This is the "Brain" of the quiz engine, used to estimate user ability ($\theta$) and question difficulty ($b$).

### Basic Concept
Instead of just counting correct answers (Classical Test Theory), IRT looks at *which* questions were answered correctly.
- Getting a hard question right gives more "ability points" than an easy one.
- The system dynamically adjusts the next question's difficulty to match the user's estimated level.

### Deep Dive: 1PL Logistic Model
We use a **1-Parameter Logistic Model**:
$$P(correct) = \frac{1}{1 + e^{-(\theta - b)}}$$
*   $\theta$ (Theta): User's latent ability (usually -3.0 to 3.0).
*   $b$: Question difficulty.

### Deep Dive: Estimation (MAP & Newton-Raphson)
To update $\theta$ after a quiz, we use **Maximum A Posteriori (MAP)** estimation. Since we can't solve this equation directly, we use the **Newton-Raphson** iterative method:
1.  **Likelihood**: Probability of the observed answers given a specific $\theta$.
2.  **Prior**: We assume $\theta$ follows a Normal distribution $N(0, 1)$ to keep estimates stable.
3.  **Iteration**: $\theta_{new} = \theta_{old} - \frac{G'(\theta)}{H''(\theta)}$
    *   $G'$ (Gradient): The direction of steepest ascent for probability.
    *   $H''$ (Hessian): The curvature (how certain we are).

---

## 2. Structural Design: The Strategy Pattern
Used in `DocumentProcessorContext` for multi-format text extraction (PDF, DOCX, TXT, XLSX).

### Basic Concept
Instead of a giant `if-else` block for different file types, we define a standard interface (`IDocumentProcessor`).

### Deep Dive: Implementation
- **Open-Closed Principle**: We can add support for new formats (e.g., Markdown) by just adding a new class with `@Component` without touching existing code.
- **Auto-Discovery**: Spring automatically injects all implementations into a `List<IDocumentProcessor>`, which the context then iterates through to find the one that `supports(contentType)`.

---

## 3. Database Consistency: Optimistic Locking
Used in `UserResource` to handle high-frequency concurrent updates.

### Basic Concept
When two users (or processes) update the same record at the same time, one might overwrite the other. Optimistic locking prevents this without "locking" the database table (which is slow).

### Deep Dive
- **@Version**: We add a version field to the entity.
- **Workflow**:
    1.  Read record (Version 1).
    2.  Modify.
    3.  Save: `UPDATE table SET ... version = 2 WHERE id = X AND version = 1`.
- **Result**: If another process updated it first, the `WHERE version = 1` fails, and Spring throws an `OptimisticLockingFailureException`.
- **Resilience**: We use **Retry Logic** in the service layer to catch this, wait a few ms, and try again automatically.

---

## 4. Community Prestige: Event-Driven Logic
Used in `ReputationService` and `ReputationScheduler`.

### Basic Concept
User roles are not static; they are earned. 

### Deep Dive
- **Idempotency**: Before recording a vote, we check `findByVoterAndTargetPost`. This ensures `1 Vote = 1 Change`, preventing "vote spamming."
- **Cron Scheduling**: Using `@Scheduled(cron = "0 0 0 1 * ?")`, the system resets penalties at the exact second the month rolls over. This is "Stateful Automation"—the code manages its own lifecycle.

---

## 5. Security: RBAC (Role-Based Access Control)
### Basic Concept
Gating features based on "how much the system trusts you."

### Deep Dive: The Prestige-Gating Loop
- **RESTRICTED**: Tier assigned to negative reputation (cannot upload).
- **EXPERT**: Tier assigned to high reputation (>500 points).
- **Logic**: Security is checked at the **Service Layer**, not just the Controller. This ensures that even if an API is bypassed, the core business logic remains protected.

---

## 💊 Summary Checklist for Mastery
- [ ] Can you explain why we use Newton-Raphson instead of simple averages?
- [ ] How does the Strategy Pattern help when adding a new `.csv` processor?
- [ ] What happens to a user's Tier if their reputation drops to -10?
- [ ] Why is `@Version` better than `synchronized` for a web service?
