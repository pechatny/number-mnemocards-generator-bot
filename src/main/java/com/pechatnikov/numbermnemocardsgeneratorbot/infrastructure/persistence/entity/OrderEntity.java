package com.pechatnikov.numbermnemocardsgeneratorbot.infrastructure.persistence.entity;

import com.pechatnikov.numbermnemocardsgeneratorbot.domain.order.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "\"order\"")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn
    private UserEntity user;

    @Enumerated
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "token_amount")
    private Long tokenAmount;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedDate
    private Instant createdAt;
}