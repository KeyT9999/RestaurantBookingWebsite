# 🪑 SƠ ĐỒ BỐ TRÍ BÀN CHO 10 NHÀ HÀNG

## 📋 Cấu hình bàn (mỗi nhà hàng có 10 bàn):
- Bàn 1: 2 người
- Bàn 2: 4 người
- Bàn 3: 4 người
- Bàn 4: 6 người
- Bàn 5: 6 người
- Bàn 6: 8 người
- Bàn 7: 10 người
- Phòng VIP 1: 12 người
- Phòng VIP 2: 15 người
- Sân ngoài trời: 20 người

---

## 1️⃣ Cơm niêu 3 Cá Bống – Nguyễn Tri Phương

```mermaid
graph TB
    subgraph "Tầng 1 - Khu vực chính"
        direction TB
        ENTRANCE[🚪 CỬA VÀO]
        CASHIER[💰 QUẦY THU NGÂN]
        
        subgraph "Khu vực A - Bàn nhỏ"
            T1[Bàn 1<br/>2 người]
            T2[Bàn 2<br/>4 người]
            T3[Bàn 3<br/>4 người]
        end
        
        subgraph "Khu vực B - Bàn vừa"
            T4[Bàn 4<br/>6 người]
            T5[Bàn 5<br/>6 người]
            T6[Bàn 6<br/>8 người]
        end
        
        subgraph "Khu vực C - Bàn lớn"
            T7[Bàn 7<br/>10 người]
        end
    end
    
    subgraph "Tầng 2 - Phòng VIP"
        VIP1[Phòng VIP 1<br/>12 người]
        VIP2[Phòng VIP 2<br/>15 người]
    end
    
    subgraph "Sân ngoài"
        OUTDOOR[Sân ngoài trời<br/>20 người]
    end
    
    ENTRANCE --> CASHIER
    CASHIER --> T1
    CASHIER --> T2
    CASHIER --> T3
    T1 --> T4
    T2 --> T5
    T3 --> T6
    T4 --> T7
    T7 --> VIP1
    VIP1 --> VIP2
    VIP2 --> OUTDOOR
    
    style T1 fill:#90EE90
    style T2 fill:#90EE90
    style T3 fill:#90EE90
    style T4 fill:#87CEEB
    style T5 fill:#87CEEB
    style T6 fill:#FFD700
    style T7 fill:#FFD700
    style VIP1 fill:#FF6347
    style VIP2 fill:#FF6347
    style OUTDOOR fill:#98D8C8
```

---

## 2️⃣ Country BBQ & Beer - Trần Bạch Đằng

```mermaid
graph LR
    subgraph "Khu vực chính - BBQ"
        direction TB
        ENTRANCE[🚪 CỬA VÀO]
        BAR[🍺 QUẦY BAR]
        
        subgraph "Row 1 - Bàn nhỏ"
            T1[Bàn 1<br/>2 người] --- T2[Bàn 2<br/>4 người] --- T3[Bàn 3<br/>4 người]
        end
        
        subgraph "Row 2 - Bàn vừa"
            T4[Bàn 4<br/>6 người] --- T5[Bàn 5<br/>6 người] --- T6[Bàn 6<br/>8 người]
        end
        
        subgraph "Row 3 - Bàn lớn"
            T7[Bàn 7<br/>10 người]
        end
    end
    
    subgraph "Khu vực VIP - Tách biệt"
        VIP1[Phòng VIP 1<br/>12 người]
        VIP2[Phòng VIP 2<br/>15 người]
    end
    
    subgraph "Sân BBQ ngoài trời"
        OUTDOOR[Sân ngoài trời<br/>20 người<br/>BBQ Area]
    end
    
    ENTRANCE --> BAR
    BAR --> T1
    T1 --> T4
    T2 --> T5
    T3 --> T6
    T4 --> T7
    T7 --> VIP1
    VIP1 --> VIP2
    BAR --> OUTDOOR
    
    style T1 fill:#FFB347
    style T2 fill:#FFB347
    style T3 fill:#FFB347
    style T4 fill:#FF8C00
    style T5 fill:#FF8C00
    style T6 fill:#FF8C00
    style T7 fill:#FF6347
    style VIP1 fill:#DC143C
    style VIP2 fill:#DC143C
    style OUTDOOR fill:#FF4500
```

---

## 3️⃣ Hải Sản Bà Cường – Hoàng Sa

