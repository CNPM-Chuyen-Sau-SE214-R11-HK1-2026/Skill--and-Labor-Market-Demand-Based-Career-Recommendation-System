-- =============================================================================
-- Migration: V3__seed_taxonomy_and_occupations.sql
-- Description: Seed Data for Skill Categories, Skills Dictionary, Occupation Groups,
--              Standardized Occupations, Occupation Skills, and Market Demand Indicators.
-- Architecture: ADR-0007 (Flyway Migration Numbering and Seed Data Strategy)
-- Target DBMS: PostgreSQL 16
-- Owner: Huỳnh Long Bảo Khanh (W2-T3)
-- =============================================================================

-- 1. Skill Categories (Taxonomy Groupings)
INSERT INTO skill_categories (code, name, description) VALUES
('CAT-PROG', 'Ngôn ngữ Lập trình', 'Các ngôn ngữ lập trình chính trong phát triển phần mềm và dịch vụ số'),
('CAT-FW', 'Framework & Thư viện', 'Các khung phát triển và thư viện chuyên dụng cho Frontend, Backend và Fullstack'),
('CAT-DB', 'Cơ sở dữ liệu & Lưu trữ', 'Hệ quản trị cơ sở dữ liệu quan hệ, NoSQL và giải pháp bộ nhớ đệm'),
('CAT-CLOUD', 'Cloud & DevOps', 'Nền tảng điện toán đám mây, tự động hóa CI/CD và điều phối hạ tầng container'),
('CAT-AI', 'Trí tuệ Nhân tạo & Dữ liệu', 'Khai phá dữ liệu lớn, xử lý ngôn ngữ tự nhiên và xây dựng mô hình học máy'),
('CAT-SOFT', 'Kỹ năng Mềm & Phương pháp', 'Kỹ năng làm việc nhóm, giao tiếp kỹ thuật, tư duy phân tích và giải quyết vấn đề')
ON CONFLICT (code) DO NOTHING;

