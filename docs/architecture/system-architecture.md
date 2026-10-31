# Kiến trúc hệ thống (System Architecture Specification)

**Dự án:** Skill- and Labor Market Demand–Based Career Recommendation System
*(Hệ thống gợi ý nghề nghiệp dựa trên kỹ năng cá nhân và nhu cầu thị trường lao động)*
**Trạng thái:** Hoàn thiện cho phát triển song song Tuần 2–5 (Production-Ready Architecture)
**Tài liệu tham chiếu chuẩn:** [`ADDITIONAL_DOC/Báo cáo đồ án.docx`](../requirements/current-state.md), [`AI_RULES.md`](../../AI_RULES.md)

---

## 1. Mục tiêu hệ thống & Phạm vi MVP (System Purpose & MVP Scope)

### 1.1. Mục tiêu cốt lõi
Hệ thống giải quyết bài toán định hướng nghề nghiệp bằng cách kết nối 3 tầng dữ liệu:
1. **Hồ sơ năng lực cá nhân:** Kỹ năng tự khai báo kèm mức độ thành thạo, học vấn, kinh nghiệm và nguyện vọng nghề nghiệp.
2. **Từ điển phân loại chuẩn hóa (Occupational Taxonomy):** Chuẩn O\*NET / ESCO phân định rõ kỹ năng cốt lõi (*essential*) và bổ trợ (*optional*).
3. **Chỉ báo nhu cầu thị trường lao động (Labor Market Information - LMI):** Tốc độ tăng trưởng ngành, mức độ thiếu hụt nhân lực và mức lương tham khảo.

### 1.2. Phạm vi MVP thống nhất (Agreed MVP Scope)
Theo đặc tả chuẩn hóa trong tài liệu đồ án:
* **Bao gồm trong MVP:**
  * Xác thực người dùng và phân quyền RBAC (`ROLE_CANDIDATE`, `ROLE_ADMIN`).
  * Quản lý hồ sơ ứng viên, học vấn, kinh nghiệm, danh mục kỹ năng và mức độ thành thạo.
  * Tra cứu, lọc danh mục nghề nghiệp và xem xu hướng thị trường lao động.
  * Thuật toán gợi ý nghề nghiệp kết hợp độ tương đồng kỹ năng ($Sim$) và trọng số thị trường ($Demand$).
  * Chẩn đoán khoảng cách kỹ năng ($\Delta$) và giải thích căn cứ gợi ý.
  * Giao diện quản trị cập nhật danh mục kỹ năng/nghề và nhập dữ liệu LMI.
* **Loại trừ khỏi phạm vi MVP (Deferred / Out of Scope):**
  * Tích hợp thanh toán, hợp đồng, escrow (các nội dung sao chép nhầm từ mẫu đồ án khác).
  * Ứng dụng di động native (hệ thống tập trung web responsive).
  * Vi dịch vụ AI riêng biệt hoặc hạ tầng streaming phân tán phức tạp (ưu tiên Modular Monolith để 6 thành viên hoàn thành trong 5 tuần).

---

## 2. Kiến trúc tổng thể: Modular Monolith (System Decomposition)

Để đáp ứng ràng buộc thời gian 5 tuần và quy mô đội ngũ 6 thành viên, hệ thống áp dụng mô hình **Modular Monolith** trên nền tảng Spring Boot 3.3.4 (Java 17). Ứng dụng được chia thành 5 phân hệ logic với ranh giới rõ ràng, giao tiếp nội bộ qua Service Interfaces và Event Publication:

```mermaid
graph TD
    subgraph ClientTier ["Tầng Client (Frontend)"]
        SPA["React 18 + TypeScript SPA (Vite)"]
        Router["React Router v6"]
        AxiosClient["Axios HTTP Client (JWT Interceptor)"]
        SPA --- Router --- AxiosClient
    end

    subgraph GatewayTier ["Tầng Reverse Proxy"]
        Nginx["Nginx Reverse Proxy / Static Host (:3000 -> :8080)"]
    end

    subgraph BackendTier ["Tầng Backend (Spring Boot 3.3.4 - Modular Monolith)"]
        Security["Spring Security 6 (JWT Filter & RBAC)"]

        subgraph Modules ["Các phân hệ nghiệp vụ nội bộ"]
            AuthMod["1. Auth Module<br/>(User, Role, JWT Provider)"]
            ProfileMod["2. Candidate Profile Module<br/>(Profile, User Skills)"]
            TaxonomyMod["3. Taxonomy Module<br/>(Skills, Occupations, O*NET)"]
            LMIMod["4. LMI Module<br/>(Market Indicators, Ingestion)"]
            RecMod["5. Recommendation Engine Module<br/>(Matching Algorithm, Gap Analysis)"]
        end

        Security --> Modules
    end

    subgraph DataTier ["Tầng Dữ liệu & Lưu trữ"]
        PostgresDB[("PostgreSQL 16 Relational Database")]
        Flyway["Flyway Migration (Schema History)"]
        PgVector["pgvector Extension (Hỗ trợ Vector Semantic Search sau MVP)"]
        PostgresDB --- Flyway
        PostgresDB -.- PgVector
    end

    AxiosClient -->|REST API Requests :8080| Security
    Nginx -.-> SPA
    Modules -->|Spring Data JPA / HikariCP| PostgresDB
```

* **Kiểm tra trạng thái dịch vụ (Service Health Check - GAP-02):** Module dùng chung (`common`) cung cấp endpoint công khai `GET /api/health` trả về `{"status": "UP"}` với mã `200 OK`, đóng vai trò là điểm kiểm tra tình trạng sống (liveness probe) tức thời cho Docker Compose, Nginx và Frontend mà không phụ thuộc CSDL.

---

## 3. Đặc tả ranh giới phân hệ & Trách nhiệm thành viên (Module Boundaries)

**Cập nhật phân công W1 ngày 2026-10-29:** Theo sheet `Task` của
`CNPM Chuyên Sâu - Nhóm 4.xlsx`, W1-T5 (Auth/JWT) giao **Đoàn Việt Khang**;
W1-T3 (backend nền tảng) giao Phạm Ngọc Gia Khang; W1-T4 (data/mapping) giao
Huỳnh Long Bảo Khanh. Ma trận Tuần 2–5 bên dưới là kế hoạch trước đó,
không dùng để thay thế phân công W1 trong Excel.

Hiện thực W1-T5 dùng `module/auth/{controller,service,dto,entity,repository}`,
`common/security`, `common/exception`, `common/dto` và `config/SecurityConfig`.
`User`/`UserRepository` thuộc auth và là điểm phối hợp với W1-T4; không tạo một
User entity thứ hai ở package phẳng. `AuthenticatedUser(userId,email,role)` là
principal không chứa password hash để các module nhận qua `@AuthenticationPrincipal`.

JWT filter xác minh chữ ký/claim, kiểm tra User còn active và role/email khớp DB,
rồi đưa principal vào SecurityContext. Security stateless, không phát session cookie,
không bật HTTP Basic/form login; CSRF tắt vì dùng Bearer header, không cookie auth.
CORS theo danh sách origin cấu hình; không dùng wildcard credentials.
Chỉ POST register/login và GET health/taxonomy được công khai; admin cần ADMIN,
profile/recommendation cần CANDIDATE; endpoint khác mặc định bị chặn.
Chưa tạo controller nghiệp vụ cho các module khác trong W1-T5.


| STT | Phân hệ (Module) | Trách nhiệm chính | Thực thể quản lý | Thành viên phụ trách |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **Auth & Security** | Đăng ký, đăng nhập, cấp phát JWT, xác thực RBAC | `User`, `Role` | Bùi Vạn Khải (24520719) |
| **2** | **Candidate Profile** | Hồ sơ năng lực, nguyện vọng lương, kỹ năng đã có | `CandidateProfile`, `UserSkill` | Phạm Ngọc Gia Khang (24520765) |
| **3** | **Taxonomy & Occupations** | Danh mục kỹ năng O\*NET/ESCO, chức danh nghề nghiệp | `Skill`, `SkillCategory`, `Occupation`, `OccupationSkill` | Lương Phúc Khang (24520747) |
| **4** | **LMI (Labor Market Info)** | Chỉ số nhu cầu tuyển dụng, tỷ lệ tăng trưởng, thống kê | `MarketDemandIndicator` | Lương Phúc Khang (24520747) |
| **5** | **Recommendation Engine** | Thuật toán đối sánh kỹ năng, tính điểm, chẩn đoán gap | `RecommendationLog` | Bùi Vạn Khải (24520719) |
| **6** | **Candidate Frontend** | Màn hình đăng nhập, hồ sơ, kỹ năng, bảng gợi ý | React Components | Đoàn Việt Khang (24520730) |
| **7** | **Explorer & Admin UI** | Màn hình tra cứu nghề nghiệp, trực quan hóa LMI, Admin | React Components | Huỳnh Long Bảo Khanh (24520781) |
| **8** | **DevOps & QA Testing** | Flyway migration, Docker Compose, CI/CD, Test | Docker, Flyway, Tests | Phan Nguyễn Thông (24521722) |