```mermaid
graph TB
    subgraph "Tầng 1 - Khu vực chính"
        ENTRANCE[🚪 CỬA VÀO]
        TANK[🐟 BỂ HẢI SẢN]
        
        subgraph "Khu A - Bàn nhỏ ven cửa sổ"
            T1[Bàn 1<br/>2 người]
            T2[Bàn 2<br/>4 người]
            T3[Bàn 3<br/>4 người]
        end
        
        subgraph "Khu B - Bàn vừa giữa nhà"
            T4[Bàn 4<br/>6 người]
            T5[Bàn 5<br/>6 người]
            T6[Bàn 6<br/>8 người]
        end
        
        subgraph "Khu C - Bàn lớn"
            T7[Bàn 7<br/>10 người]
        end
    end
    
    subgraph "Tầng 2 - Phòng VIP"
        VIP1[Phòng VIP 1<br/>12 người<br/>View đẹp]
        VIP2[Phòng VIP 2<br/>15 người<br/>View đẹp]
    end
    
    subgraph "Sân ngoài - View biển"
        OUTDOOR[Sân ngoài trời<br/>20 người<br/>View biển]
    end
    
    ENTRANCE --> TANK
    TANK --> T1
    TANK --> T2
    TANK --> T3
    T1 --> T4
    T2 --> T5
    T3 --> T6
    T4 --> T7
    T7 --> VIP1
    VIP1 --> VIP2
    TANK --> OUTDOOR
    
    style T1 fill:#87CEEB
    style T2 fill:#87CEEB
    style T3 fill:#87CEEB
    style T4 fill:#4682B4
    style T5 fill:#4682B4
    style T6 fill:#4682B4
    style T7 fill:#1E90FF
    style VIP1 fill:#0000CD
    style VIP2 fill:#0000CD
    style OUTDOOR fill:#00CED1
```

---

## 4️⃣ Hải Sản Ngọc Hương – Võ Nguyên Giáp

```mermaid
graph LR
    subgraph "Khu vực chính - Sang trọng"
        direction TB
        ENTRANCE[🚪 CỬA VÀO]
        RECEPTION[👔 LỄ TÂN]
        
        subgraph "Khu vực A - Bàn nhỏ"
            T1[Bàn 1<br/>2 người] --- T2[Bàn 2<br/>4 người] --- T3[Bàn 3<br/>4 người]
        end
        
        subgraph "Khu vực B - Bàn vừa"
            T4[Bàn 4<br/>6 người] --- T5[Bàn 5<br/>6 người] --- T6[Bàn 6<br/>8 người]
        end
        
        subgraph "Khu vực C - Bàn lớn"
            T7[Bàn 7<br/>10 người]
        end
    end
    
    subgraph "Khu VIP - Tách biệt"
        VIP1[Phòng VIP 1<br/>12 người<br/>Private]
        VIP2[Phòng VIP 2<br/>15 người<br/>Private]
    end
    
    subgraph "Sân ngoài - Premium"
        OUTDOOR[Sân ngoài trời<br/>20 người<br/>Premium]
    end
    
    ENTRANCE --> RECEPTION
    RECEPTION --> T1
    T1 --> T4
    T2 --> T5
    T3 --> T6
    T4 --> T7
    T7 --> VIP1
    VIP1 --> VIP2
    RECEPTION --> OUTDOOR
    
    style T1 fill:#E6E6FA
    style T2 fill:#E6E6FA
    style T3 fill:#E6E6FA
    style T4 fill:#9370DB
    style T5 fill:#9370DB
    style T6 fill:#9370DB
    style T7 fill:#8A2BE2
    style VIP1 fill:#4B0082
    style VIP2 fill:#4B0082
    style OUTDOOR fill:#9400D3
```

---

## 5️⃣ Nhà hàng Akataiyo Mặt Trời Đỏ - Nguyễn Du