-- 2. Skills (Standardized Skill Dictionary)
INSERT INTO skills (category_id, code, name, skill_type, description)
SELECT c.id, s.code, s.name, s.skill_type, s.description
FROM (VALUES
    ('CAT-PROG', 'SK-JAVA', 'Java', 'TECHNICAL', 'Ngôn ngữ lập trình hướng đối tượng mạnh mẽ cho các hệ thống doanh nghiệp lớn'),
    ('CAT-PROG', 'SK-PYTHON', 'Python', 'TECHNICAL', 'Ngôn ngữ đa năng cho phân tích dữ liệu, trí tuệ nhân tạo và backend'),
    ('CAT-PROG', 'SK-TS', 'TypeScript', 'TECHNICAL', 'Ngôn ngữ mở rộng có định kiểu tĩnh của JavaScript dùng cho ứng dụng web hiện đại'),
    ('CAT-PROG', 'SK-JS', 'JavaScript', 'TECHNICAL', 'Ngôn ngữ kịch bản nền tảng của web cho cả client-side và server-side'),
    ('CAT-PROG', 'SK-SQL', 'SQL', 'TECHNICAL', 'Ngôn ngữ truy vấn có cấu trúc cho quản lý và xử lý dữ liệu quan hệ'),
    ('CAT-PROG', 'SK-CPP', 'C++', 'TECHNICAL', 'Ngôn ngữ hiệu năng cao cho phát triển hệ thống nhúng và xử lý tính toán chuyên sâu'),
    ('CAT-PROG', 'SK-GO', 'Go (Golang)', 'TECHNICAL', 'Ngôn ngữ biên dịch tối ưu cho các dịch vụ microservices và hạ tầng đám mây'),
    ('CAT-PROG', 'SK-CSHARP', 'C#', 'TECHNICAL', 'Ngôn ngữ lập trình hướng đối tượng trong hệ sinh thái .NET của Microsoft'),

    ('CAT-FW', 'SK-SPRING', 'Spring Boot', 'TECHNICAL', 'Framework hàng đầu của Java để xây dựng ứng dụng web và RESTful microservices'),
    ('CAT-FW', 'SK-REACT', 'React.js', 'TECHNICAL', 'Thư viện JavaScript khai báo xây dựng giao diện người dùng dựa trên components'),
    ('CAT-FW', 'SK-NODE', 'Node.js', 'TECHNICAL', 'Môi trường thực thi JavaScript phía máy chủ hiệu năng cao dựa trên V8 engine'),
    ('CAT-FW', 'SK-EXPRESS', 'Express.js', 'TECHNICAL', 'Framework tối giản và linh hoạt cho ứng dụng web và API trên nền Node.js'),
    ('CAT-FW', 'SK-NEXT', 'Next.js', 'TECHNICAL', 'React framework hỗ trợ Server-Side Rendering (SSR) và Static Site Generation'),
    ('CAT-FW', 'SK-DJANGO', 'Django', 'TECHNICAL', 'High-level Python web framework tối ưu hóa quy trình phát triển nhanh'),
    ('CAT-FW', 'SK-FASTAPI', 'FastAPI', 'TECHNICAL', 'Modern, high-performance web framework của Python để xây dựng REST APIs'),
    ('CAT-FW', 'SK-VUE', 'Vue.js', 'TECHNICAL', 'Progressive JavaScript framework cho giao diện người dùng trực quan'),

    ('CAT-DB', 'SK-POSTGRES', 'PostgreSQL', 'TECHNICAL', 'Hệ quản trị CSDL quan hệ mã nguồn mở mạnh mẽ và có độ tin cậy cao'),
    ('CAT-DB', 'SK-MYSQL', 'MySQL', 'TECHNICAL', 'Hệ quản trị CSDL quan hệ phổ biến cho ứng dụng web'),
    ('CAT-DB', 'SK-MONGO', 'MongoDB', 'TECHNICAL', 'CSDL NoSQL dạng tài liệu linh hoạt cho dữ liệu phi cấu trúc'),
    ('CAT-DB', 'SK-REDIS', 'Redis', 'TECHNICAL', 'In-memory data structure store dùng làm caching, message broker và session store'),
    ('CAT-DB', 'SK-ELASTIC', 'Elasticsearch', 'TECHNICAL', 'Bộ máy tìm kiếm và phân tích dữ liệu phân tán RESTful'),

    ('CAT-CLOUD', 'SK-DOCKER', 'Docker & Containerization', 'TECHNICAL', 'Công nghệ đóng gói ứng dụng độc lập trong các container nhẹ'),
    ('CAT-CLOUD', 'SK-K8S', 'Kubernetes', 'TECHNICAL', 'Nền tảng điều phối, mở rộng và quản trị container tự động hóa'),
    ('CAT-CLOUD', 'SK-AWS', 'Amazon Web Services (AWS)', 'TECHNICAL', 'Hạ tầng điện toán đám mây toàn diện hàng đầu thế giới'),
    ('CAT-CLOUD', 'SK-GIT', 'Git & GitHub', 'TECHNICAL', 'Hệ thống quản lý phiên bản phân tán và nền tảng cộng tác mã nguồn'),
    ('CAT-CLOUD', 'SK-CICD', 'CI/CD Pipelines', 'TECHNICAL', 'Tự động hóa tích hợp liên tục và triển khai liên tục mã nguồn'),
    ('CAT-CLOUD', 'SK-LINUX', 'Linux Administration', 'TECHNICAL', 'Kỹ năng quản trị hệ điều hành máy chủ Linux và Shell scripting'),
    ('CAT-CLOUD', 'SK-TF', 'Terraform', 'TECHNICAL', 'Công cụ Infrastructure as Code (IaC) quản lý hạ tầng đa đám mây'),

    ('CAT-AI', 'SK-ML', 'Machine Learning', 'TECHNICAL', 'Kỹ thuật huấn luyện thuật toán học máy giải quyết bài toán dự báo và phân loại'),
    ('CAT-AI', 'SK-DL', 'Deep Learning', 'TECHNICAL', 'Mạng nơ-ron sâu cho thị giác máy tính và học biểu diễn phức tạp'),
    ('CAT-AI', 'SK-DATA-ENG', 'Data Modeling & ETL', 'TECHNICAL', 'Thiết kế luồng dữ liệu, trích xuất và biến đổi dữ liệu lớn (ETL)'),
    ('CAT-AI', 'SK-NLP', 'Natural Language Processing', 'TECHNICAL', 'Xử lý và hiểu ngôn ngữ tự nhiên, embedding và mô hình ngôn ngữ lớn (LLM)'),

    ('CAT-SOFT', 'SK-TEAMWORK', 'Làm việc nhóm (Teamwork)', 'SOFT', 'Khả năng phối hợp hiệu quả trong nhóm Agile/Scrum đa chức năng'),
    ('CAT-SOFT', 'SK-COMM', 'Giao tiếp Kỹ thuật (Communication)', 'SOFT', 'Truyền đạt rõ ràng các giải pháp kỹ thuật và tài liệu hóa kiến trúc'),
    ('CAT-SOFT', 'SK-PROB-SOLVING', 'Giải quyết vấn đề (Problem Solving)', 'SOFT', 'Tư duy phản biện và khả năng khắc phục sự cố hệ thống phức tạp')
) AS s(cat_code, code, name, skill_type, description)
JOIN skill_categories c ON c.code = s.cat_code
ON CONFLICT (code) DO NOTHING;

