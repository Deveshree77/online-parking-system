package com.smartpark.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "slot_locks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SlotLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "lock_start", nullable = false)
    private LocalDateTime lockStart;

    @Column(name = "lock_expiry", nullable = false)
    private LocalDateTime lockExpiry;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) isActive = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(lockExpiry);
    }
}