```mermaid
graph TB
    subgraph "Khu vực chính - Phong cách Nhật"
        ENTRANCE[🚪 CỬA VÀO<br/>Truyền thống]
        SUSHI_BAR[🍣 QUẦY SUSHI]
        
        subgraph "Khu vực A - Bàn nhỏ"
            T1[Bàn 1<br/>2 người<br/>Tatami]
            T2[Bàn 2<br/>4 người<br/>Tatami]
            T3[Bàn 3<br/>4 người<br/>Tatami]
        end
        
        subgraph "Khu vực B - Bàn vừa"
            T4[Bàn 4<br/>6 người]
            T5[Bàn 5<br/>6 người]
            T6[Bàn 6<br/>8 người]
        end
        
        subgraph "Khu vực C - Bàn lớn"
            T7[Bàn 7<br/>10 người]
        end
    end
    
    subgraph "Khu VIP - Phòng riêng"
        VIP1[Phòng VIP 1<br/>12 người<br/>Tatami Room]
        VIP2[Phòng VIP 2<br/>15 người<br/>Tatami Room]
    end
    
    subgraph "Sân ngoài - Zen Garden"
        OUTDOOR[Sân ngoài trời<br/>20 người<br/>Zen Garden]
    end
    
    ENTRANCE --> SUSHI_BAR
    SUSHI_BAR --> T1
    SUSHI_BAR --> T2
    SUSHI_BAR --> T3
    T1 --> T4
    T2 --> T5
    T3 --> T6
    T4 --> T7
    T7 --> VIP1
    VIP1 --> VIP2
    SUSHI_BAR --> OUTDOOR
    
    style T1 fill:#FFB6C1
    style T2 fill:#FFB6C1
    style T3 fill:#FFB6C1
    style T4 fill:#FF69B4
    style T5 fill:#FF69B4
    style T6 fill:#FF69B4
    style T7 fill:#FF1493
    style VIP1 fill:#DC143C
    style VIP2 fill:#DC143C
    style OUTDOOR fill:#C71585
```

---

## 6️⃣ Phố Biển – Đảo Xanh

```mermaid
graph LR
    subgraph "Khu vực chính - Đảo xanh"
        direction TB
        ENTRANCE[🚪 CỬA VÀO]
        GARDEN[🌴 VƯỜN XANH]
        
        subgraph "Khu A - Bàn nhỏ"
            T1[Bàn 1<br/>2 người] --- T2[Bàn 2<br/>4 người] --- T3[Bàn 3<br/>4 người]
        end
        
        subgraph "Khu B - Bàn vừa"
            T4[Bàn 4<br/>6 người] --- T5[Bàn 5<br/>6 người] --- T6[Bàn 6<br/>8 người]
        end
        
        subgraph "Khu C - Bàn lớn"
            T7[Bàn 7<br/>10 người]
        end
    end
    
    subgraph "Khu VIP - Phòng riêng"
        VIP1[Phòng VIP 1<br/>12 người]
        VIP2[Phòng VIP 2<br/>15 người]
    end
    
    subgraph "Sân ngoài - Đảo xanh"
        OUTDOOR[Sân ngoài trời<br/>20 người<br/>Đảo Xanh]
    end
    
    ENTRANCE --> GARDEN
    GARDEN --> T1
    T1 --> T4
    T2 --> T5
    T3 --> T6
    T4 --> T7
    T7 --> VIP1
    VIP1 --> VIP2
    GARDEN --> OUTDOOR
    
    style T1 fill:#90EE90
    style T2 fill:#90EE90
    style T3 fill:#90EE90
    style T4 fill:#32CD32
    style T5 fill:#32CD32
    style T6 fill:#32CD32
    style T7 fill:#228B22
    style VIP1 fill:#006400
    style VIP2 fill:#006400
    style OUTDOOR fill:#00FF00
```

---

## 7️⃣ The Anchor (Restaurant & Bierhaus) - Trần Phú

```mermaid
graph TB
    subgraph "Khu vực chính - Pub style"
        ENTRANCE[🚪 CỬA VÀO]
        BAR[🍺 QUẦY BAR<br/>Bierhaus]
        TV[📺 MÀN HÌNH LỚN]
        
        subgraph "Khu A - Bàn nhỏ gần bar"
            T1[Bàn 1<br/>2 người] --- T2[Bàn 2<br/>4 người] --- T3[Bàn 3<br/>4 người]
        end
        
        subgraph "Khu B - Bàn vừa giữa"
            T4[Bàn 4<br/>6 người] --- T5[Bàn 5<br/>6 người] --- T6[Bàn 6<br/>8 người]
        end
        
        subgraph "Khu C - Bàn lớn"
            T7[Bàn 7<br/>10 người]
        end
    end
    
    subgraph "Khu VIP - Private"
        VIP1[Phòng VIP 1<br/>12 người<br/>Private]
        VIP2[Phòng VIP 2<br/>15 người<br/>Private]
    end
    
    subgraph "Sân ngoài - Terrace"
        OUTDOOR[Sân ngoài trời<br/>20 người<br/>Terrace]
    end
    
    ENTRANCE --> BAR
    BAR --> TV
    TV --> T1
    TV --> T2
    TV --> T3
    T1 --> T4
    T2 --> T5
    T3 --> T6
    T4 --> T7
    T7 --> VIP1
    VIP1 --> VIP2
    BAR --> OUTDOOR
    
    style T1 fill:#DEB887
    style T2 fill:#DEB887
    style T3 fill:#DEB887
    style T4 fill:#CD853F
    style T5 fill:#CD853F
    style T6 fill:#CD853F
    style T7 fill:#8B4513
    style VIP1 fill:#654321
    style VIP2 fill:#654321
    style OUTDOOR fill:#A0522D
```

