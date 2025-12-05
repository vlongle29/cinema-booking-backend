package com.example.CineBook.model;

import com.example.CineBook.common.constant.GenderEnum;
import com.example.CineBook.common.constant.MembershipLevel;
import com.example.CineBook.common.constant.MembershipLevelEnum;
import com.example.CineBook.model.auditing.AuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Customer extends AuditingEntity {

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    /**
     * Used to auto calculate discount
     */
    @Builder.Default
    @Column(name = "membership_level", length = 20)
    @Enumerated(EnumType.STRING)
    private MembershipLevelEnum membershipLevel = MembershipLevelEnum.BRONZE; // BRONZE, SILVER, GOLD, PLATINUM

    /**
     * Used to redeem gifts or upgrades. Increase upon successful ticket purchase
     */
    @Column(name = "loyalty_points")
    @Builder.Default
    private Integer loyaltyPoints = 0;
}
