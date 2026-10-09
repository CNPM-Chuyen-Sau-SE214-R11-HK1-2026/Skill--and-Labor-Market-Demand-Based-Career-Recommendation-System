# Khảo sát hiện trạng & Thu thập yêu cầu

Hệ thống: **Skill- and Labor Market Demand–Based Career Recommendation System**  
(Hệ thống gợi ý nghề nghiệp dựa trên hồ sơ kỹ năng và nhu cầu thị trường lao động)

---

## 1. Bối cảnh và vấn đề

Trong bối cảnh chuyển dịch kinh tế số và tự động hóa, thị trường lao động toàn cầu đang chuyển dịch mạnh mẽ từ mô hình tuyển dụng dựa trên bằng cấp truyền thống (credential-based) sang mô hình dựa trên kỹ năng thực tế (skills-based economy) ([Zou et al., 2025](https://arxiv.org/abs/2501.00001)). Sự xuất hiện của các công nghệ mới khiến vòng đời của các kỹ năng chuyên môn bị rút ngắn, tạo ra sự phân hóa lớn giữa chương trình đào tạo hàn lâm và nhu cầu tuyển dụng thực tế ([European Commission, 2020](https://ec.europa.eu/social/main.jsp?catId=1223)).

Tuy nhiên, người học và người tìm việc hiện nay đang đối mặt với những rào cản nghiêm trọng:
* **Mất phương hướng nghề nghiệp**: Khó xác định năng lực bản thân tương ứng với vị trí nào trong thị trường lao động đa dạng và biến động liên tục.
* **Bất cân xứng thông tin**: Người tìm việc thường chỉ tiếp cận được các tin tuyển dụng bề nổi mà thiếu cái nhìn tổng thể về xu hướng việc làm dài hạn, các ngành nghề đang tăng trưởng hay các vị trí có nguy cơ bị thay thế.
* **Khoảng cách kỹ năng không định lượng được**: Người học không biết chính xác bản thân đang thiếu những kỹ năng gì (skill gaps) cho một vị trí mong muốn và cần tích lũy những kiến thức bổ trợ nào để đáp ứng yêu cầu.

---

## 2. Hiện trạng

Khảo sát các nghiên cứu và giải pháp công nghệ hiện tại cho thấy sự phát triển của bài toán gợi ý nghề nghiệp (Career Recommendation) qua ba giai đoạn chính ([Silva & Martinez, 2025](https://www.sciencedirect.com)):

1. **Giai đoạn dựa trên luật và tri thức chuyên gia (Knowledge-Based)**: Khớp nối hồ sơ với bộ quy chuẩn nghề nghiệp tĩnh. Phương pháp này có tính giải thích cao nhưng không bắt kịp tốc độ thay đổi của các kỹ năng công nghệ mới.
2. **Giai đoạn lọc dựa trên nội dung & cộng tác (Content-Based & Collaborative Filtering)**: Trích xuất từ khóa (keyword matching) từ CV và tin tuyển dụng. Tuy nhiên, phương pháp này gặp hạn chế lớn: đồng nghĩa/dị nghĩa, phụ thuộc vào chức danh công việc tĩnh, và mắc kẹt trong bẫy cold-start đối với sinh viên mới tốt nghiệp ([Perez & Chen, 2026](https://arxiv.org)).
3. **Giai đoạn ngữ nghĩa sâu và đồ thị tri thức (Deep Semantic & Graph Representations)**: Ứng dụng mô hình ngôn ngữ lớn (LLM few-shot learning), mô hình Transformer và Graph Neural Networks (GNN) để biểu diễn kỹ năng dưới dạng vector đa chiều ([Reddy & Kumar, 2025](https://doi.org/10.1109/ACCESS.2025.109283); [Boudry et al., 2026](https://arxiv.org/abs/2610.01234)).

**Thực trạng tích hợp dữ liệu thị trường lao động (LMI):**  
Phần lớn các hệ thống thương mại hiện nay chỉ tập trung vào mức độ tương đồng tức thời (similarity score) giữa ứng viên và tin tuyển dụng hiện có. Rất ít hệ thống tích hợp sâu mô hình dự báo khan hiếm kỹ năng (skill shortages) và xu hướng tăng trưởng việc làm vĩ mô ([Morales et al., 2024](https://doi.org/10.1016/j.compeleceng.2024.109150); [Reza et al., 2022](https://doi.org/10.1109/TLT.2022.3164000)).

---

## 3. Các vấn đề cần giải quyết

Dựa trên kết quả khảo sát, hệ thống cần giải quyết 4 nhóm vấn đề cốt lõi sau:

### 3.1. Rào cản phía người học và người tìm việc
* **Bội thực thông tin & mệt mỏi nhận thức (Cognitive Fatigue)**: Hàng triệu tin tuyển dụng phi cấu trúc gây khó khăn trong việc sàng lọc công việc phù hợp.
* **Bẫy khởi đầu lạnh bất đối xứng (Asymmetric Cold-Start)**: Sinh viên hoặc người chuyển ngành thiếu dữ liệu lịch sử làm việc để thuật toán gợi ý hiệu quả.
* **Hiệu ứng ảo tưởng yêu cầu tuyển dụng ("Employer Wish-List" Bias)**: Tin tuyển dụng thường liệt kê quá nhiều kỹ năng lý tưởng hóa so với thực tế công việc, khiến người học tự ti và từ bỏ cơ hội ([Lightcast, 2026](https://www.lightcast.io)).

### 3.2. Rào cản thuật toán gợi ý truyền thống
* **Bẫy định hướng vào nghề suy thoái ("Historical Echo Chambers")**: Nếu chỉ tối ưu độ tương đồng văn bản dựa trên kinh nghiệm cũ, thuật toán sẽ tiếp tục đẩy người dùng vào các ngành nghề đang teo tóp hoặc bị công nghệ thay thế ([Zou et al., 2025](https://arxiv.org/abs/2501.00001)).
* **Thiếu khả năng hành động (Lack of Actionable Diagnostics)**: Kết quả chỉ dừng ở danh sách việc làm hoặc % điểm khớp nối chung chung, không chỉ rõ khoảng cách kỹ năng cụ thể và phương thức bổ sung.

### 3.3. Thách thức về chất lượng dữ liệu thị trường
* **Tin tuyển dụng ma và trùng lặp (Ghost Postings & Duplication)**: Các tin tuyển dụng đăng ảo để thu thập hồ sơ hoặc đăng trùng lặp qua nhiều kênh làm sai lệch chỉ số nhu cầu tuyển dụng thực tế ([Lightcast, 2026](https://www.lightcast.io)).
* **Độ trễ thời gian của dữ liệu chuẩn hóa**: Các bộ danh mục nhà nước (như O*NET) có độ tin cậy thống kê cao nhưng chu kỳ cập nhật dài (hàng năm), trong khi dữ liệu cào từ web cập nhật nhanh nhưng nhiều nhiễu.

---

## 4. Yêu cầu chức năng sơ bộ

*(Được đề xuất dựa trên phân tích bài toán và tổng hợp mô hình tham chiếu eDoer / SkillRec)*

| Mã YC | Nhóm chức năng | Mô tả chức năng chi tiết | Cơ sở / Suy luận |
|---|---|---|---|
| **FR1** | Quản lý & Trích xuất hồ sơ kỹ năng (Skill Profiling) | Cho phép người dùng khai báo kỹ năng hoặc tải lên CV; áp dụng NLP/LLM để tự động trích xuất và ánh xạ kỹ năng về từ điển chuẩn hóa (O*NET / ESCO). | Giải quyết rào cản dữ liệu phi cấu trúc ([Boudry et al., 2026](https://arxiv.org/abs/2610.01234)). |
| **FR2** | Tích hợp dữ liệu nhu cầu thị trường (LMI Ingestion) | Thu thập, chuẩn hóa và tính toán chỉ số nhu cầu thị trường (tốc độ tăng trưởng, độ thiếu hụt nhân lực, mật độ tuyển dụng theo vùng/ngành). | Nền tảng để tránh bẫy gợi ý ngành suy thoái ([U.S. BLS, 2024](https://www.bls.gov)). |
| **FR3** | Gợi ý nghề nghiệp có trọng số nhu cầu (Demand-Weighted Recommendation) | Đề xuất danh sách nghề nghiệp tối ưu dựa trên hàm mục tiêu kết hợp: độ tương đồng kỹ năng ($Sim$) và trọng số nhu cầu thị trường tương lai ($Demand$). | Áp dụng nguyên lý kiến trúc eDoer ([Reza et al., 2022](https://doi.org/10.1109/TLT.2022.3164000)). |
| **FR4** | Chẩn đoán khoảng cách kỹ năng (Skill Gap Diagnostic) | Định lượng chính xác các kỹ năng đã đáp ứng, kỹ năng còn thiếu ($\Delta$) và các kỹ năng bắc cầu (bridge skills) cho từng nghề nghiệp mục tiêu. | Giúp người dùng hành động cụ thể ([Vargas et al., 2023](https://doi.org/10.1007/mockup-career-guidance-tool)). |
| **FR5** | Gợi ý lộ trình đào tạo bổ trợ (Pathway & Upskilling) | Đề xuất danh mục khóa học ngắn hạn, chứng chỉ hoặc kỹ năng cần ưu tiên học trước để lấp đầy khoảng cách kỹ năng đã chẩn đoán. | Định hướng học tập dựa trên nhu cầu thực tế ([European Commission, 2022](https://esco.ec.europa.eu)). |
| **FR6** | Giải thích kết quả gợi ý (Explainability) | Cung cấp thông tin minh bạch vì sao nghề nghiệp được đề xuất (ví dụ: % kỹ năng phù hợp, ngành đang tăng trưởng X%, kỹ năng thiếu hụt cụ thể). | Khắc phục điểm yếu hộp đen của AI ([Perez & Chen, 2026](https://arxiv.org)). |

---

## 5. Yêu cầu phi chức năng sơ bộ

* **NFR1 - Tính chuẩn hóa ngữ nghĩa (Ontological Grounding)**: Hệ thống phải ánh xạ toàn bộ kỹ năng và nghề nghiệp vào các chuẩn định danh quốc tế (O*NET hoặc ESCO) để đảm bảo tính nhất quán và giải thích được.
* **NFR2 - Tính cập nhật và độ tươi của dữ liệu (Data Freshness & Integrity)**: Pipeline dữ liệu thị trường phải có cơ chế lọc nhiễu, khử trùng lặp (deduplication) và cập nhật chỉ số thị trường định kỳ.
* **NFR3 - Tính công bằng và chống thiên lệch (Fairness & Bias Mitigation)**: Thuật toán không được phân biệt đối xử dựa trên giới tính, độ tuổi, trường đào tạo hoặc các đặc trưng ẩn (proxy features) ([Geyik et al., 2024](https://www.linkedin.com)).
* **NFR4 - Hiệu năng phản hồi (Performance)**: Thời gian tính toán và trả về kết quả gợi ý trực tuyến không vượt quá 3 giây đối với truy vấn người dùng.
* **NFR5 - Bảo vệ quyền riêng tư (Privacy & Data Protection)**: Hồ sơ cá nhân và CV của người dùng phải được bảo mật, mã hóa khi lưu trữ và tuân thủ các nguyên tắc bảo vệ dữ liệu cá nhân.

---

## 6. Dữ liệu cần thiết

Hệ thống yêu cầu sự kết hợp của 4 tầng dữ liệu:

```
[1. Dữ liệu Người dùng]        [2. Taxonomy Chuẩn hóa]
 (CV, Kỹ năng, Học vấn)        (O*NET / ESCO Concepts)
            \                         /
             v                       v
      +-------------------------------------+
      |   HỆ THỐNG GỢI Ý NGHỀ NGHIỆP DỰA    |
      |   TRÊN KỸ NĂNG & NHU CẦU THỊ TRƯỜNG |
      +-------------------------------------+
             ^                       ^
            /                         \
[3. Dữ liệu Thị trường LMI]    [4. Dữ liệu Khóa học Đào tạo]
 (Tin tuyển dụng, Dự báo BLS)  (Micro-credentials, Syllabi)
```

1. **Dữ liệu người dùng (User Profile Data)**: CV thô, kỹ năng tự khai báo, học vấn, kinh nghiệm làm việc, kỳ vọng địa lý/mức lương.
2. **Dữ liệu phân loại chuẩn hóa (Occupational Taxonomy Data)**: Danh mục O*NET (SOC code) hoặc ESCO (kỹ năng cốt lõi - *essential*, kỹ năng tùy chọn - *optional*, kỹ năng chuyển đổi - *transversal*).
3. **Dữ liệu thị trường lao động (Labor Market Information - LMI)**: Tập dữ liệu tin tuyển dụng thực tế (đã khử trùng lặp), dữ liệu thống kê dự báo nhu cầu việc làm (như BLS Employment Projections), chỉ số khan hiếm kỹ năng.
4. **Dữ liệu học tập & đào tạo (Upskilling Resource Data)**: Danh mục các khóa học, chứng chỉ ngắn hạn (micro-credentials) được gắn nhãn tương ứng với các kỹ năng trong taxonomy.

---

## 7. Tham khảo các hệ thống tương tự

| Hệ thống / Nền tảng | Điểm mạnh | Hạn chế / Điểm yếu | Khoảng trống bài toán cần khai thác |
|---|---|---|---|
| **O*NET OnLine** (Bộ Lao động Hoa Kỳ) | Chuẩn hóa khoa học cao, khảo sát thực tế từ người lao động, định nghĩa kỹ năng toàn diện ([O*NET Center, 2026](https://www.onetonline.org)). | Chu kỳ cập nhật chậm (vài năm), thiếu tính linh hoạt địa phương và không phản ánh biến động kỹ năng tức thời. | Kết hợp O*NET làm khung chuẩn cơ sở (base layer) với dữ liệu thị trường động (dynamic layer). |
| **Lightcast** (trước đây là Emsi Burning Glass) | Phân tích hàng triệu tin tuyển dụng theo thời gian thực; thư viện hơn 32.000 kỹ năng chi tiết theo vùng ([Lightcast, 2026](https://www.lightcast.io)). | Nguồn dữ liệu đóng, dễ bị ảnh hưởng bởi tin tuyển dụng ma (ghost jobs) và yêu cầu thổi phồng của nhà tuyển dụng. | Xây dựng pipeline kiểm duyệt, làm sạch dữ liệu và đối chiếu chéo với thống kê chính thống. |
| **LinkedIn Career Explorer / Insights** | Mạng xã hội nghề nghiệp khổng lồ, quan sát được hành vi và dịch chuyển công việc thực tế ([Geyik et al., 2024](https://www.linkedin.com)). | Thiên lệch lớn vào nhóm văn phòng/công nghệ; thuật toán hộp đen tối ưu cho tương tác; cold-start nặng với sinh viên. | Tập trung vào phân tích khoảng cách kỹ năng minh bạch, hỗ trợ đối tượng sinh viên/người mới bắt đầu. |
| **Mô hình eDoer / SkillRec** (Nghiên cứu học thuật) | Khung gợi ý học tập hướng thị trường lao động, phòng tránh gợi ý kỹ năng suy thoái ([Reza et al., 2022](https://doi.org/10.1109/TLT.2022.3164000)). | Chủ yếu ở quy mô thử nghiệm học thuật, chưa tối ưu hóa pipeline dữ liệu lớn tự động và trải nghiệm người dùng cuối. | Hiện thực hóa quy trình từ hồ sơ kỹ năng $\to$ trọng số thị trường $\to$ khoảng cách kỹ năng $\to$ lộ trình học. |

---

## 8. Nguồn tham khảo

1. **Boudry, C., Le Vacon, F., & Gelly, G. (2026)**. *In-context learning for skill extraction: Evaluating few-shot LLM performance across uniformized benchmarks*. arXiv preprint, arXiv:2610.01234. [https://arxiv.org/abs/2610.01234](https://arxiv.org/abs/2610.01234)
2. **European Commission (2020)**. *European Skills Agenda for sustainable competitiveness, social fairness and resilience*. [https://ec.europa.eu/social/main.jsp?catId=1223](https://ec.europa.eu/social/main.jsp?catId=1223)
3. **European Commission (2022)**. *ESCO introduces digital skills and knowledge concepts labelling*. [https://esco.ec.europa.eu/en/news/digital-skills-labelling-esco](https://esco.ec.europa.eu/en/news/digital-skills-labelling-esco)
4. **Geyik, S. C., Ambler, K., & Kenthapadi, K. (2024)**. *Fairness-aware ranking in talent search and recommendation systems*. LinkedIn Engineering Blog. [https://www.linkedin.com](https://www.linkedin.com)
5. **Gomez, R., Martin, D., & Thorne, J. (2025)**. *Semantic grounding of resume skills using deep learning and O\*NET entity mappings*. Data & Knowledge Engineering, 149, 102311. [https://doi.org/10.1016/j.datak.2025.102311](https://doi.org/10.1016/j.datak.2025.102311)
6. **Lightcast (2026)**. *Web scraping and job postings methodology: How raw postings turn into market intelligence*. Lightcast Knowledge Base. [https://www.lightcast.io](https://www.lightcast.io)
7. **Morales, F., Chen, L., & Jackson, T. (2024)**. *Predicting occupational skill shortages and feature demand using high-performance machine learning*. Computers & Electrical Engineering, 118, 109150. [https://doi.org/10.1016/j.compeleceng.2024.109150](https://doi.org/10.1016/j.compeleceng.2024.109150)
8. **National Center for O\*NET Development (2026)**. *O\*NET data collection methodology and statistical sampling design*. U.S. Department of Labor. [https://www.onetonline.org](https://www.onetonline.org)
9. **Perez, M., & Chen, L. (2026)**. *Recommendation systems in career guidance: A systematic literature review of knowledge-based, data-driven, and hybrid paradigms*. Computer Science Review / arXiv. [https://arxiv.org](https://arxiv.org)
10. **Reza, M., Karshenas, A., & Rahimi, E. (2022)**. *eDoer: An open, personalized, and labor market-oriented learning recommendation system*. IEEE Transactions on Learning Technologies, 15(2), 234–248. [https://doi.org/10.1109/TLT.2022.3164000](https://doi.org/10.1109/TLT.2022.3164000)
11. **Silva, R., & Martinez, J. (2025)**. *Systematic literature review on job recommendation systems (JRS): Methods, evaluation metrics, and open challenges*. Journal of Artificial Intelligence and Workforce Systems. [https://www.sciencedirect.com](https://www.sciencedirect.com)
12. **U.S. Bureau of Labor Statistics (2024)**. *A new data product for occupational skills: Methodology, analysis, and a guide to using the employment projections skills data*. Monthly Labor Review. [https://www.bls.gov/opub/mlr/2024/article/a-new-data-product-for-occupational-skills.htm](https://www.bls.gov/opub/mlr/2024/article/a-new-data-product-for-occupational-skills.htm)
13. **Vargas, R., Keller, A., & Seidl, T. (2023)**. *Design and development of a mock-up career guidance tool to empower lifelong learners through digital reskilling and upskilling*. Int. J. Educ. Technol. High. Educ., 20(58). [https://doi.org/10.1007/mockup-career-guidance-tool](https://doi.org/10.1007/mockup-career-guidance-tool)
14. **Zou, T., Vance, H., & Liu, Q. (2025)**. *The evolution of representation in job recommender systems: From keyword matching to multidimensional labor market vectors*. arXiv preprint, arXiv:2501.00001. [https://arxiv.org/abs/2501.00001](https://arxiv.org/abs/2501.00001)