---

## 8️⃣ Vietbamboo Restaurant - Phạm Văn Đồng

```mermaid
graph LR
    subgraph "Khu vực chính - Truyền thống Việt"
        direction TB
        ENTRANCE[🚪 CỬA VÀO]
        BAMBOO[🎋 KIẾN TRÚC TRÚC]
        
        subgraph "Khu A - Bàn nhỏ"
            T1[Bàn 1<br/>2 người] --- T2[Bàn 2<br/>4 người] --- T3[Bàn 3<br/>4 người]
        end
        
        subgraph "Khu B - Bàn vừa"
            T4[Bàn 4<br/>6 người] --- T5[Bàn 5<br/>6 người] --- T6[Bàn 6<br/>8 người]
        end
        
        subgraph "Khu C - Bàn lớn"
            T7[Bàn 7<br/>10 người]
        end
    end
    
    subgraph "Khu VIP - Phòng riêng"
        VIP1[Phòng VIP 1<br/>12 người<br/>Phòng riêng]
        VIP2[Phòng VIP 2<br/>15 người<br/>Phòng riêng]
    end
    
    subgraph "Sân ngoài - Vườn tre"
        OUTDOOR[Sân ngoài trời<br/>20 người<br/>Vườn tre]
    end
    
    ENTRANCE --> BAMBOO
    BAMBOO --> T1
    T1 --> T4
    T2 --> T5
    T3 --> T6
    T4 --> T7
    T7 --> VIP1
    VIP1 --> VIP2
    BAMBOO --> OUTDOOR
    
    style T1 fill:#F0E68C
    style T2 fill:#F0E68C
    style T3 fill:#F0E68C
    style T4 fill:#DAA520
    style T5 fill:#DAA520
    style T6 fill:#DAA520
    style T7 fill:#B8860B
    style VIP1 fill:#8B6914
    style VIP2 fill:#8B6914
    style OUTDOOR fill:#9ACD32
```

---

## 9️⃣ Vườn Nướng - Đường 304

```mermaid
graph TB
    subgraph "Khu vực chính - BBQ vườn"
        ENTRANCE[🚪 CỬA VÀO]
        GRILL[🔥 KHU VỰC NƯỚNG]
        
        subgraph "Khu A - Bàn nhỏ"
            T1[Bàn 1<br/>2 người<br/>Nướng tại bàn] --- T2[Bàn 2<br/>4 người<br/>Nướng tại bàn] --- T3[Bàn 3<br/>4 người<br/>Nướng tại bàn]
        end
        
        subgraph "Khu B - Bàn vừa"
            T4[Bàn 4<br/>6 người<br/>Nướng tại bàn] --- T5[Bàn 5<br/>6 người<br/>Nướng tại bàn] --- T6[Bàn 6<br/>8 người<br/>Nướng tại bàn]
        end
        
        subgraph "Khu C - Bàn lớn"
            T7[Bàn 7<br/>10 người<br/>Nướng tại bàn]
        end
    end
    
    subgraph "Khu VIP - Phòng riêng"
        VIP1[Phòng VIP 1<br/>12 người<br/>BBQ Private]
        VIP2[Phòng VIP 2<br/>15 người<br/>BBQ Private]
    end
    
    subgraph "Sân ngoài - BBQ vườn"
        OUTDOOR[Sân ngoài trời<br/>20 người<br/>BBQ Vườn]
    end
    
    ENTRANCE --> GRILL
    GRILL --> T1
    GRILL --> T2
    GRILL --> T3
    T1 --> T4
    T2 --> T5
    T3 --> T6
    T4 --> T7
    T7 --> VIP1
    VIP1 --> VIP2
    GRILL --> OUTDOOR
    
    style T1 fill:#FF4500
    style T2 fill:#FF4500
    style T3 fill:#FF4500
    style T4 fill:#FF6347
    style T5 fill:#FF6347
    style T6 fill:#FF6347
    style T7 fill:#DC143C
    style VIP1 fill:#8B0000
    style VIP2 fill:#8B0000
    style OUTDOOR fill:#FF1493
```

---

## 🔟 Zzuggubbong - Nguyễn Hữu Thông