-- 3. Occupation Groups (Standardized SOC Major Families)
INSERT INTO occupation_groups (code, name, description) VALUES
('GRP-SWE', 'Kỹ thuật & Phát triển Phần mềm', 'Các ngành nghề thiết kế, lập trình, kiểm thử và duy trì phần mềm ứng dụng và hệ thống'),
('GRP-DATA', 'Dữ liệu & Trí tuệ Nhân tạo', 'Các ngành nghề phân tích, khai phá dữ liệu lớn và triển khai mô hình học máy / AI'),
('GRP-SYS', 'Hạ tầng Đám mây & Vận hành Hệ thống', 'Các ngành nghề quản trị hạ tầng, DevOps, điều phối đám mây và an toàn thông tin')
ON CONFLICT (code) DO NOTHING;

-- 4. Occupations (Standardized Job Roles)
INSERT INTO occupations (group_id, code, title, description, industry, entry_salary_avg, mid_salary_avg)
SELECT g.id, o.code, o.title, o.description, o.industry, o.entry_salary_avg, o.mid_salary_avg
FROM (VALUES
    ('GRP-SWE', 'SOC-15-1252', 'Software Developer / Kỹ sư phần mềm',
     'Nghiên cứu, thiết kế, phát triển và thử nghiệm các hệ thống phần mềm ứng dụng và dịch vụ số quy mô lớn.',
     'Công nghệ Thông tin', 15000000.00, 35000000.00),

    ('GRP-SWE', 'SOC-15-1253', 'Frontend Engineer / Kỹ sư giao diện',
     'Thiết kế và triển khai giao diện người dùng tương tác, tối ưu trải nghiệm và khả năng phản hồi trên đa nền tảng.',
     'Công nghệ Thông tin', 14000000.00, 30000000.00),

    ('GRP-SWE', 'SOC-15-1254', 'Backend Engineer / Kỹ sư phía máy chủ',
     'Xây dựng các dịch vụ backend, logic nghiệp vụ, tích hợp cơ sở dữ liệu và tối ưu hiệu năng API.',
     'Công nghệ Thông tin', 16000000.00, 38000000.00),

    ('GRP-SWE', 'SOC-15-1255', 'Fullstack Developer / Lập trình viên toàn diện',
     'Đảm nhiệm toàn diện từ phát triển giao diện phía người dùng đến kiến trúc dịch vụ và CSDL phía máy chủ.',
     'Công nghệ Thông tin', 16000000.00, 36000000.00),

    ('GRP-SWE', 'SOC-15-1256', 'Mobile Application Developer / Lập trình viên di động',
     'Xây dựng ứng dụng di động cho hệ điều hành iOS và Android với hiệu năng cao và giao diện mượt mà.',
     'Công nghệ Thông tin', 15000000.00, 32000000.00),

    ('GRP-SWE', 'SOC-15-1251', 'QA / Automation Test Engineer / Kỹ sư kiểm thử tự động',
     'Thiết kế và tự động hóa các kịch bản kiểm thử phần mềm, bảo đảm chất lượng và tính ổn định của hệ thống.',
     'Công nghệ Thông tin', 13000000.00, 28000000.00),

    ('GRP-SYS', 'SOC-15-1257', 'DevOps Engineer / Kỹ sư vận hành & triển khai',
     'Thiết lập pipeline CI/CD, tự động hóa hạ tầng đám mây và tối ưu hóa chu kỳ phát hành phần mềm.',
     'Công nghệ Thông tin', 18000000.00, 42000000.00),

    ('GRP-SYS', 'SOC-15-1258', 'Cloud Solutions Architect / Kiến trúc sư đám mây',
     'Hoạch định kiến trúc hệ thống phân tán, bảo đảm khả năng mở rộng, độ sẵn sàng cao và an toàn trên nền tảng Cloud.',
     'Công nghệ Thông tin', 25000000.00, 55000000.00),

    ('GRP-SYS', 'SOC-15-1259', 'Security Engineer / Kỹ sư an toàn thông tin',
     'Đánh giá lỗ hổng bảo mật, thiết lập phòng tuyến an ninh mạng và bảo vệ dữ liệu toàn vẹn cho tổ chức.',
     'Công nghệ Thông tin', 18000000.00, 40000000.00),

    ('GRP-DATA', 'SOC-15-2051', 'Data Scientist / Nhà khoa học dữ liệu',
     'Khai phá dữ liệu chuyên sâu, xây dựng mô hình dự báo và chuyển hóa dữ liệu thành giá trị chiến lược kinh doanh.',
     'Công nghệ Thông tin', 18000000.00, 45000000.00),

    ('GRP-DATA', 'SOC-15-2052', 'Data Engineer / Kỹ sư dữ liệu',
     'Thiết kế và vận hành hệ thống đường ống dữ liệu (ETL pipelines), kho dữ liệu và hạ tầng phân tích dữ liệu lớn.',
     'Công nghệ Thông tin', 17000000.00, 40000000.00),

    ('GRP-DATA', 'SOC-15-1260', 'AI / Machine Learning Engineer / Kỹ sư trí tuệ nhân tạo',
     'Nghiên cứu, huấn luyện và triển khai các mô hình AI/ML, nhận dạng mẫu và tích hợp các giải pháp LLM hiện đại.',
     'Công nghệ Thông tin', 20000000.00, 50000000.00)
) AS o(group_code, code, title, description, industry, entry_salary_avg, mid_salary_avg)
JOIN occupation_groups g ON g.code = o.group_code
ON CONFLICT (code) DO NOTHING;