---

## 4. Các luồng xử lý tuần tự nghiệp vụ cốt lõi (Core Sequence Diagrams)

### 4.1. Luồng Xác thực & Phân quyền (Authentication & Token Issuance)

```mermaid
sequenceDiagram
    autonumber
    actor User as Người dùng
    participant UI as React Frontend
    participant Filter as JwtAuthenticationFilter
    participant AuthCtrl as AuthController
    participant AuthSvc as AuthenticationManager
    participant UserRepo as UserRepository
    participant TokenProv as JwtTokenProvider

    User->>UI: Nhập email & mật khẩu, bấm "Đăng nhập"
    UI->>AuthCtrl: POST /api/v1/auth/login {email, password}
    AuthCtrl->>AuthSvc: authenticate(UsernamePasswordAuthenticationToken)
    AuthSvc->>UserRepo: findByEmail(email)
    UserRepo-->>AuthSvc: Trả về User Entity (kèm password_hash)
    AuthSvc->>AuthSvc: BCryptPasswordEncoder.matches(raw, hash)
    alt Mật khẩu không chính xác
        AuthSvc-->>AuthCtrl: Ném BadCredentialsException
        AuthCtrl-->>UI: 401 Unauthorized {status: 401, error: "Unauthorized"}
        UI-->>User: Hiển thị thông báo "Sai tài khoản hoặc mật khẩu"
    else Xác thực thành công
        AuthSvc-->>AuthCtrl: Authentication Object (Principal, Roles)
        AuthCtrl->>TokenProv: generateToken(authentication)
        TokenProv-->>AuthCtrl: JWT Token String (24h validity, claims: sub, userId, role)
        AuthCtrl-->>UI: 200 OK {accessToken, tokenType: "Bearer", expiresIn, user}
        UI->>UI: Lưu token vào LocalStorage/Memory, gắn vào Axios Interceptor
        UI-->>User: Điều hướng vào Dashboard
    end
```

### 4.2. Luồng Khai báo Hồ sơ & Kỹ năng Ứng viên (Candidate Profile & Skills Submission)

```mermaid
sequenceDiagram
    autonumber
    actor Candidate as Ứng viên
    participant UI as React Frontend
    participant Security as SecurityContext (JWT)
    participant ProfCtrl as ProfileController
    participant ProfSvc as CandidateProfileService
    participant SkillRepo as UserSkillRepository
    participant TaxonRepo as SkillRepository

    Candidate->>UI: Chọn kỹ năng (vd: Java), chọn level "INTERMEDIATE", số năm kinh nghiệm
    UI->>ProfCtrl: POST /api/v1/profile/skills {skillId: 12, proficiencyLevel: "INTERMEDIATE", yearsExperience: 1.5}
    ProfCtrl->>Security: Lấy authenticated userId từ JWT Principal
    Security-->>ProfCtrl: userId = 101
    ProfCtrl->>ProfSvc: addSkillToCandidate(userId=101, skillId=12, level, years)
    ProfSvc->>TaxonRepo: findById(12)
    alt Không tìm thấy kỹ năng trong từ điển
        TaxonRepo-->>ProfSvc: Optional.empty()
        ProfSvc-->>ProfCtrl: Ném ResourceNotFoundException("SKILL_NOT_FOUND")
        ProfCtrl-->>UI: 404 Not Found
    else Kỹ năng hợp lệ
        ProfSvc->>SkillRepo: existsByUserIdAndSkillId(101, 12)
        alt Đã khai báo kỹ năng này trước đó
            ProfSvc-->>ProfCtrl: Ném DuplicateResourceException("SKILL_ALREADY_EXISTS")
            ProfCtrl-->>UI: 409 Conflict
        else Chưa tồn tại
            ProfSvc->>SkillRepo: save(UserSkill)
            SkillRepo-->>ProfSvc: UserSkill saved
            ProfSvc-->>ProfCtrl: UserSkillResponse DTO
            ProfCtrl-->>UI: 201 Created {skillId: 12, name: "Java", level: "INTERMEDIATE"}
            UI-->>Candidate: Cập nhật Badge kỹ năng trên giao diện
        end
    end
```