```mermaid
graph LR
    subgraph "Khu vực chính - Phong cách Hàn"
        direction TB
        ENTRANCE[🚪 CỬA VÀO]
        KOREAN_BAR[🍶 QUẦY HÀN]
        
        subgraph "Khu A - Bàn nhỏ"
            T1[Bàn 1<br/>2 người<br/>Sitting] --- T2[Bàn 2<br/>4 người<br/>Sitting] --- T3[Bàn 3<br/>4 người<br/>Sitting]
        end
        
        subgraph "Khu B - Bàn vừa"
            T4[Bàn 4<br/>6 người<br/>BBQ] --- T5[Bàn 5<br/>6 người<br/>BBQ] --- T6[Bàn 6<br/>8 người<br/>BBQ]
        end
        
        subgraph "Khu C - Bàn lớn"
            T7[Bàn 7<br/>10 người<br/>BBQ]
        end
    end
    
    subgraph "Khu VIP - Phòng riêng"
        VIP1[Phòng VIP 1<br/>12 người<br/>Private Room]
        VIP2[Phòng VIP 2<br/>15 người<br/>Private Room]
    end
    
    subgraph "Sân ngoài - Korean style"
        OUTDOOR[Sân ngoài trời<br/>20 người<br/>Korean Style]
    end
    
    ENTRANCE --> KOREAN_BAR
    KOREAN_BAR --> T1
    T1 --> T4
    T2 --> T5
    T3 --> T6
    T4 --> T7
    T7 --> VIP1
    VIP1 --> VIP2
    KOREAN_BAR --> OUTDOOR
    
    style T1 fill:#FFD700
    style T2 fill:#FFD700
    style T3 fill:#FFD700
    style T4 fill:#FFA500
    style T5 fill:#FFA500
    style T6 fill:#FFA500
    style T7 fill:#FF8C00
    style VIP1 fill:#FF6347
    style VIP2 fill:#FF6347
    style OUTDOOR fill:#FF4500
```

---

## 📊 BẢNG TỔNG HỢP

| Nhà hàng | Tổng sức chứa | Khu vực chính | Khu VIP | Sân ngoài |
|----------|---------------|---------------|---------|-----------|
| 1. Cơm niêu 3 Cá Bống | 93 người | 7 bàn (40 người) | 2 phòng (27 người) | 1 sân (20 người) |
| 2. Country BBQ & Beer | 93 người | 7 bàn (40 người) | 2 phòng (27 người) | 1 sân (20 người) |
| 3. Hải Sản Bà Cường | 93 người | 7 bàn (40 người) | 2 phòng (27 người) | 1 sân (20 người) |
| 4. Hải Sản Ngọc Hương | 93 người | 7 bàn (40 người) | 2 phòng (27 người) | 1 sân (20 người) |
| 5. Akataiyo | 93 người | 7 bàn (40 người) | 2 phòng (27 người) | 1 sân (20 người) |
| 6. Phố Biển | 93 người | 7 bàn (40 người) | 2 phòng (27 người) | 1 sân (20 người) |
| 7. The Anchor | 93 người | 7 bàn (40 người) | 2 phòng (27 người) | 1 sân (20 người) |
| 8. Vietbamboo | 93 người | 7 bàn (40 người) | 2 phòng (27 người) | 1 sân (20 người) |
| 9. Vườn Nướng | 93 người | 7 bàn (40 người) | 2 phòng (27 người) | 1 sân (20 người) |
| 10. Zzuggubbong | 93 người | 7 bàn (40 người) | 2 phòng (27 người) | 1 sân (20 người) |

**Tổng sức chứa tối đa mỗi nhà hàng: 93 người**

---

## 🎨 CHÚ GIẢI MÀU SẮC

- 🟢 **Xanh lá nhạt**: Bàn nhỏ (2-4 người)
- 🔵 **Xanh dương**: Bàn vừa (6 người)
- 🟡 **Vàng**: Bàn lớn (8-10 người)
- 🔴 **Đỏ**: Phòng VIP (12-15 người)
- 🟢 **Xanh lá đậm/Xanh ngọc**: Sân ngoài trời (20 người)

---

## 📝 LƯU Ý

- Tất cả sơ đồ được thiết kế để tối ưu không gian và luồng khách
- Khu VIP được đặt ở vị trí tách biệt, riêng tư
- Sân ngoài trời thường gần khu vực chính để dễ phục vụ
- Bố trí bàn theo nguyên tắc: nhỏ gần cửa, lớn ở trong, VIP tách biệt