-- 5. Occupation Skills (Mapping Weights & Requirements)
INSERT INTO occupation_skills (occupation_id, skill_id, importance_weight, is_required)
SELECT occ.id, sk.id, m.importance_weight, m.is_required
FROM (VALUES
    -- Software Developer (SOC-15-1252)
    ('SOC-15-1252', 'SK-JAVA', 1.00, true),
    ('SOC-15-1252', 'SK-SPRING', 0.90, true),
    ('SOC-15-1252', 'SK-SQL', 0.85, true),
    ('SOC-15-1252', 'SK-POSTGRES', 0.80, false),
    ('SOC-15-1252', 'SK-GIT', 0.85, true),
    ('SOC-15-1252', 'SK-DOCKER', 0.70, false),
    ('SOC-15-1252', 'SK-PROB-SOLVING', 0.80, true),

    -- Frontend Engineer (SOC-15-1253)
    ('SOC-15-1253', 'SK-JS', 1.00, true),
    ('SOC-15-1253', 'SK-TS', 0.95, true),
    ('SOC-15-1253', 'SK-REACT', 0.95, true),
    ('SOC-15-1253', 'SK-NEXT', 0.75, false),
    ('SOC-15-1253', 'SK-GIT', 0.80, true),
    ('SOC-15-1253', 'SK-TEAMWORK', 0.75, false),

    -- Backend Engineer (SOC-15-1254)
    ('SOC-15-1254', 'SK-JAVA', 1.00, true),
    ('SOC-15-1254', 'SK-SPRING', 1.00, true),
    ('SOC-15-1254', 'SK-SQL', 0.90, true),
    ('SOC-15-1254', 'SK-POSTGRES', 0.85, true),
    ('SOC-15-1254', 'SK-REDIS', 0.75, false),
    ('SOC-15-1254', 'SK-DOCKER', 0.80, false),
    ('SOC-15-1254', 'SK-CICD', 0.70, false),

    -- Fullstack Developer (SOC-15-1255)
    ('SOC-15-1255', 'SK-TS', 0.90, true),
    ('SOC-15-1255', 'SK-REACT', 0.90, true),
    ('SOC-15-1255', 'SK-NODE', 0.85, true),
    ('SOC-15-1255', 'SK-SPRING', 0.80, false),
    ('SOC-15-1255', 'SK-SQL', 0.85, true),
    ('SOC-15-1255', 'SK-POSTGRES', 0.80, false),
    ('SOC-15-1255', 'SK-DOCKER', 0.75, false),

    -- Mobile Developer (SOC-15-1256)
    ('SOC-15-1256', 'SK-TS', 0.85, true),
    ('SOC-15-1256', 'SK-REACT', 0.85, true),
    ('SOC-15-1256', 'SK-JS', 0.85, true),
    ('SOC-15-1256', 'SK-GIT', 0.75, true),

    -- QA Automation (SOC-15-1251)
    ('SOC-15-1251', 'SK-JAVA', 0.80, true),
    ('SOC-15-1251', 'SK-PYTHON', 0.85, true),
    ('SOC-15-1251', 'SK-CICD', 0.80, true),
    ('SOC-15-1251', 'SK-GIT', 0.75, true),

    -- DevOps Engineer (SOC-15-1257)
    ('SOC-15-1257', 'SK-DOCKER', 1.00, true),
    ('SOC-15-1257', 'SK-K8S', 0.95, true),
    ('SOC-15-1257', 'SK-LINUX', 0.90, true),
    ('SOC-15-1257', 'SK-CICD', 0.95, true),
    ('SOC-15-1257', 'SK-AWS', 0.90, true),
    ('SOC-15-1257', 'SK-TF', 0.80, false),

    -- Cloud Solutions Architect (SOC-15-1258)
    ('SOC-15-1258', 'SK-AWS', 1.00, true),
    ('SOC-15-1258', 'SK-K8S', 0.90, true),
    ('SOC-15-1258', 'SK-DOCKER', 0.85, true),
    ('SOC-15-1258', 'SK-COMM', 0.85, true),
    ('SOC-15-1258', 'SK-PROB-SOLVING', 0.90, true),

    -- Security Engineer (SOC-15-1259)
    ('SOC-15-1259', 'SK-LINUX', 0.90, true),
    ('SOC-15-1259', 'SK-PYTHON', 0.80, true),
    ('SOC-15-1259', 'SK-PROB-SOLVING', 0.90, true),

    -- Data Scientist (SOC-15-2051)
    ('SOC-15-2051', 'SK-PYTHON', 1.00, true),
    ('SOC-15-2051', 'SK-ML', 0.95, true),
    ('SOC-15-2051', 'SK-SQL', 0.85, true),
    ('SOC-15-2051', 'SK-DL', 0.80, false),
    ('SOC-15-2051', 'SK-NLP', 0.75, false),

    -- Data Engineer (SOC-15-2052)
    ('SOC-15-2052', 'SK-PYTHON', 0.95, true),
    ('SOC-15-2052', 'SK-SQL', 1.00, true),
    ('SOC-15-2052', 'SK-POSTGRES', 0.90, true),
    ('SOC-15-2052', 'SK-DATA-ENG', 0.95, true),
    ('SOC-15-2052', 'SK-DOCKER', 0.75, false),

    -- AI / Machine Learning Engineer (SOC-15-1260)
    ('SOC-15-1260', 'SK-PYTHON', 1.00, true),
    ('SOC-15-1260', 'SK-ML', 1.00, true),
    ('SOC-15-1260', 'SK-DL', 0.95, true),
    ('SOC-15-1260', 'SK-NLP', 0.85, false),
    ('SOC-15-1260', 'SK-FASTAPI', 0.80, false),
    ('SOC-15-1260', 'SK-DOCKER', 0.75, false)
) AS m(occ_code, skill_code, importance_weight, is_required)
JOIN occupations occ ON occ.code = m.occ_code
JOIN skills sk ON sk.code = m.skill_code
ON CONFLICT (occupation_id, skill_id) DO NOTHING;