### 4.3. Luồng Gợi ý Nghề nghiệp & Chẩn đoán Khoảng cách Kỹ năng (Recommendation & Skill Gap)

```mermaid
sequenceDiagram
    autonumber
    actor Candidate as Ứng viên
    participant UI as React Frontend
    participant RecCtrl as RecommendationController
    participant RecSvc as RecommendationEngineService
    participant ProfRepo as CandidateProfileRepository
    participant UserSkillRepo as UserSkillRepository
    participant OccSkillRepo as OccupationSkillRepository
    participant LMIRepo as MarketDemandRepository
    participant RecLogRepo as RecommendationLogRepository

    Candidate->>UI: Nhấp xem "Gợi ý nghề nghiệp phù hợp"
    UI->>RecCtrl: GET /api/v1/recommendations
    RecCtrl->>RecSvc: generateRecommendations(authenticatedUserId)
    RecSvc->>UserSkillRepo: findByUserId(userId)
    UserSkillRepo-->>RecSvc: Danh sách kỹ năng của ứng viên (Skills_cand)
    RecSvc->>ProfRepo: findByUserId(userId)
    ProfRepo-->>RecSvc: Nguyện vọng lương, địa điểm, ngành nghề

    RecSvc->>OccSkillRepo: findAllWithSkills()
    OccSkillRepo-->>RecSvc: Danh sách các nghề và kỹ năng yêu cầu (Skills_occ, Weights)

    RecSvc->>LMIRepo: findLatestIndicators()
    LMIRepo-->>RecSvc: Demand scores & Growth rates theo từng nghề

    loop Cho từng Nghề nghiệp trong danh mục
        RecSvc->>RecSvc: 1. Tính SkillMatch = Tương quan trọng số kỹ năng (Jaccard / Cosine)
        RecSvc->>RecSvc: 2. Lấy DemandScore = Điểm nhu cầu tuyển dụng (thang 0-100)
        RecSvc->>RecSvc: 3. Tính PrefMatch = Độ tương thích lương & địa điểm
        RecSvc->>RecSvc: 4. FinalScore = 0.6 * SkillMatch + 0.3 * DemandScore + 0.1 * PrefMatch
        RecSvc->>RecSvc: 5. Bóc tách SkillGap = Skills_occ (Required) \ Skills_cand
    end

    RecSvc->>RecSvc: Sắp xếp theo FinalScore giảm dần, lấy Top 10
    RecSvc->>RecLogRepo: saveAll(Top recommendations audit log)
    RecSvc-->>RecCtrl: List<RecommendationResponse>
    RecCtrl-->>UI: 200 OK [ {rank: 1, occupation: "Software Engineer", finalScore: 86.4, gapCount: 2, ...} ]
    UI-->>Candidate: Hiển thị danh sách thẻ nghề nghiệp, thanh % tương thích & kỹ năng cần học
```

