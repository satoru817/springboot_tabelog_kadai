package com.example.demo.constants;

import lombok.Getter;

/**
 * システムで使用される役割（ロール）を定義する列挙型です。
 * ユーザーの権限レベルを表し、システムのアクセス制御に使用されます。
 *
 * <p>各ロールは特定の権限セットを持ち、以下の3つのレベルが定義されています：
 * <ul>
 *     <li>ADMIN: システム管理者権限</li>
 *     <li>PAID: 有料会員権限</li>
 *     <li>UNPAID: 無料会員権限</li>
 * </ul>
 * </p>
 *
 * @since 1.0
 */
@Getter
public enum RoleName {
    /**
     * システム管理者権限を表します。
     * システムの全機能にアクセスできる最高権限レベルです。
     */
    ADMIN("ROLE_ADMIN"),

    /**
     * 有料会員権限を表します。
     * 有料会員向けの機能にアクセスできる権限レベルです。
     */
    PAID("ROLE_PAID_USER"),

    /**
     * 無料会員権限を表します。
     * 基本的な機能のみにアクセスできる権限レベルです。
     */
    UNPAID("ROLE_UNPAID_USER");

    /**
     * Spring Securityで使用される実際のロール名を保持します。
     */
    private final String roleName;

    /**
     * Roleの新しいインスタンスを生成します。
     *
     * @param roleName Spring Securityで使用される実際のロール名
     */
    RoleName(String roleName) {
        this.roleName = roleName;
    }
}