-- 6. Market Demand Indicators (LMI Metrics)
INSERT INTO market_demand_indicators (occupation_id, region, demand_score, growth_rate_pct, open_job_postings_count, competitive_index, snapshot_date)
SELECT occ.id, lmi.region, lmi.demand_score, lmi.growth_rate_pct, lmi.open_job_postings_count, lmi.competitive_index, lmi.snapshot_date::DATE
FROM (VALUES
    ('SOC-15-1252', 'NATIONAL', 88.50, 12.40, 1250, 2.80, '2026-01-01'),
    ('SOC-15-1253', 'NATIONAL', 84.00, 10.20, 980, 2.50, '2026-01-01'),
    ('SOC-15-1254', 'NATIONAL', 91.00, 14.50, 1420, 3.10, '2026-01-01'),
    ('SOC-15-1255', 'NATIONAL', 89.00, 13.00, 1100, 2.90, '2026-01-01'),
    ('SOC-15-1256', 'NATIONAL', 79.50, 8.50, 620, 2.20, '2026-01-01'),
    ('SOC-15-1251', 'NATIONAL', 81.00, 9.10, 710, 2.40, '2026-01-01'),
    ('SOC-15-1257', 'NATIONAL', 93.50, 18.20, 850, 3.40, '2026-01-01'),
    ('SOC-15-1258', 'NATIONAL', 87.00, 11.80, 420, 3.00, '2026-01-01'),
    ('SOC-15-1259', 'NATIONAL', 86.50, 15.00, 560, 3.20, '2026-01-01'),
    ('SOC-15-2051', 'NATIONAL', 85.00, 13.50, 680, 2.70, '2026-01-01'),
    ('SOC-15-2052', 'NATIONAL', 90.50, 16.80, 940, 3.10, '2026-01-01'),
    ('SOC-15-1260', 'NATIONAL', 95.00, 22.50, 890, 3.80, '2026-01-01')
) AS lmi(occ_code, region, demand_score, growth_rate_pct, open_job_postings_count, competitive_index, snapshot_date)
JOIN occupations occ ON occ.code = lmi.occ_code
ON CONFLICT (occupation_id, region, snapshot_date) DO NOTHING;