### 4.4. Luồng Nạp & Đồng bộ Dữ liệu Quản trị LMI (Admin LMI Data Ingestion)

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Quản trị viên
    participant UI as React Admin Portal
    participant Filter as SecurityFilter (ROLE_ADMIN)
    participant AdminCtrl as AdminLMIController
    participant LMISvc as LMIIngestionService
    participant OccRepo as OccupationRepository
    participant LMIRepo as MarketDemandRepository

    Admin->>UI: Tải lên file JSON/CSV chỉ số thị trường mới
    UI->>AdminCtrl: POST /api/v1/admin/lmi/import [ {occupationCode, demandScore, growthRatePct, ...} ]
    AdminCtrl->>Filter: Kiểm tra quyền hạn Token
    alt Không phải ROLE_ADMIN
        Filter-->>AdminCtrl: Quyền hạn không đủ
        AdminCtrl-->>UI: 403 Forbidden {error: "ACCESS_DENIED"}
    else Có quyền ROLE_ADMIN
        AdminCtrl->>LMISvc: ingestMarketDataBatch(records)
        loop Cho từng bản ghi chỉ số LMI
            LMISvc->>OccRepo: findByCode(occupationCode)
            alt Nghề không tồn tại
                LMISvc->>LMISvc: Đánh dấu lỗi bản ghi, bỏ qua hoặc log warning
            else Tìm thấy nghề
                LMISvc->>LMIRepo: upsert(MarketDemandIndicator theo occupation_id, region, snapshot_date)
            end
        end
        LMISvc-->>AdminCtrl: IngestionSummary (processed, inserted, updated, errors)
        AdminCtrl-->>UI: 200 OK {totalProcessed: 50, inserted: 48, updated: 2, errors: 0}
        UI-->>Admin: Hiển thị báo cáo nạp thành công
    end
```

---

## 5. Công thức toán học đề xuất (Recommendation Engine Mathematics)

Điểm phù hợp tổng hợp ($\text{FinalScore}$) giữa ứng viên $u$ và nghề nghiệp $j$ được tính toán minh bạch:

$$\text{FinalScore}(u, j) = 0.6 \cdot \text{SkillMatch}(u, j) + 0.3 \cdot \text{DemandScore}(j) + 0.1 \cdot \text{PrefMatch}(u, j)$$

Trong đó:
1. **Điểm khớp kỹ năng ($\text{SkillMatch} \in [0, 100]$):**
   $$\text{SkillMatch}(u, j) = \frac{\sum_{s \in S_u \cap S_j} w_{j,s} \cdot \mu(u, s)}{\sum_{s \in S_j} w_{j,s}} \times 100$$
   * $S_u$: Tập kỹ năng ứng viên khai báo.
   * $S_j$: Tập kỹ năng yêu cầu của nghề nghiệp $j$.
   * $w_{j,s}$: Trọng số quan trọng của kỹ năng $s$ đối với nghề $j$ ($w_{j,s} \in [0.0, 1.0]$).
   * $\mu(u, s)$: Hệ số thành thạo kỹ năng của ứng viên ($\text{BEGINNER}=0.5$, $\text{INTERMEDIATE}=0.8$, $\text{ADVANCED}=1.0$, $\text{EXPERT}=1.0$).

2. **Điểm nhu cầu thị trường ($\text{DemandScore} \in [0, 100]$):**
   Lấy trực tiếp từ bảng `market_demand_indicators` dựa trên kỳ khảo sát gần nhất.

3. **Điểm tương thích nguyện vọng ($\text{PrefMatch} \in [0, 100]$):**
   Đánh giá độ khớp về dải lương kỳ vọng và địa điểm làm việc ưu tiên.

4. **Tập khoảng cách kỹ năng ($\text{SkillGap}$):**
   $$\text{SkillGap}(u, j) = S_j \setminus S_u = \{s \in S_j \mid s \notin S_u\}$$
   Được phân tách thành:
   * **Khoảng cách cốt lõi (Essential Gaps):** $\{s \in \text{SkillGap} \mid \text{is\_required}(s) = \text{true}\}$.
   * **Khoảng cách bổ trợ (Optional Gaps):** $\{s \in \text{SkillGap} \mid \text{is\_required}(s) = \text{false}\}$.

---

## 6. Xử lý lỗi tập trung & Chuẩn hóa phản hồi (Error Handling)

Mọi ngoại lệ trên toàn hệ thống được bắt tại `GlobalExceptionHandler` (`@RestControllerAdvice`) và chuyển đổi thành định dạng chuẩn:

```json
{
  "timestamp": "2026-10-22T15:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Dữ liệu đầu vào không hợp lệ",
  "path": "/api/v1/profile/skills",
  "errors": [
    {
      "field": "proficiencyLevel",
      "rejectedValue": null,
      "reason": "Cấp độ kỹ năng phải thuộc một trong các giá trị: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT"
    }
  ]
}
```
